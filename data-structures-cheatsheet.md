# Data Structures Cheatsheet — for System Design

The real skill tested in "design X" interviews isn't drawing boxes, it's picking the
right data structure for the bottleneck you just identified. This file lists the
data structures that keep coming up, when to reach for each, and exactly where
each one is used in *System Design Interview: An Insider's Guide* (Alex Xu), so
you can trace "why this structure" back to a concrete, worked example.

Each entry: **what it is → why you'd pick it → complexity → book example**.

---

## Hash table / hash map

**What**: key → value mapping via a hash function; O(1) average lookup/insert/delete.

**Pick it when**: you need fast point lookups by key and don't care about order.

**Complexity**: O(1) average get/put; O(n) worst case on heavy collisions.

**Where the book uses it**:
- **Ch.6 Key-Value Store** — the single-server starting point: "an intuitive approach
  is to store key-value pairs in a hash table, which keeps everything in memory."
  The rest of the chapter is essentially *what to do once the hash table doesn't fit
  on one machine* (→ consistent hashing, below).
- **Ch.8 URL Shortener** — naive design maps `shortURL → longURL` in a hash table
  before moving to a relational DB for durability.
- **Ch.9 Web Crawler** — the "URL Seen?" dedup component is backed by a hash table
  (small scale) or a Bloom filter (large scale).
- **Ch.13 Search Autocomplete** — a trie can be serialized into a hash table
  (`prefix → node data`) for storage in a key-value store.
- **Ch.5 Consistent Hashing** — the whole chapter starts from "why does resizing a
  plain hash table (`hash(key) % n`) remap almost all keys?" — the pain point hash
  tables have at scale, which consistent hashing fixes.

**Java**: `HashMap<K,V>` (no order), `LinkedHashMap<K,V>` (insertion/access order —
handy for building an LRU cache), `ConcurrentHashMap<K,V>` (thread-safe).

---

## TreeMap / sorted map (balanced BST)

**What**: a map that keeps keys sorted, with O(log n) "nearest key" queries
(`ceilingKey`, `floorKey`, `tailMap`, `headMap`).

**Pick it when**: you need range queries or "find the next/previous key" — the
hallmark case being a hash ring.

**Where the book uses it**: **Ch.5 Consistent Hashing** and **Ch.6 Key-Value Store**
— servers and virtual nodes are placed at hashed positions on a ring; a key's owner
is "the first server encountered going clockwise from the key's position." That's
literally a `ceilingKey` lookup with wraparound to `firstKey()`. (This is the
`TreeMap<Long,String>` ring we built and discussed earlier in this conversation.)

**Java**: `TreeMap<K,V>`, `TreeSet<K>`.

---

## Doubly linked list (+ hash map) → LRU cache

**What**: a doubly linked list gives O(1) removal/insertion at both ends; paired
with a hash map from key → node, you get O(1) "move this key to the front" —
the standard LRU cache implementation.

**Pick it when**: you need a fixed-capacity cache with O(1) get/put and O(1)
eviction of the least-recently-used item.

**Where the book uses it**: **Ch.1 Scale From Zero to Millions of Users** — the
caching tier section names "Least Recently Used (LRU)" as "the most popular cache
eviction policy," used throughout later chapters (news feed cache, trie cache,
video metadata cache) wherever an in-memory cache with bounded size is needed.

**Java**: `LinkedHashMap<K,V>` with `accessOrder=true` and an overridden
`removeEldestEntry` gives you LRU in ~10 lines; or hand-roll `HashMap` +
intrusive doubly linked list nodes for full control (needed if you must support
custom eviction hooks or thread-safety schemes).

---

## Trie (prefix tree)

**What**: a tree where each path from the root spells out a prefix; children are
indexed by the next character.

**Pick it when**: you need fast prefix search — autocomplete, spell-check, IP
routing tables, dictionary lookups. A relational DB doing `LIKE 'prefix%'` across
billions of rows doesn't scale; a trie turns prefix lookup into O(L) where L is
the prefix length.

**Complexity**: O(L) search/insert (L = string length), independent of how many
strings are stored.

