# Payment Gateway / Third-Party API Integration — Interview Prep

For: CreateFuture — Senior Software Engineer (Java) interview
Focus: defensive integration design, testing strategy, resilience under third-party failure

---

## 1. Why this topic matters

The job ad calls out *"Integrating with third-party APIs such as payment gateways"* as a core responsibility. Interviewers ask about this because payment integrations are the textbook case where sloppy engineering costs real money — a duplicate charge, a lost webhook, or an unhandled timeout isn't a cosmetic bug, it's an incident. They want to see that you think in terms of **failure modes**, not just the happy path.

This doc covers the three pillars they're likely to probe:

1. **Defensive design** — retries, idempotency, webhook verification
2. **Testing strategy** — sandboxes, mocks, contract testing
3. **Resilience** — circuit breakers, fallbacks, degraded-mode behavior

Each section has the theory, a Java/Spring-flavored example, and framing you can reuse in an answer.

---

## 2. Defensive Integration Design

### 2.1 Idempotency Keys

**The problem:** Your service calls the payment gateway to charge a card. The request times out — but you don't know if the charge actually went through on their side before the timeout. If you naively retry, you might charge the customer twice.

**The fix:** Generate a unique idempotency key per *logical* operation (not per HTTP attempt) and send it with the request. The gateway (Stripe, Adyen, PayPal, etc.) stores the result of the first request under that key — if it sees the same key again, it returns the original result instead of processing a new charge.

**Key design decisions to mention:**
- The key must be generated **once per business operation** (e.g. "checkout attempt #4521"), and reused across all retries of that same attempt — not regenerated on every retry.
- Store the key (and ideally the outcome) on your own side too, in a `payment_attempts` table, so you can reconcile even if the gateway's idempotency window expires (most gateways only guarantee dedup for 24h).
- UUID v4 is a common choice, but deterministic keys (e.g. hash of `orderId + amount + userId`) can be even safer since they survive process restarts without needing persisted state.

**Example (Spring Boot):**

```java
@Service
public class PaymentService {

    private final PaymentGatewayClient gatewayClient;
    private final PaymentAttemptRepository attemptRepository;

    @Transactional
    public PaymentResult charge(Order order) {
        // Deterministic idempotency key — same order + amount always
        // produces the same key, so a retried request (even after a
        // crash/restart) is safe.
        String idempotencyKey = generateIdempotencyKey(order);

        PaymentAttempt attempt = attemptRepository
            .findByIdempotencyKey(idempotencyKey)
            .orElseGet(() -> attemptRepository.save(
                PaymentAttempt.pending(idempotencyKey, order)));

        if (attempt.isTerminal()) {
            // Already succeeded or failed — don't call the gateway again
            return attempt.toResult();
        }

        ChargeRequest request = ChargeRequest.builder()
            .amount(order.getAmount())
            .currency(order.getCurrency())
            .idempotencyKey(idempotencyKey)
            .build();

        ChargeResponse response = gatewayClient.charge(request);
        attempt.markComplete(response);
        attemptRepository.save(attempt);

        return attempt.toResult();
    }

    private String generateIdempotencyKey(Order order) {
        return DigestUtils.sha256Hex(order.getId() + ":" + order.getAmount());
    }
}
```

**Talking point:** *"I'd rather over-engineer idempotency at the boundary than debug a double-charge in production — it's much cheaper to prevent than to refund."*

---

### 2.2 Retries with Backoff

**The problem:** Networks are unreliable. A payment call can fail due to a transient blip (DNS hiccup, brief gateway overload) that would succeed on the very next attempt — but naive retry storms can make an already-struggling third party worse.

**Rules of thumb to mention:**
- **Only retry idempotent/safe operations**, or non-idempotent ones *paired with* an idempotency key (see above).
- **Retry on transient errors only**: timeouts, connection resets, HTTP 502/503/504. **Never retry on 4xx** (bad request, invalid card, auth failure) — retrying a client error just repeats the same failure.
- Use **exponential backoff with jitter** to avoid synchronized retry storms across many clients hitting the gateway at once.
- Cap the number of attempts and have a clear terminal failure state (e.g. mark the order "payment pending — manual review" rather than retrying forever).

