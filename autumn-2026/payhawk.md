# Payhawk — Onsite Interview Prep (Algorithms + Design Engineering)

Role: (fill in) · Format: 3-hour onsite whiteboard, 2 interviewers · Topics: algorithms, data structures, design engineering, system design.

Sources: aggregated candidate reports (JobMentis interview bank). Reported real prompts:
- **Rate limiter**: stream of `(user_id, timestamp)`, allow at most N requests per user per minute, return true/false.
- **Top-K spenders**: given user IDs + transaction amounts (users can have multiple transactions), return top K users by total spend, efficient for large K and large N.
- **Refactor poorly-written financial code** (e.g. currency conversion with precision bugs) — code quality/readability/maintainability round.
- System design: duplicate expense report detection; notification service (card limit, invoice, payment due) across channels.

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

## 2. Top-K Spenders — aggregate then select

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

## 3. Currency Conversion Refactor — code quality round

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

## 4. Practice checklist
- [ ] Implement `SlidingWindowLogRateLimiter` from scratch, no reference, in under 20 min.
- [ ] Implement `SlidingWindowCounterRateLimiter` from scratch, explain the trade-off out loud.
- [ ] Implement Top-K spenders with the min-heap approach, from scratch, under 15 min.
- [ ] Time yourself doing a mock "refactor this bad currency code" exercise — write bad code, then fix it, narrating each change.
- [ ] Re-skim `algorithmic-patterns.md` sections: Two Pointers, Monotonic Queue/Deque, Hash Map for windows — directly reusable vocabulary for the rate limiter discussion.
- [ ] Rehearse the "clarify → brute force → optimize → complexity → test cases" workflow out loud for each (matches your `Questions.md`-style interview mindset notes).
