# Payhawk — Onsite Interview Prep (Algorithms + Design Engineering)

Role: (fill in) · Format: 3-hour onsite whiteboard, 2 interviewers · Topics: algorithms, data structures, design engineering, system design.

Sources: aggregated candidate reports (JobMentis interview bank) + an unverified but detailed secondary report (user-supplied, Bulgarian-language, not independently confirmed). Reported real prompts:
- **Rate limiter**: stream of `(user_id, timestamp)`, allow at most N requests per user per minute, return true/false.
- **Top-K spenders**: given user IDs + transaction amounts (users can have multiple transactions), return top K users by total spend, efficient for large K and large N.
- **Refactor poorly-written financial code** (e.g. currency conversion with precision bugs) — code quality/readability/maintainability round.
- System design: duplicate expense report detection; notification service (card limit, invoice, payment due) across channels.

### Unverified secondary report — treat as plausible, not confirmed
A second source (not independently verified — no direct candidate transcript, so weight accordingly) claims Payhawk's SWE interview **deliberately avoids abstract LeetCode-style puzzles** and instead focuses on practical systems engineering: how your code choices affect real infrastructure, databases, and load. Per this source, expect two flavors instead of/alongside classic DS&A:

1. **"Optimize this API" scenario** — given a slow real-world endpoint (e.g. "monthly financial report generation is slow due to huge transaction volume"), propose fixes. Graded on:
   - **Database**: composite indexing, avoiding N+1 query problems, read/write replica splitting.
   - **Caching**: where/when to introduce Redis/Memcached, and how to handle cache invalidation.
   - **Async processing**: offloading heavy work to message queues (RabbitMQ/Kafka).
2. **Practical systems algorithms on the whiteboard** (distributed-systems/fintech-flavored, not textbook LeetCode):
   - **Rate limiting**: Token Bucket or Leaky Bucket (not just sliding window — see Section 1c/1d below).
   - **Load balancing**: implement plain or Weighted Round-Robin.
   - **Idempotency**: prevent a double-click "Pay" button from processing a payment twice (idempotency keys) — critical fintech pattern.

Given I can't verify this source directly, treat it as **additive prep, not a replacement** for the three confirmed JobMentis prompts above — cover both.

---

## 1. Rate Limiter — `allow(userId, timestamp) -> boolean`

Grading signals reported: hash map of per-user timestamp queues, correct window-check logic, mention of concurrency (locks/atomics), pruning old entries so memory doesn't grow unbounded.

### 1a. Sliding Window Log (exact, simple — lead with this)

Store every request timestamp per user; on each call, drop timestamps older than the window, then check count.

```java
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowLogRateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    // per-user deque of request timestamps, oldest at the front
    private final Map<String, Deque<Long>> userRequests = new ConcurrentHashMap<>();

    public SlidingWindowLogRateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allow(String userId, long timestamp) {
        Deque<Long> timestamps = userRequests.computeIfAbsent(userId, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // prune anything outside the window from the front
            long windowStart = timestamp - windowMillis;
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(timestamp);
                return true;
            }
            return false;
        }
    }
}
```

**Complexity:** O(1) amortized per call (each timestamp pushed/popped once). **Space:** O(maxRequests) per active user — this is the "pruning" signal they explicitly grade for; without pruning, memory grows unbounded as users make more requests over time.

