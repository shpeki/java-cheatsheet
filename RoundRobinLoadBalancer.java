import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h2>RoundRobinLoadBalancer</h2>
 *
 * <p>A thread-safe implementation of the <b>round robin</b> load-balancing
 * algorithm: requests are handed to registered servers in strict rotating
 * order — server 0, server 1, server 2, ..., server n-1, server 0, server 1,
 * ... — regardless of how busy each server currently is.</p>
 *
 * <h3>Where this sits among load-balancing algorithms</h3>
 * <ul>
 *   <li><b>Round robin (this class)</b> — simplest possible fairness: every
 *       server gets an equal, predictable share of requests over time.
 *       No notion of server capacity or current load.</li>
 *   <li><b>Weighted round robin</b> — same idea, but servers with more
 *       capacity get proportionally more turns (see
 *       {@link WeightedRoundRobinLoadBalancer} below, in this same file).</li>
 *   <li><b>Least connections</b> — routes to whichever server currently has
 *       the fewest active connections; needs live feedback from each
 *       server, unlike round robin which needs none.</li>
 *   <li><b>Consistent hashing</b> — routes the *same key* (client IP,
 *       session ID) to the *same server* every time, for cache locality /
 *       session stickiness; round robin makes no such guarantee — the same
 *       client can land on a different server on every request.</li>
 * </ul>
 *
 * <p>Round robin is exactly the default algorithm NGINX uses for its
 * {@code upstream} blocks when no other balancing method is specified, and
 * is one of the standard algorithms in HAProxy ({@code balance roundrobin}).
 * It works best when all servers have roughly equal capacity and requests
 * are roughly equal cost — which is why weighted round robin and least
 * connections exist for the cases where that assumption breaks down.</p>
 *
 * <h3>Complexity</h3>
 * <p>{@link #next()} is O(1) (ignoring the rare concurrent-shrink retry
 * discussed below). {@link #addServer} is O(1) amortized. {@link #removeServer}
 * is O(n) because the underlying list must shift elements — see the
 * "Why {@code CopyOnWriteArrayList}" section for why that trade-off is
 * deliberate here.</p>
 *
 * <h3>Why {@code CopyOnWriteArrayList} + {@code AtomicInteger}?</h3>
 * <p>A load balancer's server list is read constantly (every single request)
 * and written rarely (only when a server is provisioned, decommissioned, or
 * detected as unhealthy). {@link CopyOnWriteArrayList} is built for exactly
 * that read-heavy / write-rare pattern: reads ({@code get}, iteration) never
 * block and need no locking at all, because every write makes a fresh copy
 * of the entire backing array. That makes writes relatively expensive
 * (O(n) — hence {@link #removeServer}'s cost above), which is the right
 * trade for this use case: we're happy to pay more when a server joins or
 * leaves the pool in exchange for every request-routing lookup being lock-free.</p>
 *
 * <p>{@link AtomicInteger} plays the same role for the rotation cursor
 * itself: {@code getAndIncrement()} is a single lock-free CAS
 * (compare-and-swap) operation, so many threads can call {@link #next()}
 * concurrently without ever blocking each other while computing "whose turn
 * is it".</p>
 *
 * <h3>A subtle race, and how it's handled</h3>
 * <p>Because the server count can change concurrently with a call to
 * {@link #next()}, there is a small window between reading
 * {@code servers.size()} and calling {@code servers.get(index)} where a
 * server could be removed, shrinking the list. If that happens, {@code index}
 * might now point past the end of the (new, smaller) list, and {@code get}
 * would throw {@link IndexOutOfBoundsException}. Rather than let that
 * exception leak out to the caller for what is really just an ordinary,
 * harmless race, {@link #next()} catches it and retries against the
 * now-current size. This keeps the common case (no concurrent
 * add/remove) completely lock-free, while staying correct in the rare case
 * where the pool changes mid-lookup.</p>
 *
 * <h3>Integer overflow</h3>
 * <p>{@code AtomicInteger}'s counter is a 32-bit {@code int}. After roughly
 * 2.1 billion calls it wraps around from {@code Integer.MAX_VALUE} to a
 * negative number. A plain {@code i % n} on a negative {@code i} would
 * return a negative (or zero) result in Java, which is not a valid list
 * index — that's why {@link #next()} uses {@link Math#floorMod(int, int)}
 * instead of {@code %}: {@code floorMod} always returns a value in
 * {@code [0, n)} regardless of the sign of its first argument, so the
 * rotation keeps working correctly straight through the overflow.</p>
 *
 * @param <T> the type representing a server/backend/endpoint (a
 *            {@code String} host:port, a connection object, a custom
 *            {@code ServerInfo} record — whatever the caller wants to
 *            round-robin over)
 */
public class RoundRobinLoadBalancer<T> {

    /** The pool of servers currently eligible to receive requests. */
    private final CopyOnWriteArrayList<T> servers = new CopyOnWriteArrayList<>();

    /**
     * Monotonically increasing (until it wraps) counter; each call to
     * {@link #next()} claims the next value via a lock-free
     * {@code getAndIncrement()} and maps it onto a server index with
     * {@link Math#floorMod}.
     */
    private final AtomicInteger cursor = new AtomicInteger(0);

    /** Creates an empty load balancer. Servers must be added via {@link #addServer}. */
    public RoundRobinLoadBalancer() {
    }

    /**
     * Creates a load balancer pre-populated with the given servers, in
     * iteration order.
     *
     * @param initialServers servers to seed the pool with
     */
    public RoundRobinLoadBalancer(Collection<? extends T> initialServers) {
        servers.addAll(initialServers);
    }

    /**
     * Registers a new server at the end of the rotation.
     *
     * @param server the server to add; duplicates are allowed (a server can
     *               appear more than once if you want it to get a bigger
     *               share of requests without implementing full weighting)
     */
    public void addServer(T server) {
        servers.add(server);
    }

    /**
     * Removes one occurrence of {@code server} from the pool, if present.
     *
     * @param server the server to remove
     * @return {@code true} if a matching server was found and removed
     */
    public boolean removeServer(T server) {
        return servers.remove(server);
    }

    /** @return the number of servers currently registered */
    public int size() {
        return servers.size();
    }

    /** @return {@code true} if no servers are registered */
    public boolean isEmpty() {
        return servers.isEmpty();
    }

    /**
     * @return an independent snapshot list of the currently registered
     *         servers, in rotation order (safe to keep/modify — it does not
     *         reflect later changes to this load balancer)
     */
    public List<T> snapshot() {
        return new ArrayList<>(servers);
    }

    /**
     * Returns the next server in rotation, advancing the internal cursor by
     * one. Thread-safe: concurrent callers each get a distinct, correctly
     * rotating turn.
     *
     * @return the next server to route a request to
     * @throws NoSuchElementException if no servers are currently registered
     */
    public T next() {
        while (true) {
            int n = servers.size();
            if (n == 0) {
                throw new NoSuchElementException("RoundRobinLoadBalancer has no servers registered");
            }
            int i = cursor.getAndIncrement();
            int index = Math.floorMod(i, n); // see class Javadoc: safe under overflow, unlike i % n
            try {
                return servers.get(index);
            } catch (IndexOutOfBoundsException shrankConcurrently) {
                // The pool shrank between servers.size() and servers.get(index)
                // on another thread's removeServer() call. Not a real error —
                // just retry against the now-current size.
            }
        }
    }

    /**
     * Resets the rotation to start again from the first server on the next
     * call to {@link #next()}. Mainly useful for deterministic tests/demos.
     */
    public void resetRotation() {
        cursor.set(0);
    }

    @Override
    public String toString() {
        return "RoundRobinLoadBalancer" + servers;
    }

    // ------------------------------------------------------------------
    // Demo + tests
    // ------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException {
        basicRotationDemo();
        concurrentFairnessTest();
        weightedRoundRobinDemo();
    }

    private static void basicRotationDemo() {
        System.out.println("=== Basic round robin rotation ===");
        RoundRobinLoadBalancer<String> lb =
                new RoundRobinLoadBalancer<>(List.of("server-A", "server-B", "server-C"));

        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sequence.append(lb.next()).append(" ");
        }
        System.out.println(sequence.toString().trim());
        // Expect: server-A server-B server-C server-A server-B server-C server-A server-B

        lb.removeServer("server-B");
        System.out.println("After removing server-B: " + lb.next() + " " + lb.next() + " " + lb.next());
        System.out.println();
    }

    /**
     * Fires many requests from many threads at once and checks that every
     * server ends up with (almost) exactly the same number of requests —
     * proving the {@code AtomicInteger}-based rotation is correct under
     * real concurrency, not just in a single-threaded demo.
     */
    private static void concurrentFairnessTest() throws InterruptedException {
        System.out.println("=== Concurrent fairness test ===");
        int serverCount = 5;
        int threadCount = 20;
        int callsPerThread = 10_000;

        RoundRobinLoadBalancer<Integer> lb = new RoundRobinLoadBalancer<>();
        for (int i = 0; i < serverCount; i++) {
            lb.addServer(i);
        }

        AtomicInteger[] hitsPerServer = new AtomicInteger[serverCount];
        for (int i = 0; i < serverCount; i++) {
            hitsPerServer[i] = new AtomicInteger(0);
        }

        Thread[] threads = new Thread[threadCount];
        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> {
                for (int i = 0; i < callsPerThread; i++) {
                    int server = lb.next();
                    hitsPerServer[server].incrementAndGet();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int totalCalls = threadCount * callsPerThread;
        int expectedPerServer = totalCalls / serverCount;
        System.out.println("Total calls: " + totalCalls + ", expected per server: " + expectedPerServer);
        for (int i = 0; i < serverCount; i++) {
            int actual = hitsPerServer[i].get();
            System.out.println("  server " + i + ": " + actual);
            if (Math.abs(actual - expectedPerServer) > threadCount) {
                // A small slack (< one full round per thread) accounts for
                // harmless interleaving at the very end of the run; a larger
                // gap would indicate a real fairness bug in next().
                throw new AssertionError("server " + i + " got an unfair share: " + actual);
            }
        }
        System.out.println("PASS: rotation stayed fair under " + threadCount + " concurrent threads.\n");
    }

    private static void weightedRoundRobinDemo() {
        System.out.println("=== Weighted round robin (smooth, NGINX-style) ===");
        WeightedRoundRobinLoadBalancer<String> wlb = new WeightedRoundRobinLoadBalancer<>();
        wlb.addServer("A", 5);
        wlb.addServer("B", 1);
        wlb.addServer("C", 1);

        StringBuilder sequence = new StringBuilder();
        int[] counts = new int[3]; // A, B, C
        int totalPicks = 700; // 100 full weight-cycles of 5+1+1
        for (int i = 0; i < totalPicks; i++) {
            String picked = wlb.next();
            if (i < 14) {
                sequence.append(picked).append(" ");
            }
            switch (picked) {
                case "A": counts[0]++; break;
                case "B": counts[1]++; break;
                case "C": counts[2]++; break;
                default: throw new AssertionError("unexpected server: " + picked);
            }
        }
        System.out.println("First 14 picks: " + sequence.toString().trim());
        System.out.println("Counts after " + totalPicks + " picks -> A=" + counts[0] + " B=" + counts[1] + " C=" + counts[2]);

        // Weights are 5:1:1 out of 7, so over 700 picks (a multiple of 7) we
        // expect exactly 500 / 100 / 100.
        if (counts[0] != 500 || counts[1] != 100 || counts[2] != 100) {
            throw new AssertionError("weighted distribution did not match expected 5:1:1 ratio");
        }
        System.out.println("PASS: distribution matches the 5:1:1 weight ratio exactly.");
    }
}

/**
 * <h2>WeightedRoundRobinLoadBalancer</h2>
 *
 * <p>Round robin's natural extension for a pool of servers with
 * <b>unequal capacity</b>: a beefier server should receive proportionally
 * more requests than a smaller one, but — just like plain round robin — the
 * distribution should still be spread out evenly over time rather than
 * arriving in unfair bursts.</p>
 *
 * <p>This implements the exact algorithm NGINX uses for its
 * {@code weight=} parameter on upstream servers, sometimes called
 * <b>"smooth" weighted round robin</b>. A naive weighted approach — just
 * repeat server A five times in a row for every one visit to B and C — would
 * technically hit the right 5:1:1 ratio, but would send five requests to A
 * back-to-back before ever trying B or C, momentarily overloading A far more
 * than its "fair share per unit time" while B and C sit idle. The smooth
 * algorithm below spreads A's extra turns out interleaved with B and C
 * instead.</p>
 *
 * <h3>The algorithm</h3>
 * <p>Each server keeps two numbers: its fixed {@code weight} (how much of
 * the traffic it should get, relative to the others) and a mutable
 * {@code currentWeight} (a running "credit balance", starting at 0). Every
 * time a server must be picked:</p>
 * <ol>
 *   <li>Add every server's {@code weight} to its own {@code currentWeight}
 *       (everyone accrues credit, proportional to their configured weight).</li>
 *   <li>Pick whichever server now has the highest {@code currentWeight}.</li>
 *   <li>Subtract the sum of <i>all</i> weights from the winner's
 *       {@code currentWeight} (the winner "spends" its credit, guaranteeing
 *       it won't win again immediately unless its weight is large enough
 *       relative to the others that it should).</li>
 * </ol>
 *
 * <p>Worked example with weights A=5, B=1, C=1 (total=7) — watch how the
 * schedule interleaves instead of clumping A's five turns together:</p>
 * <pre>
 * pick 1: currentWeights become A=5,B=1,C=1 -> pick A -> A -= 7 -> A=-2,B=1,C=1
 * pick 2: currentWeights become A=3,B=2,C=2 -> pick A -> A -= 7 -> A=-4,B=2,C=2
 * pick 3: currentWeights become A=1,B=3,C=3 -> B and C are tied; the first
 *          one encountered while scanning the list wins ties (here, B,
 *          since it was registered before C) -> B -= 7 -> A=1,B=-4,C=3
 * pick 4: currentWeights become A=6,B=-3,C=4 -> pick A -> A -= 7 -> A=-1,B=-3,C=4
 * pick 5: currentWeights become A=4,B=-2,C=5 -> pick C -> C -= 7 -> A=4,B=-2,C=-2
 * pick 6: currentWeights become A=9,B=-1,C=-1 -> pick A -> A -= 7 -> A=2,B=-1,C=-1
 * pick 7: currentWeights become A=7,B=0,C=0 -> pick A -> A -= 7 -> A=0,B=0,C=0 (back to start)
 * </pre>
 * <p>giving the sequence {@code A A B A C A A} — five A's, one B, one C, but
 * spread across all seven slots instead of {@code A A A A A B C}. Over any
 * multiple of the total weight (7, 14, 21, ...) the exact 5:1:1 ratio holds.</p>
 *
 * <h3>Why {@code synchronized} instead of lock-free like the plain version?</h3>
 * <p>{@link RoundRobinLoadBalancer} could use a single lock-free
 * {@code AtomicInteger} because it only ever needs to update <i>one</i>
 * number atomically (the cursor). This algorithm needs to read and update
 * <i>every</i> server's {@code currentWeight} together as one atomic step —
 * two threads both doing step 1 and 2 concurrently, interleaved, could both
 * "win" or corrupt each other's bookkeeping. Rather than a fiddly
 * multi-variable compare-and-swap scheme, a single {@code synchronized}
 * method is the simple, obviously-correct choice — an intentional
 * simplicity/throughput trade-off, not an oversight.</p>
 *
 * @param <T> the type representing a server/backend/endpoint
 */
class WeightedRoundRobinLoadBalancer<T> {

    /** One server's fixed weight plus its mutable running credit balance. */
    private static final class WeightedServer<T> {
        final T server;
        final int weight;
        int currentWeight;

        WeightedServer(T server, int weight) {
            this.server = server;
            this.weight = weight;
            this.currentWeight = 0;
        }
    }

    private final List<WeightedServer<T>> servers = new ArrayList<>();
    private int totalWeight = 0;

    /**
     * Registers a server with the given weight.
     *
     * @param server the server to add
     * @param weight how much traffic it should receive relative to the
     *               others; must be positive (e.g. weight 5 means "5x the
     *               traffic of a server with weight 1")
     * @throws IllegalArgumentException if {@code weight <= 0}
     */
    public synchronized void addServer(T server, int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive, got " + weight);
        }
        servers.add(new WeightedServer<>(server, weight));
        totalWeight += weight;
    }

    /** @return the number of servers currently registered */
    public synchronized int size() {
        return servers.size();
    }

    /**
     * Returns the next server according to the smooth weighted round-robin
     * schedule described in the class Javadoc.
     *
     * @return the next server to route a request to
     * @throws NoSuchElementException if no servers are registered
     */
    public synchronized T next() {
        if (servers.isEmpty()) {
            throw new NoSuchElementException("WeightedRoundRobinLoadBalancer has no servers registered");
        }

        WeightedServer<T> chosen = null;
        for (WeightedServer<T> candidate : servers) {
            candidate.currentWeight += candidate.weight;
            if (chosen == null || candidate.currentWeight > chosen.currentWeight) {
                chosen = candidate;
            }
        }

        chosen.currentWeight -= totalWeight;
        return chosen.server;
    }
}