**Example using Resilience4j** (you already have hands-on experience with this from the Lounge project, so lean on it):

```java
@Configuration
public class ResilienceConfig {

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                500, 2.0, 0.5)) // base, multiplier, jitter factor
            .retryOnException(ex ->
                ex instanceof SocketTimeoutException
                || ex instanceof GatewayServerErrorException) // 5xx only
            .build();
        return RetryRegistry.of(config);
    }
}
```

```java
Retry retry = retryRegistry.retry("paymentGateway");

Supplier<ChargeResponse> chargeSupplier = Retry.decorateSupplier(retry,
    () -> gatewayClient.charge(request));

ChargeResponse response = chargeSupplier.get();
```

**Talking point:** *"Retries without idempotency are dangerous, and idempotency without retries is pointless — the two go together."*

---

### 2.3 Webhook Verification

**The problem:** Payment gateways notify you asynchronously via webhooks — "payment succeeded," "payment failed," "dispute opened." Since this is an inbound HTTP call to *your* server, anyone could forge a POST request that looks like a legitimate webhook unless you verify it.

**The fix — signature verification:**
1. The gateway signs the payload (usually HMAC-SHA256) using a shared secret and sends the signature in a header (e.g. `Stripe-Signature`).
2. You recompute the HMAC over the raw request body using the same secret and compare it to the header — **constant-time comparison** to avoid timing attacks.
3. Also verify a **timestamp** included in the signed payload and reject anything older than a few minutes, to prevent replay attacks (someone capturing and resending an old, validly-signed webhook).

**Example (Stripe-style signature verification in Java):**

```java
@RestController
@RequestMapping("/webhooks")
public class PaymentWebhookController {

    private static final long TOLERANCE_SECONDS = 300; // 5 minutes

    @Value("${payment.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/payment-gateway")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Signature") String signatureHeader) {

        WebhookSignature parsed = WebhookSignature.parse(signatureHeader);
        // header looks like: t=1694012345,v1=5257a869e7...

        if (isReplayed(parsed.timestamp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String expectedSignature = computeHmac(
            parsed.timestamp() + "." + rawPayload, webhookSecret);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parsed.signature().getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Safe to process — also check we haven't already handled this
        // event id (webhooks can be delivered more than once).
        processEvent(rawPayload);
        return ResponseEntity.ok().build();
    }

    private boolean isReplayed(long timestamp) {
        long now = Instant.now().getEpochSecond();
        return Math.abs(now - timestamp) > TOLERANCE_SECONDS;
    }

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new WebhookVerificationException(e);
        }
    }
}
```