**Where the book uses it**: **Ch.13 Search Autocomplete System** is built entirely
around this structure: "fetching the top 5 search queries from a relational
database is inefficient. The data structure trie... is used to overcome the
problem." The chapter's key optimization — caching the **top-k results at each
node** so you don't have to traverse the whole subtree — is the single most
important trie trick to know for interviews. The trie is rebuilt weekly by
offline workers and cached in a distributed cache ("Trie Cache") for fast reads,
and can be serialized into a hash table (prefix → node data) for persistence.

**Java**: no built-in trie — implement with `TrieNode { Map<Character,TrieNode> children; boolean isEnd; /* extra: top-k list, frequency */ }`,
or use a `HashMap<Character,TrieNode>` per node (simpler) vs. a fixed `TrieNode[26]`
array (faster, English-only).

---

## Bloom filter

**What**: a probabilistic set membership structure — a bit array + k hash
functions. Answers "definitely not present" or "maybe present" (false positives
possible, false negatives impossible). Cannot store or retrieve the actual values.

**Pick it when**: you need to cheaply rule out "have I seen this before?" across
a huge set, and an occasional false positive is acceptable if it just triggers a
slower fallback check (e.g., hit the real DB).

**Where the book uses it**:
- **Ch.9 Web Crawler** — the "URL Seen?" dedup filter at scale: "Bloom filter and
  hash table are common techniques to implement the 'URL Seen?' component."
- **Ch.8 URL Shortener** — checking whether a generated short URL already exists
  before hitting the database, to avoid an expensive query on every request.
- **Ch.6 Key-Value Store** — inside each SSTable-based storage engine: "If data
  is not in memory, the system checks the bloom filter... to figure out which
  SSTables might contain the key" before touching disk.

**Java**: no JDK built-in — Guava's `BloomFilter<T>` is the standard choice
(`com.google.common.hash.BloomFilter`), or roll your own with a `BitSet` +
several `MessageDigest`/`MurmurHash` seeds.

---

## Merkle tree (hash tree)

**What**: a tree where every non-leaf node's hash is derived from its children's
hashes; the root hash summarizes the entire dataset.

**Pick it when**: you need to detect *where* two large replicas differ without
transferring or comparing all the data — only mismatched hash branches need to
be walked.

**Where the book uses it**: **Ch.6 Key-Value Store**, anti-entropy / replica
sync: "Using Merkle trees, the amount of data needed to be synchronized is
proportional to the differences between the two replicas, and not the amount of
data they contain." Compare root hashes first; if they differ, recurse into
children to isolate the out-of-sync buckets.

**Java**: no JDK built-in — build a binary tree of `MessageDigest` (SHA-256)
hashes bottom-up over fixed-size key ranges ("buckets").

---

## Priority queue (binary heap)

**What**: a queue where the element with the highest (or lowest) priority is
always dequeued first; typically backed by a binary heap. O(log n) insert/remove,
O(1) peek.

**Pick it when**: you need "give me the most urgent/important item next," not
strict arrival order — schedulers, top-k problems, Dijkstra/A*, merge-k-lists.