**Concurrency talking point:** per-user lock (synchronized on that user's deque, not a global lock) so different users don't contend; `ConcurrentHashMap.computeIfAbsent` for safe lazy creation of a user's queue. Mention that a global lock would serialize all users and kill throughput — deliberately scoping the lock to the deque shows you thought about contention.

### 1b. Sliding Window Counter (approximate, O(1) space per user — mention as an optimization)

Trade exactness for memory: keep two fixed windows (current + previous) and interpolate.

```java
public class SlidingWindowCounterRateLimiter {
    private final int maxRequests;
    private final long windowMillis;

    private static class Counter {
        long windowStart;
        int previousCount;
        int currentCount;
    }

    private final Map<String, Counter> userCounters = new ConcurrentHashMap<>();

    public SlidingWindowCounterRateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allow(String userId, long timestamp) {
        Counter c = userCounters.computeIfAbsent(userId, k -> {
            Counter nc = new Counter();
            nc.windowStart = timestamp;
            return nc;
        });

        synchronized (c) {
            long elapsed = timestamp - c.windowStart;

            if (elapsed >= 2 * windowMillis) {
                // fully new window, no overlap with anything counted
                c.windowStart = timestamp;
                c.previousCount = 0;
                c.currentCount = 0;
            } else if (elapsed >= windowMillis) {
                // slide: current becomes previous
                c.windowStart += windowMillis;
                c.previousCount = c.currentCount;
                c.currentCount = 0;
            }

            double overlapRatio = 1.0 - (double) (timestamp - c.windowStart) / windowMillis;
            double weightedCount = c.previousCount * overlapRatio + c.currentCount;

            if (weightedCount < maxRequests) {
                c.currentCount++;
                return true;
            }
            return false;
        }
    }
}
```

**Complexity:** O(1) time and O(1) space per user (vs O(maxRequests) for the log variant) — the right answer when asked "how would you make this scale to millions of users with high request volume."

**Trade-off to state out loud:** approximate — assumes uniform request distribution within the previous window, so it can slightly over/under-count at window boundaries. Sliding-window log is exact but memory-heavier; sliding-window counter is the industry-standard compromise (this is literally how many real API gateways implement it).

### Interview script for this question
1. Clarify: single instance or distributed (multiple servers behind a load balancer)? → if distributed, mention Redis-backed shared counters (`INCR` + `EXPIRE`, or a Lua script for atomicity) instead of in-memory maps.
2. Start with sliding window log — simplest correct solution, get it working.
3. Discuss memory growth, propose pruning (already built in above).
4. Offer sliding window counter as the O(1)-space optimization, state the accuracy trade-off.
5. Mention concurrency: per-user locking, or lock-free with `AtomicLong`/CAS if pushed further.

---

### 1c. Token Bucket — the "practical systems" variant (asked instead of, or alongside, sliding window)

Bucket holds up to `capacity` tokens; refills continuously at a fixed rate; each request consumes 1 token. Allows short bursts up to capacity, then throttles to the steady refill rate. This is the industry-default algorithm (AWS, Stripe, nginx all use variants of it) — if asked "what rate-limiting algorithm would you actually put in production," lead with this, not sliding window.

```java
public class TokenBucket {
    private final long capacity;
    private final double refillTokensPerMillis;
    private double tokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, double refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerMillis = refillTokensPerSecond / 1000.0;
        this.tokens = capacity; // start full
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean allow() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;
        if (elapsed > 0) {
            double newTokens = elapsed * refillTokensPerMillis;
            tokens = Math.min(capacity, tokens + newTokens); // never exceed capacity
            lastRefillTimestamp = now;
        }
    }
}
```

**Verified behavior** (capacity=3, refill=1/sec): first 3 requests allowed instantly (burst), 4th and 5th blocked, allowed again after waiting ~2.1s for refill. **Complexity:** O(1) time and space per request — no per-request history stored, unlike sliding-window-log. **Talking point:** "Token bucket allows controlled bursts up to capacity while enforcing a steady long-run rate — that's usually what you actually want for an API, versus sliding window's harder edge."

### 1d. Leaky Bucket — the alternative, mention the contrast

Same capacity idea, but instead of tokens refilling, requests queue up and "leak out" (get processed) at a fixed constant rate — smooths bursts into a steady output stream rather than allowing them through. Conceptually: a literal bucket with a hole in the bottom leaking at a fixed rate; pour requests in the top, if it overflows they're dropped/rejected.

**Token Bucket vs Leaky Bucket — the distinction to state clearly if asked:**
- **Token bucket**: allows bursts up to capacity, then throttles — good when occasional bursts are fine (typical API rate limiting).
- **Leaky bucket**: enforces a strictly smooth, constant output rate regardless of input burstiness — good when the downstream consumer genuinely cannot handle bursts (e.g. smoothing writes to a fragile legacy system).

### 1e. Idempotency Keys — critical fintech pattern (double-click "Pay" problem)

**Problem:** user double-clicks "Pay," or a client retries after a network timeout without knowing if the first request succeeded — naive handling processes the payment twice.

**Fix:** client generates a unique idempotency key per logical operation (not per HTTP attempt) and sends it with the request; server persists the key with the operation's outcome, so a repeated key returns the original result instead of reprocessing.

```java
@Service
public class PaymentService {
    private final PaymentAttemptRepository attemptRepository;
    private final PaymentGatewayClient gatewayClient;

    @Transactional
    public PaymentResult pay(String idempotencyKey, PaymentRequest request) {
        // Look up first — if this key was already processed, return the stored result
        // instead of charging again. Do this INSIDE a transaction with a unique
        // constraint on idempotencyKey to avoid a race between two concurrent
        // requests with the same key.
        Optional<PaymentAttempt> existing = attemptRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get().toResult(); // already handled — return cached outcome
        }

        PaymentAttempt attempt = attemptRepository.save(
            PaymentAttempt.pending(idempotencyKey, request));

        PaymentResult result = gatewayClient.charge(request);
        attempt.markComplete(result);
        attemptRepository.save(attempt);
        return result;
    }
}
```

**Key points to state out loud:**
- Idempotency key must be **generated once per business operation** and reused across retries of that same attempt — not regenerated per HTTP call.
- Enforce **uniqueness at the database level** (unique constraint on the key column) so two concurrent requests with the same key can't both slip through a check-then-act race — the DB constraint is the real guarantee, the `findBy` lookup is just an optimization to avoid hitting the gateway twice.
- Store the key server-side even though the client generated it, so you can reconcile/debug after the fact.
- This is exactly the same problem/solution shape as `createfuture-Payment Gateway.md` Section 2.1 in this repo — reuse that material if this comes up.

---

## 2. "Optimize this API" — architecture/systems scenario

Reported framing: a real endpoint (e.g. "generate monthly financial report") has become slow due to a large transaction volume. Talk through the fix, not code — this is a whiteboard-diagram/discussion round, not an implementation round.

### Structure your answer in this order (narrate explicitly)
1. **Diagnose first, don't just list fixes** — ask/state: is this read-heavy or write-heavy? What's the current query doing — full table scan, N+1 queries, unindexed filter/sort? What's actual current latency vs. target?
2. **Database layer**
   - **Composite indexes**: index columns in the order they're filtered/sorted (equality fields first, then range, then sort — the ESR rule already in your OfficeRnD notes). E.g. a report filtered by `(orgId, dateRange)` and sorted by `date` wants a compound index on `(orgId, date)`.
   - **N+1 queries**: if the report loop fetches related data per row (e.g. per-transaction category name) instead of one joined/batched query — fix with a JOIN, a single `WHERE id IN (...)` batch fetch, or a DataLoader-style batching pattern.
   - **Read/write splitting**: route the heavy report query to a read replica so it doesn't compete with live transactional writes on the primary.
3. **Caching**
   - Where: cache the computed report (or its expensive sub-aggregates) in Redis/Memcached, keyed by `(orgId, period)`, with a sensible TTL.
   - Cache invalidation: either TTL-based (accept some staleness for a report that doesn't need to be real-time) or event-based (invalidate/recompute when a new transaction lands in that period) — state the trade-off explicitly, don't just say "use a cache."
4. **Async processing**
   - Move report generation off the synchronous request path entirely: client requests generation → job enqueued (RabbitMQ/Kafka) → worker computes and stores the result → client polls or gets notified (ties directly into the notification-system design question) when ready. This is the right answer when the computation is inherently heavy (large aggregation over transactions) rather than a query-optimization problem.
5. **Wrap-up**: state which fix you'd do first and why (usually: fix the query/index before reaching for caching or async — caching a slow query just hides the problem for a while and adds invalidation complexity you didn't need).

**Talking point:** "I'd rather fix the underlying query — the N+1 or missing index — before reaching for a cache, since caching a fundamentally slow query just delays the problem and adds invalidation complexity. Async processing is for cases where the work is inherently heavy, not a band-aid for a fixable query."

---

## 3. Weighted Round-Robin — load balancing (mention if asked to "implement a load balancer")

Distributes requests across servers proportional to each server's weight, spread smoothly (not in weight-sized clumps) — this is the same algorithm nginx uses internally.

```java
public class WeightedRoundRobin {
    record Server(String name, int weight) {}

    private final List<Server> servers;
    private final int[] currentWeights;
    private final int totalWeight;

    public WeightedRoundRobin(List<Server> servers) {
        this.servers = servers;
        this.currentWeights = new int[servers.size()];
        this.totalWeight = servers.stream().mapToInt(Server::weight).sum();
    }

    public synchronized String next() {
        int best = -1;
        for (int i = 0; i < servers.size(); i++) {
            currentWeights[i] += servers.get(i).weight();
            if (best == -1 || currentWeights[i] > currentWeights[best]) {
                best = i;
            }
        }
        currentWeights[best] -= totalWeight;
        return servers.get(best).name();
    }
}
```

**Verified output** for servers A(weight 5), B(weight 1), C(weight 1) over 7 calls: `A A B A C A A` — A gets ~5/7 of traffic, smoothly interleaved rather than "AAAAA BC". **Plain (unweighted) Round-Robin** is the same idea with all weights equal — just cycle through servers with a modulo index, simpler if that's all they ask for.

---

## 4. Top-K Spenders — aggregate then select

`List<UserId> topKSpenders(List<Transaction> transactions, int k)` where `Transaction { userId, amount }`, users can appear multiple times.

### Step 1: Aggregate (always O(n))
```java
Map<String, Double> totalByUser = new HashMap<>();
for (Transaction t : transactions) {
    totalByUser.merge(t.userId(), t.amount(), Double::sum);
}
```

### Step 2a: Min-heap of size K — lead with this (O(n log k))
```java
import java.util.*;

public List<String> topKSpenders(List<Transaction> transactions, int k) {
    Map<String, Double> totalByUser = new HashMap<>();
    for (Transaction t : transactions) {
        totalByUser.merge(t.userId(), t.amount(), Double::sum);
    }

    // min-heap ordered by amount, so the smallest of our current top-k sits at the root
    PriorityQueue<Map.Entry<String, Double>> minHeap =
        new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

    for (Map.Entry<String, Double> entry : totalByUser.entrySet()) {
        minHeap.offer(entry);
        if (minHeap.size() > k) {
            minHeap.poll(); // evict the current smallest
        }
    }

    List<String> result = new ArrayList<>();
    while (!minHeap.isEmpty()) {
        result.add(minHeap.poll().getKey());
    }
    Collections.reverse(result); // largest spender first
    return result;
}
```

**Complexity:** O(n) to aggregate + O(u log k) to select, where u = distinct users (u ≤ n). Much better than sorting all users when k ≪ u.

### Step 2b: Follow-up — "what if k is close to u, or this needs to run continuously as a stream?"
- If k ≈ u: just sort — O(u log u), heap overhead isn't worth it.
- If it's a live stream (transactions keep arriving, need "current top K" at any time): keep the hash map of running totals, and use a **TreeMap<Double, Set<String>>** or an **indexed priority queue** keyed by amount so you can efficiently move a user when their total changes (remove old total, re-insert new total) — O(log u) per update instead of rebuilding the heap from scratch. Mention this is essentially LFU-cache territory.
- If data is too large for memory: two-pass approach — first pass to count/aggregate with external sort or map-reduce style partitioning by user, second pass to select top-k per partition then merge.

### Interview script
1. Clarify: one-shot batch, or streaming/continuously updated?
2. Aggregate with hash map — state this is unavoidable O(n).
3. Selection: propose min-heap of size k immediately (don't sort everything).
4. State complexity, compare to full sort, justify why heap wins when k ≪ n.
5. If they push on streaming, pivot to the indexed-structure answer above.

---

## 5. Currency Conversion Refactor — code quality round

They reportedly hand you a "moderately complex, poorly written function" for financial calculations (e.g. currency conversion) and ask you to refactor for readability/maintainability/robustness.

### Classic planted bugs to watch for and fix
- **`double`/`float` for money** — binary floating point can't represent decimal fractions exactly (e.g. `0.1 + 0.2 != 0.3`); always use `BigDecimal` for money, constructed from a `String` (`new BigDecimal("19.99")`, never `new BigDecimal(19.99)` which inherits the float imprecision).
- **No explicit rounding mode** — `BigDecimal` division without a `RoundingMode` throws `ArithmeticException` on non-terminating decimals; always specify (`RoundingMode.HALF_EVEN` is the banker's-rounding standard for finance, minimizes cumulative bias vs `HALF_UP`).
- **Silent truncation instead of proper rounding.**
- **No null/empty checks** on currency codes or amounts before conversion.
- **Hardcoded exchange rates** instead of injecting a rate provider (testability, single responsibility).
- **Mixing concerns**: fetching the rate, converting, and formatting all inline in one method — split into separate, named, testable steps.
- **No handling of unsupported currency pairs** — should throw a specific exception, not silently return 0 or NaN.
- **Magic numbers** (e.g. `100` for cents conversion) without named constants.

### Before/after shape to rehearse
```java
// BEFORE (typical planted bug pattern)
public double convert(double amount, String from, String to) {
    double rate = getRate(from, to); // hardcoded lookup somewhere
    return amount * rate; // no rounding, double precision loss, no validation
}
```

```java
// AFTER
public class CurrencyConverter {
    private final ExchangeRateProvider rateProvider;

    public CurrencyConverter(ExchangeRateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(from, "source currency must not be null");
        Objects.requireNonNull(to, "target currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }

        BigDecimal rate = rateProvider.getRate(from, to)
            .orElseThrow(() -> new UnsupportedCurrencyPairException(from, to));

        return amount.multiply(rate)
                     .setScale(to.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }
}
```

**Narrate while refactoring:** "I'm switching to BigDecimal because float/double can't represent decimal fractions exactly, which is unacceptable for money. I'm injecting the rate provider so this is testable and doesn't hardcode a dependency. I'm adding explicit validation and a named exception so a bad currency pair fails loudly instead of silently. I'm specifying a rounding mode explicitly rather than relying on a default, and using HALF_EVEN since that's the standard for minimizing cumulative rounding bias in financial systems."

---

## 6. Practice checklist
- [ ] Implement `SlidingWindowLogRateLimiter` from scratch, no reference, in under 20 min.
- [ ] Implement `SlidingWindowCounterRateLimiter` from scratch, explain the trade-off out loud.
- [ ] Implement `TokenBucket` from scratch, under 15 min — this is likely the higher-priority rate-limiting algorithm per the secondary report.
- [ ] Explain Leaky Bucket verbally and contrast it with Token Bucket without needing to code it.
- [ ] Implement Weighted Round-Robin from scratch, and the plain (unweighted) version as a fallback if pressed for time.
- [ ] Walk through the Idempotency Key pattern verbally, including the DB unique-constraint race-condition point — this is a very likely fintech-specific follow-up regardless of which rate-limiting question they ask.
- [ ] Rehearse the "Optimize this API" scenario out loud once, end-to-end, using the 5-step structure in Section 2 — this is a discussion, not code, so practice narrating fluently under time pressure.
- [ ] Implement Top-K spenders with the min-heap approach, from scratch, under 15 min.
- [ ] Time yourself doing a mock "refactor this bad currency code" exercise — write bad code, then fix it, narrating each change.
- [ ] Re-skim `algorithmic-patterns.md` sections: Two Pointers, Monotonic Queue/Deque, Hash Map for windows — directly reusable vocabulary for the rate limiter discussion.
- [ ] Re-skim `createfuture-Payment Gateway.md` Section 2.1 (idempotency keys) — same pattern, more depth, already in this repo.
- [ ] Rehearse the "clarify → brute force → optimize → complexity → test cases" workflow out loud for each (matches your `Questions.md`-style interview mindset notes).