**Also worth mentioning:**
- Webhooks can arrive **out of order** or **more than once** — your handler must be idempotent too (dedupe on the gateway's event ID).
- Always return `200 OK` quickly once you've durably queued the event; do slow processing (sending emails, updating other systems) asynchronously so the gateway doesn't time out and retry unnecessarily.

**Talking point:** *"A webhook endpoint is just an unauthenticated public endpoint until you verify the signature — I treat it the same as any other untrusted input."*

---

## 3. Testing Strategy

### 3.1 Sandbox Environments

Every major gateway provides a sandbox/test mode (Stripe test keys, PayPal Sandbox, Adyen test environment) with test card numbers that simulate specific outcomes (success, decline, insufficient funds, 3DS challenge, etc.).

**What to mention:**
- Use sandbox for **end-to-end / manual QA** and for exercising specific failure scenarios (declined card, expired card) that are hard to trigger otherwise.
- Keep sandbox credentials clearly separated from production config (different Spring profiles/`application-sandbox.yml`, separate secrets in your vault).
- Sandbox environments can be **slower or flakier** than production — don't rely on them for fast feedback in CI; that's what mocking is for.

### 3.2 Mocking the Third Party in Unit/Integration Tests

You don't want your build to depend on a live network call to a third party. Use a mock HTTP server so you control exact responses, including edge cases (timeouts, malformed JSON, 500s).

**Example using WireMock:**

```java
@SpringBootTest
@AutoConfigureWireMock(port = 0)
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void chargeSucceeds_whenGatewayReturns200() {
        stubFor(post(urlEqualTo("/v1/charges"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    { "id": "ch_123", "status": "succeeded" }
                    """)));

        PaymentResult result = paymentService.charge(testOrder());

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void chargeRetries_whenGatewayReturns503ThenSucceeds() {
        stubFor(post(urlEqualTo("/v1/charges"))
            .inScenario("retry-flow")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(503))
            .willSetStateTo("second-attempt"));

        stubFor(post(urlEqualTo("/v1/charges"))
            .inScenario("retry-flow")
            .whenScenarioStateIs("second-attempt")
            .willReturn(aResponse().withStatus(200)
                .withBody("{ \"id\": \"ch_124\", \"status\": \"succeeded\" }")));

        PaymentResult result = paymentService.charge(testOrder());

        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void chargeFailsGracefully_whenGatewayTimesOut() {
        stubFor(post(urlEqualTo("/v1/charges"))
            .willReturn(aResponse().withFixedDelay(10_000))); // simulate timeout

        assertThatThrownBy(() -> paymentService.charge(testOrder()))
            .isInstanceOf(PaymentTimeoutException.class);
    }
}
```

This lets you test retry logic, timeout handling, and error mapping **deterministically and fast**, without ever touching the real gateway.

### 3.3 Contract Testing

**The problem:** Mocks are only useful if they accurately reflect what the real API does. If the gateway changes their response shape, your mocks will happily keep passing while production breaks.

**The fix — consumer-driven contract testing (e.g. Pact):**
- Your service (the *consumer*) defines a contract: "when I send this request, I expect a response shaped like this."
- That contract is verified against the *provider* (the gateway, or a contract broker they publish to) — for gateways you don't control, this is trickier, but many now publish OpenAPI specs you can validate mocks against automatically (schema validation on your WireMock stubs), which gets you most of the benefit.

**What to mention if you haven't used Pact directly:** you can still describe the *concept* — validating your stubs/mocks against the provider's published OpenAPI/JSON schema so a mock and a real response can't silently drift apart. This is a very reasonable thing to say even without hands-on Pact experience.

---

## 4. Resilience — Handling Third-Party Downtime

### 4.1 Circuit Breaker Pattern

**The problem:** If the payment gateway is down or slow, and you keep sending requests (and waiting for timeouts) on every checkout, you tie up threads/connections and can cascade the outage into your own service.

**The fix:** A circuit breaker tracks failure rate; once it crosses a threshold, it "opens" — failing fast (without even attempting the call) for a cooldown period, then allows a few test requests through ("half-open") to see if the dependency has recovered.

**Example (Resilience4j — you already have this in your Lounge stack):**

```java
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public CircuitBreaker paymentGatewayCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)                 // open at 50% failures
            .slowCallRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(5)
            .slidingWindowSize(20)
            .build();

        return CircuitBreaker.of("paymentGateway", config);
    }
}
```

```java
CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");

Supplier<ChargeResponse> decorated = CircuitBreaker
    .decorateSupplier(cb, () -> gatewayClient.charge(request));

try {
    return decorated.get();
} catch (CallNotPermittedException e) {
    // Circuit is open — fail fast, don't even try the network call
    return handleGatewayUnavailable(request);
}
```

### 4.2 Fallback / Degraded-Mode Behavior

What you actually *do* when the circuit is open matters as much as detecting the outage:

- **Queue for later:** Persist the payment intent (e.g. to a `pending_payments` table or a Kafka topic) and process it asynchronously via a retry worker once the gateway recovers, rather than losing the request.
- **User-facing messaging:** Show "Payment processing is delayed, we'll confirm shortly" instead of a hard failure — much better UX than an error page, and avoids abandoned carts.
- **Bulkhead isolation:** Run calls to the payment gateway in a separate thread pool/connection pool from the rest of your app, so a slow gateway can't starve threads needed for unrelated requests.
- **Timeouts everywhere:** A circuit breaker doesn't help if the individual call itself has no timeout — always set explicit connect/read timeouts on the HTTP client.

**Talking point:** *"The goal isn't to make the third party never fail — it's to make sure their outage degrades gracefully instead of cascading into ours."*

---

## 5. STAR-Format Story Templates (adapt to your Lounge experience)

Even if you haven't personally built a payment gateway integration, the **Lounge project's game-launch flow** (Spring Boot facade/aggregator services, Resilience4j, Redis TTL tokens, JWT/RS256) is structurally the same problem: calling external/downstream services that can be slow, fail, or need secure token verification. Reframe that experience:

**Story template 1 — Resilience**
> *Situation:* "On the Lounge project, our game-launch flow depended on downstream services (aggregator/facade calls) that could be slow or intermittently fail."
> *Task:* "We needed the player-facing experience to stay responsive even when a downstream dependency degraded."
> *Action:* "I used Resilience4j to add circuit breakers and retry policies around those calls, with sensible timeouts and fallback responses instead of letting failures cascade to the frontend."
> *Result:* "[Fill in: reduced timeout-related errors / improved launch success rate / faster incident detection — whatever you actually observed]."

**Story template 2 — Secure inbound verification (maps to webhook verification)**
> *Situation:* "We used JWT (RS256) and Redis-backed TTL tokens to secure the game-launch flow."
> *Task:* "Any request coming through this flow needed to be verified as authentic and not replayed or expired."
> *Action:* "I worked on verifying signed tokens and enforcing TTL expiry via Redis, similar in principle to verifying a signed webhook payload with a timestamp tolerance window."
> *Result:* "[Fill in the outcome/impact]."

Fill in the `[Result]` lines with real specifics before the interview — numbers, incident counts, or "this became the pattern other services adopted" are all strong closers.

---

## 6. Likely Interview Questions & How to Structure Answers

| Question | What they're really testing |
|---|---|
| "How would you prevent double-charging a customer?" | Do you know idempotency keys, and where responsibility lives (client-generated key, stored server-side)? |
| "The payment gateway is returning 503s intermittently — what do you do?" | Retry strategy, backoff, circuit breaker, and knowing *not* to retry blindly |
| "How do you verify a webhook is legitimate?" | HMAC signature verification, replay protection, idempotent processing |
| "How would you test an integration with a payment provider without hitting their real API in CI?" | Mocking (WireMock), sandbox vs. mock distinction, contract testing awareness |
| "What happens to the order if the payment gateway is completely down for 10 minutes?" | Fallback/degraded mode thinking — queuing, async processing, user messaging, not just "it fails" |
| "Walk me through designing a payment integration from scratch." | Can you synthesize all of the above into one coherent design, in order of priority |

---

## 7. Quick-Reference Cheat Sheet

- **Idempotency key** → prevents duplicate side effects on retry
- **Retry only on**: timeouts, connection errors, 5xx — **never** on 4xx
- **Exponential backoff + jitter** → avoid retry storms
- **Webhook = untrusted input** → verify HMAC signature + timestamp/replay check
- **Dedupe webhook events** by provider's event ID
- **Circuit breaker** → fail fast once a dependency is clearly unhealthy
- **Fallback ≠ failure** → queue, degrade gracefully, inform the user
- **Bulkhead** → isolate the payment call's resources from the rest of the app
- **Sandbox** for realistic E2E scenarios, **mocks** for fast deterministic CI tests, **contract tests** to keep mocks honest

---

*Prepared for: CreateFuture Senior Software Engineer (Java) interview — role-specific capability round.*