**Where the book uses it**: **Ch.14 Design YouTube**, the encoding pipeline's
resource manager runs on two priority queues: a **task queue** ("contains tasks
to be executed," picked by priority) and a **worker queue** ("contains worker
utilization info," used to find the optimal free worker) — plus a running queue
that isn't priority-ordered. The task scheduler is just repeated "pop the top
of each queue."

**Java**: `PriorityQueue<T>` (min-heap by default; pass a `Comparator` for
max-heap or custom ordering).

---

## FIFO queue (and message queues)

**What**: strict first-in-first-out ordering. A **message queue** is the
distributed-systems version: a durable, asynchronous buffer between producers
and consumers, decoupling the two.

**Pick it when**: you need to decouple a fast/unreliable producer from a slower
consumer, buffer bursts of work, or process jobs asynchronously and in order.

**Where the book uses it** (this is the single most repeated pattern in the
book):
- **Ch.1** — introduces the message queue as a core scaling building block.
- **Ch.4 Rate Limiter** — the leaky bucket algorithm is "usually implemented
  with a first-in-first-out (FIFO) queue," processing requests at a fixed rate.
- **Ch.9 Web Crawler** — a FIFO URL frontier queue feeds worker threads (plus a
  priority mechanism for politeness/freshness).
- **Ch.10 Notification System** — every notification type gets its own message
  queue so one third-party outage doesn't block the others; workers pull from
  the queue and retry failures by re-enqueuing.
- **Ch.11 News Feed** — the fanout service pushes `(friend list, post ID)` onto
  a message queue so fanout-on-write happens asynchronously.
- **Ch.14 YouTube** — a "completion queue" decouples the transcoding pipeline
  stages so encoding doesn't block on download.

**Java**: `java.util.Queue`/`Deque` (`ArrayDeque`) for in-process queues;
`BlockingQueue` (`LinkedBlockingQueue`, `ArrayBlockingQueue`) for
producer/consumer threads. For the distributed message-queue role itself, that's
an external system (Kafka, SQS, RabbitMQ), not a JDK collection — same *role* as
`Queue`, but the durability/fan-out guarantees come from the broker, not the
data structure.

---

## Sorted set

**What**: a set that keeps its members ordered by an associated score — O(log n)
insert/remove, O(log n) range queries by score.

**Pick it when**: you need both "is X a member" *and* "give me everything in
this score/time range," e.g., a sliding time window.

**Where the book uses it**: **Ch.4 Rate Limiter**, sliding window log algorithm —
"Timestamp data is usually kept in cache, such as sorted sets of Redis," letting
you prune request timestamps older than the window in O(log n) and count what's
left. Also used to avoid race conditions on the counter (an alternative to
locks/Lua scripts).

**Java**: no direct JDK equivalent for the Redis "sorted set" (a data
structure external to your app, held in Redis) — inside the JVM the closest
analogue is a `TreeMap<Score, Set<Member>>` or `ConcurrentSkipListMap`, but in
practice this one lives in your cache layer (Redis `ZADD`/`ZRANGEBYSCORE`), not
in application code.

---

## Rate limiter — data structure by algorithm

**Ch.4 Design a Rate Limiter** covers five algorithms; each one is really just
a thin layer of logic wrapped around one specific, small data structure.
Collected here in one place for quick reference (each structure is described
in more depth in its own section above).

| Algorithm | Data structure | Why |
|---|---|---|
| **Token bucket** | Counter + timestamp per client — `{tokens, lastRefillTime}` | No collection needed — refill is arithmetic (`elapsed × refillRate`, capped at bucket size) done on read. One struct per client (`HashMap<ClientId, Bucket>` in-memory, or a Redis key with both fields updated atomically via Lua script). |
| **Leaking bucket** | Bounded FIFO queue | Requests are enqueued and drained at a fixed rate by a separate process — that *is* the algorithm. A capacity-limited `ArrayBlockingQueue`; full queue = drop the request. |
| **Fixed window counter** | Counter keyed by (client, window) | Simplest of the five: `Map<String, Integer>` keyed by client + current window ID (e.g. `user123:2026091314`). Redis: `INCR` + `EXPIRE` on that key. |
| **Sliding window log** | Sorted structure ordered by timestamp (see **Sorted set**, above) | Needs every individual request timestamp for a client so old ones can be pruned and the rest counted — a `TreeMap<Long,...>`/`Deque<Long>` in-memory, or Redis sorted set (`ZADD` / `ZREMRANGEBYSCORE` / `ZCARD`) in practice. O(k) space per client (one entry per request in the window) in exchange for perfect accuracy. |
| **Sliding window counter** | Two counters — current window + previous window | Avoids storing every timestamp by keeping `{previousWindowCount, currentWindowCount}` and computing a weighted estimate (`current + previous × overlap fraction`). Same shape as the fixed-window counter, just two integers instead of one — O(1) space, approximate accuracy. |

**The trade-off across all five**: precision costs memory. Token/leaking
bucket and fixed window are O(1) space per client; sliding window log is
O(k) (k = requests in the window) for exact accuracy; sliding window counter
splits the difference at O(1) space with an approximation.

**A distributed-systems catch worth flagging**: this cheatsheet's [round
robin load balancer](RoundRobinLoadBalancer.java) note applies in reverse
here. A load balancer's replicas don't need to share state — fairness is
statistical, so each replica running its own independent rotation is fine.
A rate limiter's replicas **do** need to share state: if each of your N app
servers keeps its counter/queue/sorted-set in local memory, each one
enforces the limit independently, so a client can get up to Nx the intended
quota by spreading requests across replicas. That's why all five algorithms
above are normally implemented against a centralized store (Redis) reachable
by every app replica, not in each replica's local memory.

