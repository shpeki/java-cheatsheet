# Study Plan — Senior/Staff Java Interview Prep (Conga first)

Built 2026-10-02 from (a) an inventory of every topic in this repo, (b) the Conga job ad and `conga-gaps.md`, and (c) public Java/Spring Boot study plans (sources at the end).

## 0. Assumptions (change them if wrong)

| Assumption | Why | If wrong |
|---|---|---|
| **No interview date is known.** The plan is **28 days** (4 weeks), Day 1 = whenever you start. | None of the files give a date. | Use the 14-day and 3-day cuts in §7. |
| **Target = Conga Staff Software Engineer (Java), Sofia.** Other companies in `autumn-2026/` reuse the same core blocks. | You asked about this job last. | Swap the "Conga stack" week for that company's file. |
| **2–3 h on weekdays, 4–5 h on weekend days.** | Mirrors the daily split used by the 12-week plan I found. | Scale each day's blocks, keep the order. |
| **Spring Boot is in the plan because you asked, but the Conga ad lists Dropwizard (as a plus), not Spring.** | The ad's stack: Java, Cassandra, Redis, Azure Service Bus, AKS, Helm, OpenSearch. | Spring Boot stays P1, and Week 3 shrinks to Dropwizard concepts + Spring ideas that carry over. |

**How to use it:** each day has a tick-box checklist. A day is done only when its **Output** exists (a written answer, working code, or a spoken explanation). Reading alone does not count. The 12-week plan I found says the goal is "I can confidently explain, implement, design, debug and defend" each topic, not "I finished the syllabus".

**Priority tiers** (borrowed from that plan): **P0** = must be able to explain *and* code/design it cold (target ≥85% confidence). **P1** = solid working knowledge (≥75%). **P2** = awareness only.

---

## 1. Topic inventory: everything found in the folder, plus what is missing

Status: ✅ **Covered** (a dedicated section exists) · 🟡 **Partial** (mentions or only part) · ❌ **Missing** (not in the repo; you will write it, see §8).

### 1.1 Core Java
| Topic | Where in the repo | Status | Tier |
|---|---|---|---|
| OOP (encapsulation, inheritance, polymorphism, abstraction), interface vs abstract class, composition vs inheritance | `README.md` (OOP, interfaces), `Questions.md`, `short-cheat-sheet.md` | ✅ | P0 |
| SOLID | `README.md` (twice, with examples), `short-cheat-sheet.md` | ✅ | P0 |
| Design patterns (creational/structural/behavioral) | `java-design-patterns.md`, `additional-java-topics.md` | ✅ | P1 |
| Pass-by-value, static vs dynamic polymorphism, marker interfaces, nested classes, access modifiers | `Questions.md`, `README.md` | ✅ | P1 |
| `equals()` / `hashCode()` contract | `README.md` | ✅ | P0 |
| String pool, immutability | `README.md` | ✅ | P1 |
| Exceptions (checked/unchecked, try-with-resources, custom, best practices) | `README.md` | ✅ | P0 |
| Collections: HashMap internals, HashMap vs TreeMap, List vs Set, concurrent vs synchronized, `ConcurrentHashMap` | `README.md`, `Questions.md`, `short-cheat-sheet.md` | ✅ | P0 |
| Generics (erasure, bounded types, wildcards, PECS) | `additional-java-topics.md` | ✅ | P1 |
| Streams (intermediate/terminal, parallel), lambdas, functional interfaces, higher-order functions | `README.md`, `additional-java-topics.md` | ✅ | P0 |
| Reflection | `additional-java-topics.md` | ✅ | P2 |
| Java 8–17 features (records, sealed, text blocks…) | `additional-java-topics.md` | ✅ | P1 |
| **Java 21** (virtual threads, sequenced collections, pattern matching, generational ZGC) | `conga-gaps.md` §7.1 / §7.1.1 | ✅ | P0 for Conga |
| Testing: JUnit 5, Mockito, TDD, test doubles | `additional-java-topics.md`, `short-cheat-sheet.md` | ✅ | P0 |
| BDD / Cucumber, Testcontainers, contract tests | `conga-gaps.md` §7.5 (outline only) | 🟡 | P1 |

