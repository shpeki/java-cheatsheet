import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * A minimal, dependency-free consistent hashing ring.
 *
 * Idea:
 *  - Hash both nodes (servers/shards) and keys (requests/cache keys) onto the
 *    same circular numeric space (0 .. Long.MAX_VALUE here, using the first
 *    8 bytes of an MD5 digest as the hash).
 *  - A key belongs to the first node whose hash is >= the key's hash, walking
 *    clockwise around the circle (wrapping back to the smallest node if we
 *    fall off the end).
 *  - Because the mapping only depends on relative position on the ring, adding
 *    or removing one node only reshuffles the keys that were adjacent to it —
 *    roughly K/N keys out of K total, instead of nearly all of them (which is
 *    what happens with plain "hash(key) % N").
 *  - Each physical node is placed at several "virtual node" positions on the
 *    ring so that load is spread evenly even with a small number of real
 *    nodes, and so one physical node's share of the ring isn't one big
 *    lopsided arc.
 */
public class ConsistentHashRing<T> {

    // Ring: hash position -> virtual node label (encodes which physical node it belongs to)
    private final SortedMap<Long, String> ring = new TreeMap<>();
    // physical node -> its underlying value (e.g. server address)
    private final Map<String, T> nodes = new HashMap<>();
    private final int virtualNodesPerNode;

    public ConsistentHashRing(int virtualNodesPerNode) {
        if (virtualNodesPerNode < 1) {
            throw new IllegalArgumentException("virtualNodesPerNode must be >= 1");
        }
        this.virtualNodesPerNode = virtualNodesPerNode;
    }

    /** Adds a physical node, spreading it across the ring as several virtual nodes. */
    public void addNode(String nodeId, T value) {
        nodes.put(nodeId, value);
        for (int i = 0; i < virtualNodesPerNode; i++) {
            String virtualNodeKey = nodeId + "#VN" + i;
            ring.put(hash(virtualNodeKey), virtualNodeKey);
        }
    }

    /** Removes a physical node and all of its virtual positions. */
    public void removeNode(String nodeId) {
        nodes.remove(nodeId);
        for (int i = 0; i < virtualNodesPerNode; i++) {
            ring.remove(hash(nodeId + "#VN" + i));
        }
    }

    /** Returns the physical node responsible for the given key. */
    public T get(String key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("Ring has no nodes");
        }
        long hash = hash(key);
        // tailMap gives everything from `hash` upward; the first entry is the
        // nearest node walking clockwise. If nothing is >= hash, wrap around
        // to the very first node on the ring.
        SortedMap<Long, String> tail = ring.tailMap(hash);
        String virtualNodeKey = tail.isEmpty() ? ring.get(ring.firstKey()) : tail.get(tail.firstKey());
        String nodeId = physicalNodeOf(virtualNodeKey);
        return nodes.get(nodeId);
    }

    private String physicalNodeOf(String virtualNodeKey) {
        return virtualNodeKey.substring(0, virtualNodeKey.indexOf("#VN"));
    }

    /** MD5-based hash, using the first 8 bytes of the digest as a signed long. */
    private long hash(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(input.getBytes("UTF-8"));
            long h = 0;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xFF);
            }
            return h;
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // ---- Demo ----
    public static void main(String[] args) {
        ConsistentHashRing<String> ring = new ConsistentHashRing<>(100); // 100 virtual nodes each

        ring.addNode("cache-A", "10.0.0.1:6379");
        ring.addNode("cache-B", "10.0.0.2:6379");
        ring.addNode("cache-C", "10.0.0.3:6379");

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            keys.add("user:" + i);
        }

        System.out.println("--- Initial placement (3 nodes) ---");
        Map<String, String> before = new HashMap<>();
        for (String key : keys) {
            String node = ring.get(key);
            before.put(key, node);
            System.out.println(key + " -> " + node);
        }

        // Add a 4th node and see how few keys actually move.
        ring.addNode("cache-D", "10.0.0.4:6379");

        System.out.println("\n--- After adding cache-D ---");
        int moved = 0;
        for (String key : keys) {
            String node = ring.get(key);
            boolean changed = !node.equals(before.get(key));
            if (changed) moved++;
            System.out.println(key + " -> " + node + (changed ? "   (moved)" : ""));
        }
        System.out.println("\nKeys remapped: " + moved + " / " + keys.size()
                + " (a naive hash % N would remap almost all of them)");

        // Simple load distribution check across many keys.
        System.out.println("\n--- Load distribution over 100,000 keys ---");
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 100_000; i++) {
            String node = ring.get("object-" + i);
            counts.merge(node, 1, Integer::sum);
        }
        counts.forEach((node, count) -> System.out.println(node + ": " + count));
    }
}