---

## Consistent hash ring (built on a sorted structure)

**What**: covered in depth earlier in this conversation — nodes and keys hashed
onto a circle; a key belongs to the nearest node clockwise; only ~k/n keys move
when a node joins/leaves, instead of nearly all of them with plain `hash % n`.

**Pick it when**: you're sharding data or requests across a *changing* set of
nodes (servers scale up/down, cache nodes fail) and can't afford a near-total
remap on every membership change.

**Where the book uses it** — this is the most cross-referenced technique in the
whole book:
- **Ch.5 Design Consistent Hashing** — the dedicated chapter; introduces virtual
  nodes to fix uneven partition sizes, and lists real systems that use it
  (partitioning components of Dynamo, Cassandra, and content delivery networks).
- **Ch.1** — foreshadowed under "resharding data" as the fix for shard
  exhaustion / uneven distribution.
- **Ch.6 Key-Value Store** — used to decide both **data partitioning** (which
  server owns a key) and **replication** (walk clockwise, take the next N
  distinct servers as replicas).
- **Ch.9 Web Crawler** — distributes URLs across downloader servers so adding/
  removing a downloader doesn't reshuffle everything.
- **Ch.11 News Feed** — mitigates the "hotkey" problem (a celebrity's data
  getting disproportionate traffic) by spreading load more evenly.

**Java**: as discussed earlier, a `TreeMap<Long,String>` ring of virtual-node
hashes plus a `HashMap<String,T>` from physical node ID to the real
address/value.

---

## Vector clock

**What**: a `[server, version-counter]` pair per replica, attached to a data
item, used to determine whether one version *causally precedes*, *succeeds*, or
*conflicts with* another — without a global clock.

**Pick it when**: you replicate writes across multiple nodes without strict
consensus and need to detect (not necessarily auto-resolve) concurrent
conflicting writes.

**Where the book uses it**: **Ch.6 Key-Value Store**, conflict resolution —
walks through exactly how `D1([Sx,1])` → `D2([Sx,2])` → conflicting
`D3([Sx,2],[Sy,1])` branches are detected, and notes the practical fix for
unbounded growth (cap the pair list length).

**Java**: model as `Map<String,Integer>` (server ID → counter) with a
compare method implementing the ancestor/sibling rules above; not something the
JDK provides directly.

---

## Bitmap / bit array

**What**: an array of bits, one per possible value, for extremely compact
membership or flag tracking. (The backbone of a Bloom filter, and useful on its
own for things like "which of these 10M user IDs did X".)

**Pick it when**: you're tracking presence/absence over a huge, dense ID space
and a full hash table would waste memory.

**Where it shows up conceptually**: underlies the Bloom filter implementations
in Ch.6, Ch.8, and Ch.9 above — worth knowing as the primitive, not just "Bloom
filter" as a black box.

**Java**: `java.util.BitSet`.

---

## Arrays / chunking (fixed-size blocks)

**What**: not a fancy structure — just splitting a large object into fixed-size
pieces so each piece can be transferred, retried, versioned, or deduplicated
independently.

**Pick it when**: files are too large to move/process atomically, or you want
resumable uploads and cheap diffing (only changed chunks need re-syncing).

**Where the book uses it**:
- **Ch.14 YouTube** — splits video into GOP (Group of Pictures) chunks so each
  chunk is "an independently playable unit" that can be encoded/uploaded in
  parallel.