### 1.2 Concurrency and JVM
| Topic | Where | Status | Tier |
|---|---|---|---|
| Java Memory Model, happens-before, `volatile`, atomics | `README.md`, `additional-java-topics.md` | ✅ | P0 |
| `synchronized`, locks, `ReadWriteLock`, critical sections, deadlock/livelock | `README.md` | ✅ | P0 |
| Concurrency vs parallelism | `README.md` | ✅ | P0 |
| **`ExecutorService`, thread pools, `CompletableFuture`** | Mentions in `README.md`, `real-time-communications.md`; no dedicated section | 🟡 | **P0** |
| `StampedLock`, `Semaphore`, latches, barriers, `BlockingQueue` producer–consumer | none | ❌ | P0 |
| JVM architecture, memory areas, class loading, JIT | `README.md`, `short-cheat-sheet.md` | ✅ | P0 |
| Garbage collection (concepts), memory leaks, OOM | `short-cheat-sheet.md`, `README.md` | ✅ | P1 |
| GC tuning in practice, reading GC logs, heap dumps, JFR/profilers | none | ❌ | P1 |

### 1.3 Spring / Spring Boot
| Topic | Where | Status | Tier |
|---|---|---|---|
| IoC, DI (constructor/setter/field), bean scopes, bean lifecycle, resolving ambiguous beans | `README.md` (Spring and Microservices), `short-cheat-sheet.md` | ✅ | P0 |
| Spring Boot for microservices (overview), Spring Cloud, Eureka/service discovery | `README.md`, `short-cheat-sheet.md` | 🟡 | P1 |
| SSE / WebSocket in Spring, `@RestController` examples | `real-time-communications.md` | 🟡 | P2 |
| **Auto-configuration, starters, `@ConditionalOn…`, configuration properties, profiles** | none | ❌ | P1 |
| **Spring MVC request flow (`DispatcherServlet`, filters vs interceptors), validation, `@ControllerAdvice` error handling** | none | ❌ | P1 |
| **AOP and proxies (JDK vs CGLIB), why `@Transactional` self-invocation fails** | none | ❌ | P1 |
| **`@Transactional` (propagation, isolation, rollback rules)** | Only used inside other docs; isolation levels covered in `short-cheat-sheet.md` / `Questions.md` (PostgreSQL) | 🟡 | P0 |
| **Spring Data JPA / Hibernate: entity states, lazy vs eager, N+1, `JOIN FETCH`/entity graphs, optimistic locking** | Optimistic/pessimistic locking in `README.md`; N+1 and JPA only mentioned | 🟡 | P0 |
| **Spring Security: filter chain, JWT, OAuth2/OIDC, method security** | JWT/OAuth mentioned in company docs only | ❌ | P1 |
| **Actuator + Micrometer, health probes for K8s** | none | ❌ | P1 |
| **Spring testing slices (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`), Testcontainers** | none | ❌ | P1 |
| **Caching abstraction, `@Async`, scheduling** | none | ❌ | P2 |
| **Resilience4j (retry, circuit breaker, bulkhead)** | Mentioned in `README.md`, `createfuture-Payment Gateway.md` | 🟡 | P1 |
| **Spring Data Redis / Kafka / Service Bus integration** | none | ❌ | P1 |
| **Dropwizard** (the framework the Conga ad names) | `conga-gaps.md` §7.2 | ✅ | P1 for Conga |
| WebFlux / reactive | Mentions only | 🟡 | P2 |

### 1.4 Data, messaging, search
| Topic | Where | Status | Tier |
|---|---|---|---|
| SQL basics, joins, GROUP BY, HAVING, clauses | `PO.md`, `accenture-algo-theory-prep*.md` | ✅ | P0 |
| Indexes (B-tree, composite, covering), ACID, locking, isolation levels | `README.md`, `short-cheat-sheet.md`, `Questions.md` | ✅ | P0 |
| PostgreSQL scaling in Kubernetes | `Questions.md` | ✅ | P2 |
| Query plans (`EXPLAIN`), slow-query diagnosis | Mentioned in `payhawk.md` (API optimisation) | 🟡 | P1 |
| **Cassandra** | `conga-gaps.md` §1 (+ `data-structures-cheatsheet.md` mention) | ✅ | **P0 for Conga** |
| **Redis (caching, eviction, cluster, Streams)** | `conga-gaps.md` §2, `data-structures-cheatsheet.md` | ✅ | **P0 for Conga** |
| **Azure Service Bus** | `conga-gaps.md` §3 | ✅ | **P0 for Conga** |
| Kafka | `createfuture-*.md`, `README.md` mention | 🟡 | P1 |
| **OpenSearch / Elasticsearch** | `conga-gaps.md` §5 (concept outline, ⚠️ unverified) | 🟡 | P1 |

### 1.5 Architecture and system design
| Topic | Where | Status | Tier |
|---|---|---|---|
| Monolith vs microservices, microservice patterns (core, data, communication, deployment, security, observability) | `README.md`, `short-cheat-sheet.md` | ✅ | P0 |
| Stateless vs stateful | `stateful-vs-stateless.md` | ✅ | P1 |
| Load balancing | `load-balancing.md`, `RoundRobinLoadBalancer.java` | ✅ | P1 |
| Data structures for system design (LRU, trie, bloom, rate limiter, consistent hashing, vector clock…) | `data-structures-cheatsheet.md`, `src/ConsistentHashRing.java`, `src/RedBlackTreeMap.java` | ✅ | P0 |
| Real-time communication (SSE, WebSockets, polling) | `real-time-communications.md` | ✅ | P1 |
| Payment/third-party integration, idempotency, webhooks, retries | `createfuture-Payment Gateway.md` | ✅ | P0 |
| System-design interview method + per-company prompts | `createfuture-System-Design-Live-Coding.md`, `officernd.md`, `payhawk.md`, `conga-gaps.md` §9 | ✅ | P0 |
| **Event-driven patterns (outbox, saga, CQRS, idempotent consumer)** | `conga-gaps.md` §6 | ✅ | **P0 for Conga** |
| Observability (logs, metrics, tracing, SLOs) | One-line mention in `conga-gaps.md` §6; `README.md` monitoring patterns | 🟡 | P1 |

### 1.6 Cloud, containers, delivery
| Topic | Where | Status | Tier |
|---|---|---|---|
| Docker basics | Mentions in `README.md`, `accenture-*` | 🟡 | P1 |
| **Kubernetes, AKS (Workload Identity), KEDA, Helm** | `conga-gaps.md` §4 | ✅ | P1 for Conga |
| **CI/CD: Jenkins, GitHub Actions** | `conga-gaps.md` §4.6 (⚠️ general knowledge) | 🟡 | P1 |
| Git workflow, Maven/Gradle | none | ❌ | P2 |

### 1.7 Algorithms and coding rounds
| Topic | Where | Status | Tier |
|---|---|---|---|
| Two pointers, sliding window, fast/slow, prefix sum, monotonic deque, hash maps, backtracking, DFS/BFS, binary search on answer | `algorithmic-patterns.md`, `binary-search.md` | ✅ | P0 |
| Big-O analysis | `algorithmic-patterns.md` | ✅ | P0 |
| Boyer-Moore, sweep line, Kadane, palindromes | `accenture-algo-theory-prep*.md` | ✅ | P1 |
| LRU cache, rate limiter, top-K | `data-structures-cheatsheet.md`, `payhawk.md` | ✅ | P0 |
| Trees: BST/red-black | `src/RedBlackTreeMap.java` | 🟡 | P1 |
| **Tree DFS/BFS drills (level order, LCA, validate BST), classic DP (LCS, knapsack), anagram grouping, merge/quick sort** | none | ❌ | P1 |
| **OO design exercise (tic-tac-toe / parking lot)** | none | ❌ | P1 |

### 1.8 Soft skills / leadership
| Topic | Where | Status | Tier |
|---|---|---|---|
| Behavioural/leadership prep (EM angle) | `Tide.md` | 🟡 | P1 |
| **Staff-level STAR stories (architecture influence, mentoring, incident, reversal, disagreement)** | outline in `conga-gaps.md` §8 | 🟡 | **P0 for a Staff role** |
| Company research (Conga product/domain) | none | ❌ | P1 |

---

## 2. Weekly map

| Week | Theme | Focus | New files you create |
|---|---|---|---|
| **1** | Core Java, concurrency, JVM, Java 21 | Review what exists, close concurrency gaps | `concurrency-drills.md` |
| **2** | The Conga stack | Cassandra, Redis, Service Bus, event-driven patterns | (notes go into `conga-gaps.md`) |
| **3** | Spring Boot, persistence, microservices, cloud | Fill the Spring/JPA gap, K8s/Helm/CI-CD, OpenSearch | `spring-boot-cheatsheet.md`, `jpa-hibernate-cheatsheet.md` |
| **4** | Practice and polish | System design reps, coding drills, behavioural, mocks | `star-stories.md`, `conga-company-notes.md` |

**Daily split on weekdays (~2.5 h):** 60 min new topic · 30 min coding/DSA · 30 min interview questions out loud · 20–30 min spaced revision of yesterday and last week. **Weekend days (4–5 h):** 90 min topic · 60 min coding · 60 min system design · 45 min mock/questions.

---

## 3. Week 1 — Core Java, concurrency, JVM, Java 21

**Goal:** every P0 Java topic can be explained in under 2 minutes without notes. Concurrency drills are coded unaided.

### Day 1 — OOP and SOLID (P0)
- [ ] Read `README.md` "SOLID Principles", "OOP Principles", `short-cheat-sheet.md` (SOLID, OOP).
- [ ] Do: for each SOLID letter, give a 30-second spoken example from your own code.
- [ ] Code: 30 min, tree DFS (recursive + iterative) from scratch.
- **Output:** 5 spoken answers recorded or written (one per SOLID letter).

### Day 2 — Collections and `equals`/`hashCode` (P0)
- [ ] Read `README.md` HashMap vs TreeMap, `ConcurrentHashMap`, Lists vs Sets, `equals()`/`hashCode()`, `short-cheat-sheet.md` (HashMap collisions).
- [ ] Do: draw HashMap put/get including resize and treeification from memory.
- [ ] Code: anagram grouping with `Map<String, List<String>>`; LRU cache with `LinkedHashMap`, then with a doubly linked list + map (`data-structures-cheatsheet.md`).
- **Output:** working LRU + a one-page HashMap internals sketch.

### Day 3 — Streams, lambdas, generics (P0/P1)
- [ ] Read `README.md` Streams (intermediate vs terminal, parallel), Higher-Order Functions; `additional-java-topics.md` Generics.
- [ ] Do: 8 stream exercises (group, partition, flatMap, reduce, `Collectors.toMap` merge function, parallel-stream pitfalls).
- [ ] Explain aloud: type erasure, PECS.
- **Output:** a file of 8 solved stream problems.

### Day 4 — Exceptions, testing basics (P0)
- [ ] Read `README.md` Exception Handling; `additional-java-topics.md` Testing (JUnit 5, Mockito, TDD, test doubles).
- [ ] Do: write tests (JUnit 5 + Mockito) for the `RoundRobinLoadBalancer.java` class in the repo.
- **Output:** a passing test class; explain mock vs stub vs spy.

### Day 5 — JMM, `synchronized`, locks, atomics (P0)
- [ ] Read `README.md` Synchronization, Critical Section, Volatile vs AtomicInteger; `additional-java-topics.md` Java Memory Model.
- [ ] Do: explain happens-before with 3 concrete examples. Reproduce a race condition, then fix it three ways (`synchronized`, `ReentrantLock`, `AtomicInteger`).
- **Output:** a runnable demo of the race and the three fixes.

### Day 6 (weekend) — Executors, `CompletableFuture`, coordination (P0, ❌ in repo)
- [ ] Read: Java docs for `ExecutorService`/`ThreadPoolExecutor` (core/max/queue/rejection) and `CompletableFuture`. Write notes in `concurrency-drills.md`.
- [ ] Code (unaided): producer–consumer with `BlockingQueue`; a `CompletableFuture` pipeline that fans out 3 calls, combines results, handles a failure and a timeout; a `Semaphore`-limited task runner.
- [ ] Explain: how to size a pool for CPU-bound vs I/O-bound work; why `CallerRunsPolicy` gives back-pressure.
- **Output:** `concurrency-drills.md` with 3 working programs.

### Day 7 (weekend) — JVM, GC, virtual threads (P0)
- [ ] Read `README.md` JVM Architecture; `short-cheat-sheet.md` JVM Memory Areas, Garbage Collection; `conga-gaps.md` §7.1 and §7.1.1.
- [ ] Do: run the virtual-threads mini-drill at the end of §7.1.1 (fixed pool of 200 vs `newVirtualThreadPerTaskExecutor()`, then add a `Semaphore`).
- [ ] Do: run a small program with `-Xlog:gc` and read the output; capture a heap dump and open it (VisualVM or MAT).
- **Output:** timing results for the virtual-thread drill; a 5-line summary of what the GC log showed.

**Week 1 review gate:** answer cold, out loud: pinning (and the Java 24 change), HashMap resize, volatile vs atomic, thread-pool sizing, G1 vs ZGC at a high level. Anything shaky goes onto the Week 4 revision list.

---

## 4. Week 2 — The Conga stack (Cassandra, Redis, Service Bus)

**Goal:** be able to design with these tools and discuss failure modes, even without production experience. Use `conga-gaps.md` as the text; each day you also *do* something.

### Day 8 — Cassandra model (P0)
- [ ] Read `conga-gaps.md` §1.1–1.3.
- [ ] Do: for each query below, write the table: "latest 50 orders per customer", "orders by status per day", "audit events per tenant with a TTL". State partition key, clustering key, and expected partition size.
- **Output:** 3 `CREATE TABLE` statements + one paragraph each on partition sizing.

### Day 9 — Cassandra consistency and pitfalls (P0)
- [ ] Read §1.4–1.7.
- [ ] Do: explain W+R>RF with RF=3 and four level combinations; explain why `ALLOW FILTERING` and tombstone-heavy queues hurt; when to use LWT.
- [ ] (Optional, 45 min) Run Cassandra in Docker (`docker run cassandra`) and create one of your Day 8 tables.
- **Output:** a table of consistency-level combinations and what each guarantees.

### Day 10 — Redis caching (P0)
- [ ] Read §2.1.
- [ ] Do: write the cache-aside code in Java (Jedis or Lettuce) with TTL jitter and a per-key lock (`SET NX PX`) to prevent a stampede.
- [ ] Explain: eviction policies, when `allkeys-lru` vs `volatile-ttl`, what Redis Cluster hash tags are for, what can be lost on failover.
- **Output:** cache-aside class + stampede test with 50 concurrent threads.

### Day 11 — Redis Streams (P0)
- [ ] Read §2.2 and the two Redis doc pages it links.
- [ ] Do (Docker Redis): `XADD`, create a group, `XREADGROUP` with two consumers, kill one without `XACK`, recover with `XAUTOCLAIM`; check the delivery counter with `XPENDING`.
- **Output:** a terminal transcript saved in your notes, plus a 3-sentence at-least-once explanation.

### Day 12 — Service Bus (P0)
- [ ] Read `conga-gaps.md` §3.
- [ ] Do: draw the message lifecycle (send → peek-lock → complete/abandon/DLQ) and mark where duplicates can occur. Write the idempotent-consumer logic (dedupe on `message-id`) in pseudo-Java.
- [ ] Explain: sessions (ordering), duplicate-detection window, max delivery count, DLQ reasons, why Basic tier does not work for sessions/topics.
- **Output:** the lifecycle diagram + idempotent consumer code.

### Day 13 (weekend) — Event-driven patterns (P0)
- [ ] Read `conga-gaps.md` §6; `createfuture-Payment Gateway.md` (idempotency, retries, webhooks).
- [ ] Do: design the **transactional outbox** for an order service: tables, relay, failure cases, consumer dedupe. Then sketch a 3-step saga with compensations.
- [ ] Do: system-design rep #1 (45 min, timed, out loud): "quote/contract approval workflow engine" (`conga-gaps.md` §9).
- **Output:** outbox design + saga sketch + the rep's notes (what you missed).

### Day 14 (weekend) — Review and consolidation
- [ ] Re-explain, without notes: Cassandra partition design, Redis Streams recovery, Service Bus sessions + DLQ, outbox, at-least-once vs exactly-once.
- [ ] Redo any Week 1 items from the review gate.
- **Output:** a list of remaining weak spots (feeds Week 4).

**Week 2 gate:** you can answer the likely questions at the end of §1.7, §2.3 and §3.8 of `conga-gaps.md` without opening it.

---

## 5. Week 3 — Spring Boot, persistence, microservices, cloud

**Goal:** close the biggest hole in the repo (Spring Boot + JPA) and finish the Conga infrastructure topics. The Spring items are written *by you* into new files, because writing is how they stick.

### Day 15 — Spring core recap + Boot auto-configuration (P1)
- [ ] Read `README.md` Spring and Microservices (IoC/DI, scopes, lifecycle), `short-cheat-sheet.md` Spring Framework.
- [ ] Learn (docs.spring.io reference): how `@SpringBootApplication` works (`@EnableAutoConfiguration`, component scan), starters, `@ConditionalOnClass/OnMissingBean`, `application.yml` + profiles, `@ConfigurationProperties`.
- [ ] Do: build a tiny Boot app; add a custom auto-configuration that registers a bean only if a property is set; explain how you disable or override an auto-config.
- **Output:** start `spring-boot-cheatsheet.md` with sections: IoC recap, auto-config, config/profiles.

### Day 16 — Spring MVC, validation, error handling (P1)
- [ ] Learn: `DispatcherServlet` request flow, filters vs interceptors, `@RestController`, `@Valid`, `@ControllerAdvice` + problem details, idempotent endpoints.
- [ ] Do: REST API with DTOs, validation, a global error handler and an `Idempotency-Key` header on POST.
- **Output:** the API + a "request lifecycle" diagram in `spring-boot-cheatsheet.md`.

### Day 17 — AOP, proxies, `@Transactional` (P0)
- [ ] Learn: JDK vs CGLIB proxies; propagation (`REQUIRED`, `REQUIRES_NEW`, `NESTED`…), isolation, rollback only on unchecked by default, **self-invocation bypasses the proxy**, read-only transactions, transaction + messaging (why you need the outbox).
- [ ] Re-read your isolation-level notes (`Questions.md` PostgreSQL Transaction Isolation Levels).
- [ ] Do: write a test that proves `@Transactional` is ignored on a self-call and then fix it.
- **Output:** a failing-then-passing test and the notes in `spring-boot-cheatsheet.md`.

### Day 18 — JPA/Hibernate (P0)
- [ ] Learn: entity states (transient/persistent/detached/removed), persistence context + dirty checking, lazy vs eager, **N+1 and fixes** (`JOIN FETCH`, entity graphs, batch size), `equals/hashCode` on entities, optimistic locking (`@Version`), pagination pitfalls with fetch joins.
- [ ] Re-read `README.md` Optimistic vs Pessimistic Locking, Indexes.
- [ ] Do: reproduce N+1 with SQL logging on, then fix it and compare query counts. Run `EXPLAIN` on one query with and without an index.
- **Output:** `jpa-hibernate-cheatsheet.md` + a before/after query count.

### Day 19 — Security, Actuator, testing slices (P1)
- [ ] Learn: Spring Security filter chain, JWT validation, OAuth2 resource server (high level), method security; Actuator endpoints and securing them, health groups for K8s probes (links to the K8s probe notes), Micrometer metrics.
- [ ] Learn: `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`, **Testcontainers** (Postgres/Redis).
- [ ] Do: secure one endpoint with a JWT test; write one Testcontainers test.
- **Output:** two more sections in `spring-boot-cheatsheet.md`; passing tests.

### Day 20 (weekend) — Microservices, resilience, Dropwizard mapping (P0/P1)
- [ ] Read `README.md` Microservice Patterns (communication, data, deployment, security, observability); `short-cheat-sheet.md` Monolith vs Microservices.
- [ ] Do: add Resilience4j retry + circuit breaker + timeout to a client call; explain which operations are safe to retry.
- [ ] Read `conga-gaps.md` §7.2 (Dropwizard); write a table mapping Dropwizard ↔ Spring Boot concepts.
- [ ] Observability: add correlation ID logging and one Micrometer metric.
- **Output:** resilience demo; Dropwizard↔Spring table.

### Day 21 (weekend) — K8s, AKS, Helm, CI/CD, OpenSearch
- [ ] Read `conga-gaps.md` §4 and §5.
- [ ] Do: containerise your Day 15–20 app (multi-stage Dockerfile), write a minimal Helm chart (deployment, service, probes, config), run it on a local cluster (kind/minikube); break the readiness probe and watch what happens; test graceful shutdown.
- [ ] Explain: Workload Identity flow, KEDA scaling on queue depth, what a CI pipeline for this app looks like (Jenkins and GitHub Actions).
- [ ] Do: OpenSearch/Elasticsearch: index 5 documents in Docker, try `match` vs `term`, `text` vs `keyword`.
- **Output:** the Helm chart + a short "probe/shutdown" note.

**Week 3 gate:** answer cold: auto-config, `@Transactional` pitfalls, N+1 fix, JWT flow, what readiness vs liveness do.

---

## 6. Week 4 — Practice, polish, mocks

**Goal:** convert knowledge into interview performance: timed, spoken, with feedback.

### Day 22 — System design rep #2 (P0)
- [ ] 45 min timed: "document generation service" (async jobs, KEDA-scaled workers, retries/DLQ), then 20 min self-review against the framework in `conga-gaps.md` §9.
- [ ] Read `load-balancing.md` §9 (answer structure) first if you skipped it.
- **Output:** notes: what you clarified, what you forgot (failure handling? observability? back-pressure?).

### Day 23 — Coding drills (P0)
- [ ] 3 problems, 30 min each, no IDE autocomplete: LRU cache, tree level-order + LCA, one DP (LCS or knapsack). Say your complexity out loud.
- [ ] Add: merge sort from scratch; validate BST.
- **Output:** the solutions + complexity notes.

### Day 24 — Coding drills + OO design (P1)
- [ ] 2 more from `algorithmic-patterns.md` patterns (sliding window, monotonic deque) + rate limiter (sliding window, `payhawk.md`).
- [ ] OO design: tic-tac-toe or parking lot with SOLID classes, then extend it with one new requirement and show what changed.
- **Output:** the code; a note on where the design flexed.

### Day 25 — Behavioural (P0 for a Staff role)
- [ ] Write 6 STAR stories into `star-stories.md`: influencing architecture, mentoring, production incident, a reversed decision, a disagreement, delivering through ambiguity (`conga-gaps.md` §8).
- [ ] Prepare 3 questions to ask them about architecture and team.
- **Output:** `star-stories.md`, each story ≤2 minutes spoken.

### Day 26 — Company research + AI tools (P1)
- [ ] Research Conga's product/domain and recent engineering posts; write `conga-company-notes.md` (what they build, who the customers are, what "multi-tenant, event-driven" likely means for them). The repo has nothing on this yet.
- [ ] Prepare your answer on responsible AI-assisted development (the ad lists Copilot/ChatGPT as a plus).
- **Output:** one page of company notes + a 60-second AI-tools answer.

### Day 27 (weekend) — Full mock interview day
- [ ] Mock 1 (60 min): Java/concurrency deep dive + Spring questions (a friend, a peer, or a Claude session role-playing an interviewer; ask for harsh feedback).
- [ ] Mock 2 (60 min): system design on a prompt you have not practised (e.g. audit log at scale, notification service).
- [ ] Mock 3 (30 min): behavioural, using your STAR stories.
- **Output:** feedback notes per mock.

### Day 28 (weekend) — Fix and rest
- [ ] Spend ~3 h on the top 5 weak spots from the mocks. Skim only: `conga-gaps.md` question lists.
- [ ] Prepare logistics. Sleep early. No new topics.
- **Output:** the 1-page "last look" sheet (§9).

---

## 7. Shorter versions

### 14-day cut (everything is P0 only)
| Days | Content |
|---|---|
| 1–2 | Concurrency + `CompletableFuture` + virtual threads (W1 Days 5–7) |
| 3 | Collections/HashMap + LRU + streams (W1 Days 2–3) |
| 4–6 | Cassandra, Redis (cache + Streams), Service Bus (W2 Days 8–12) |
| 7 | Event-driven patterns + system-design rep #1 (W2 Day 13) |
| 8–9 | `@Transactional` + JPA/N+1 (W3 Days 17–18) |
| 10 | K8s probes/shutdown + Helm basics (W3 Day 21, shortened) |
| 11 | Coding drills (W4 Days 23–24, shortened) |
| 12 | STAR stories + company notes (W4 Days 25–26) |
| 13 | Mock interview day (W4 Day 27) |
| 14 | Fix weak spots, rest |

### 3-day emergency cut
1. **Day 1:** concurrency + virtual threads + Java 21 headlines + HashMap/collections cold answers.
2. **Day 2:** Cassandra partition design, Redis Streams recovery, Service Bus peek-lock/DLQ/sessions, outbox. Read only the "Likely questions" lists in `conga-gaps.md`.
3. **Day 3:** one system-design rep, one LRU + one tree problem, STAR stories, rest.

---

## 8. New files to write (so the folder covers every gap)

| File | Fills | Written in |
|---|---|---|
| `concurrency-drills.md` | Executors, `CompletableFuture`, `BlockingQueue`, `Semaphore` | Week 1, Day 6 |
| `spring-boot-cheatsheet.md` | Auto-config, MVC, AOP/transactions, security, Actuator, testing slices, Resilience4j | Week 3, Days 15–20 |
| `jpa-hibernate-cheatsheet.md` | Entity states, lazy/eager, N+1, locking | Week 3, Day 18 |
| `star-stories.md` | Staff-level behavioural answers | Week 4, Day 25 |
| `conga-company-notes.md` | Company/product research | Week 4, Day 26 |
| *(optional)* `observability.md`, `docker-k8s-cheatsheet.md` | Logging/metrics/tracing; Docker + K8s beyond Conga's file | any spare half-day |

---

## 9. Readiness checklist and tracking

**"Last look" sheet (Day 28):** list on one page the 10 things you most often fumble.

**Readiness check (tick when you can explain, implement/design, and debug it unprompted):**

| P0 topic | Explain | Implement/design | Debug |
|---|---|---|---|
| HashMap/collections | ☐ | ☐ (LRU) | ☐ |
| Concurrency (locks, atomics, executors, `CompletableFuture`) | ☐ | ☐ | ☐ (deadlock) |
| JVM/GC/virtual threads/pinning | ☐ | ☐ | ☐ (GC log, thread dump) |
| Cassandra modelling + consistency | ☐ | ☐ | ☐ (tombstones) |
| Redis cache + Streams | ☐ | ☐ | ☐ (stuck PEL) |
| Service Bus lifecycle, sessions, DLQ | ☐ | ☐ | ☐ (why DLQ?) |
| Outbox / saga / idempotent consumer | ☐ | ☐ | ☐ |
| `@Transactional` + JPA/N+1 | ☐ | ☐ | ☐ |
| System design method | ☐ | ☐ (2 reps) | n/a |
| Coding (LRU, tree, DP) | ☐ | ☐ | ☐ |
| STAR stories | ☐ | ☐ | n/a |

**Rule:** below ~85% on any P0 row by Day 26 means Day 28 goes to that row.

**Progress log:** copy this into your notes and fill daily.

| Day | Done? | Output location | Weak spots found |
|---|---|---|---|
| 1 | ☐ | | |
| … | | | |

---

## 10. Self-review of this plan
- **Coverage:** every topic in §1 is scheduled or explicitly P2/optional; every ❌ has a place in the §8 table or a scheduled day.
- **Known limits:** (1) no interview date, so day counts are generic; (2) the Spring Boot topic list comes from public study plans and search snippets plus general knowledge, not from the Conga ad (which does not mention Spring); (3) the Week 3/4 exercises are suggestions I have not run; (4) Conga-specific interview content is inferred, not confirmed (see `conga-gaps.md`).

## Sources
Plans and topic lists I used for structure and Spring Boot topics:
- 12-week Java backend roadmap with P0/P1/P2 tiers and daily time split: [dushyanthgowda28/java-backend-interview-prep](https://github.com/dushyanthgowda28/java-backend-interview-prep)
- Java backend topic structure (Java core, Spring, JPA, Kafka, testing, system design): [6yJlka/java-backend-interview-prep](https://github.com/6yJlka/java-backend-interview-prep)
- Interview topic categories and "map to the job description, then close the highest-impact gaps": [50+ Java Backend Interview Questions 2026 (easyinterview.me)](https://easyinterview.me/blogs/the-interview-questions-that-matter/complete-java-backend-developer-interview-guide)
- Search results used for senior Spring Boot topics (auto-configuration, `@Transactional`, N+1, security, Actuator) and 4-week/30-day plan outlines, **read as search-result summaries only** because the pages returned HTTP 403 when fetched: [45+ Spring Boot Interview Questions for Experienced Developers (golinuxcloud)](https://www.golinuxcloud.com/spring-boot-interview-questions-experienced/), [How To Crack Senior Java Interviews (6–10 YOE) In 4 Weeks (faun.dev)](https://faun.dev/co/stories/pramod_kumar_0820/how-to-crack-senior-java-interviews-6-to-10-years-of-experience-in-4-weeks/), [30 Days to Crack Java & Spring Boot Interviews (Medium)](https://medium.com/@pandyahimanshu09041995/30-days-to-crack-java-spring-boot-interviews-a-practical-roadmap-64cdaa75084a)
- Inside this repo: `conga-gaps.md` and every file named in §1.