- **Ch.15 Google Drive** — "Block servers chunk the files into blocks, compress,
  encrypt the blocks" before upload; only modified blocks are re-synced on edit,
  and chunking logic is centralized in block servers rather than duplicated
  across iOS/Android/Web clients.

**Java**: plain arrays / `byte[]` slices, or `RandomAccessFile`/`FileChannel`
for chunked reads of large files.

---

## Graph (and graph databases)

**What**: nodes + edges; a directed graph adds direction to edges. Traversed via
DFS (deep, stack/recursion-based) or BFS (level-by-level, queue-based). A
**directed acyclic graph (DAG)** additionally forbids cycles, guaranteeing a
valid execution/topological order.

**Pick it when**: your data is fundamentally about relationships/connections
(who links to whom, who's friends with whom), or you need to model a pipeline
of dependent stages that must run in a valid order with room for parallelism.

**Where the book uses it**:
- **Ch.9 Web Crawler** — models the web itself as a directed graph (pages =
  nodes, hyperlinks = edges); crawling is graph traversal. The chapter picks
  **BFS over DFS** specifically because DFS can recurse arbitrarily deep, and
  implements the BFS frontier as the FIFO queue described above — then has to
  patch BFS's "politeness" problem (most links point back to the same host,
  flooding it) with per-host queues.
- **Ch.11 News Feed** — friend/follow relationships are stored in a **graph
  database** ("Social Graph... stores user relationship data"; fetching friend
  IDs is called out as a graph-database-shaped query, better suited to it than
  a relational join table at scale).
- **Ch.14 Design YouTube** — the transcoding pipeline uses a **DAG** (modeled
  on Facebook's streaming video engine) so processing stages (watermark,
  thumbnail, encode at multiple resolutions) can run sequentially where
  required and in parallel everywhere else, without needing one rigid
  hardcoded pipeline per use case.

**Java**: no JDK built-in graph type — model as `Map<Node, List<Node>>`
(adjacency list, the common choice) or `boolean[][]`/`int[][]` adjacency
matrix for dense small graphs; traverse with `ArrayDeque` as the BFS queue or
plain recursion for DFS. For a DAG scheduler, track in-degree per node and
process with a topological sort (Kahn's algorithm, itself BFS-based).

---

## Quick picker — symptom → structure

| If your bottleneck is... | Reach for... | Book chapter |
|---|---|---|
| Fast key→value lookup, single node | Hash table | Ch.6, Ch.8 |
| Cache with bounded size + eviction | Doubly linked list + hash map (LRU) | Ch.1 |
| Prefix search / autocomplete | Trie | Ch.13 |
| "Have I seen this before?" at huge scale | Bloom filter | Ch.6, Ch.8, Ch.9 |
| Detecting where two replicas diverge | Merkle tree | Ch.6 |
| Sharding across nodes that join/leave | Consistent hash ring (sorted map) | Ch.5, Ch.6, Ch.9, Ch.11 |
| Resolving conflicting concurrent writes | Vector clock | Ch.6 |
| "Most urgent job next" scheduling | Priority queue / heap | Ch.14 |
| Decoupling producer/consumer, async work | FIFO / message queue | Ch.1, Ch.4, Ch.9, Ch.10, Ch.11, Ch.14 |
| Rate limiting (see full per-algorithm table above) | Counter / FIFO queue / sorted set, per algorithm | Ch.4 |
| Compact large-scale membership tracking | Bitmap | Ch.6, Ch.8, Ch.9 (via Bloom filter) |
| Large file transfer/versioning | Fixed-size chunks/blocks | Ch.14, Ch.15 |
| Modeling relationships (friends, links) | Graph / graph database | Ch.9, Ch.11 |
| Pipeline of dependent stages, parallelizable | DAG | Ch.14 |

---

*Source: data structure usages extracted from Alex Xu's* System Design Interview:
An Insider's Guide *(chapters 1, 4–15). Java type suggestions are mine, added
for a Java-focused cheatsheet — the book itself is language-agnostic.*
