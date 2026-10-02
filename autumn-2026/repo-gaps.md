# Repo Gaps — everything missing or thin in this folder

Compiled 2026-10-02 from the topic inventory in `study-plan.md` §1, the Conga job ad, and `conga-gaps.md`. Evidence comes from heading lists of every `.md` file plus keyword searches across the repo.

**Legend**
- **Missing** = no dedicated section anywhere in the repo.
- **Thin** = mentioned or only partly covered.
- **Tier:** P0 = must explain and code/design cold, P1 = solid working knowledge, P2 = awareness.
- **Fix** = the file where it gets written, and the day in `study-plan.md` when that happens.

---

## Summary: the biggest gaps

1. **Spring Boot internals are missing:** auto-configuration, MVC request flow, AOP/proxies, `@Transactional`, JPA/Hibernate/N+1, Spring Security, Actuator and test slices.
2. **The executor / `CompletableFuture` coverage in `README.md` is thin**, and there is nothing on `Semaphore`, latches, barriers, `StampedLock` or `BlockingQueue` producer–consumer.
3. **There is nothing on Conga itself** (product, domain, engineering culture).
4. **Staff-level behavioural material is only an outline** (STAR stories do not exist yet).
5. **Several Conga-stack topics exist only in `conga-gaps.md`**, which was written in this session and still has unverified (⚠️) parts: OpenSearch internals, CI/CD tools, and Cassandra Accord transactions.
6. **Tree/DP coding drills and an OO-design exercise are missing**, even though Glassdoor reports for other Conga roles mention trees, DP, LRU and tic-tac-toe.

---

## A. Spring / Spring Boot

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| A1 | Auto-configuration, starters, `@ConditionalOn…`, `@ConfigurationProperties`, profiles | Missing | Only IoC/DI, bean scopes and lifecycle in `README.md` | Standard senior Spring Boot question | P1 | `spring-boot-cheatsheet.md`, Day 15 |
| A2 | Spring MVC: `DispatcherServlet` flow, filters vs interceptors, validation, `@ControllerAdvice` | Missing | Controller snippets in `real-time-communications.md` only | REST API design is core to any backend round | P1 | `spring-boot-cheatsheet.md`, Day 16 |
| A3 | AOP and proxies (JDK vs CGLIB), self-invocation pitfall | Missing | None | Explains why `@Transactional`/`@Async` silently fail | P1 | `spring-boot-cheatsheet.md`, Day 17 |
| A4 | `@Transactional`: propagation, isolation, rollback rules, read-only, transactions with messaging | Thin | Isolation levels (PostgreSQL) in `Questions.md`, `short-cheat-sheet.md`; annotation used inside company docs only | Likely deep-dive question; links to the outbox pattern | P0 | `spring-boot-cheatsheet.md`, Day 17 |
| A5 | Spring Data JPA / Hibernate: entity states, persistence context, lazy vs eager, **N+1** and fixes, `@Version`, fetch-join pagination | Thin | Optimistic/pessimistic locking and indexes in `README.md`; N+1 and JPA only mentioned | Most common persistence interview topic | P0 | `jpa-hibernate-cheatsheet.md`, Day 18 |
| A6 | Spring Security: filter chain, JWT, OAuth2/OIDC resource server, method security | Missing | JWT/OAuth named in company docs only | Expected in API-security questions | P1 | `spring-boot-cheatsheet.md`, Day 19 |
| A7 | Actuator + Micrometer, health groups for Kubernetes probes | Missing | None | Links Spring to the K8s probe material | P1 | `spring-boot-cheatsheet.md`, Day 19 |
| A8 | Test slices (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`) and **Testcontainers** | Missing | JUnit 5/Mockito only | Integration testing for Cassandra/Redis-style stacks | P1 | `spring-boot-cheatsheet.md`, Day 19 |
| A9 | Resilience4j: retry, circuit breaker, bulkhead, time limiter | Thin | Mentioned in `README.md`, `createfuture-Payment Gateway.md` | Needed for downstream-failure answers | P1 | Day 20 |
| A10 | Spring integration with Redis, Kafka, Service Bus | Missing | None | Matches the Conga stack | P1 | `spring-boot-cheatsheet.md`, Days 19–20 |
| A11 | Caching abstraction, `@Async`, scheduling | Missing | None | Common follow-ups | P2 | `spring-boot-cheatsheet.md` (optional) |
| A12 | WebFlux / reactive | Thin | Mentions in `README.md`, `short-cheat-sheet.md` | Needed to compare with virtual threads | P2 | optional |
| A13 | **Spring Boot is not named in the Conga ad** (Dropwizard is) | Scope note | `conga-gaps.md` §7.2 covers Dropwizard | Spend time proportionally | P1 | Dropwizard↔Spring mapping table, Day 20 |

## B. Concurrency and JVM

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| B1 | `ExecutorService` / `ThreadPoolExecutor` (core/max/queue/rejection), pool sizing | Thin | Mentions in `README.md`; no dedicated section | The ad says "multi-threading" | P0 | `concurrency-drills.md`, Day 6 |
| B2 | `CompletableFuture`: `thenCompose` vs `thenCombine`, `allOf`, error handling, timeouts, common-pool pitfall | Thin | Mentions in `README.md`, `real-time-communications.md` | Typical coding exercise | P0 | `concurrency-drills.md`, Day 6 |
| B3 | `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `Phaser`, `StampedLock`, `BlockingQueue` producer–consumer | Missing | None (only `synchronized`, `ReentrantLock`, `ReadWriteLock`, atomics) | Often coded live | P0 | `concurrency-drills.md`, Day 6 |
| B4 | Deadlock diagnosis with thread dumps | Thin | Deadlock concept covered; no tooling section | Staff-level debugging question | P1 | Day 5 |
| B5 | GC tuning in practice: reading GC logs, heap dumps, JFR, async-profiler | Missing | GC concepts in `short-cheat-sheet.md`, `README.md` | "Performance optimisation" is in the ad | P1 | Day 7 |
| B6 | Virtual threads | Covered (new) | `conga-gaps.md` §7.1.1; some ⚠️ items unverified (framework switches, driver pinning) | Java 21 plus in the ad | P0 | verify the ⚠️ items before the interview |

## C. Data, messaging, search

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| C1 | Cassandra | Covered (new) | `conga-gaps.md` §1. Unverified: Accord transactions status, numeric partition-size limits, tombstone thresholds, DataStax driver specifics | Named in the ad | P0 | verify ⚠️ items, then do Days 8–9 |
| C2 | Redis caching, eviction, Cluster, Streams | Covered (new) | `conga-gaps.md` §2. Unverified: some cache-pattern details; trimming options added in Redis 8.2 | Named in the ad | P0 | Days 10–11 |
| C3 | Azure Service Bus | Covered (new) | `conga-gaps.md` §3. Unverified: `maxConcurrentCalls`/auto-complete options, Spring Cloud Azure | Named in the ad | P0 | Day 12 |
| C4 | OpenSearch / Elasticsearch | Thin | `conga-gaps.md` §5: only the Java-client fact is verified; the rest is general knowledge | Named in the ad | P1 | Day 21; verify against OpenSearch docs |
| C5 | Kafka | Thin | Mentions across company docs, `README.md` | Common comparison point for Service Bus/Streams | P1 | optional note |
| C6 | Query plans (`EXPLAIN`) and slow-query diagnosis | Thin | Mentioned in `payhawk.md` | Practical performance question | P1 | Day 18 |
| C7 | Multi-tenant data modelling (one huge tenant, tenant isolation) | Thin | Only in `officernd.md` and `conga-gaps.md` | Conga is a multi-tenant SaaS style platform (inferred) | P1 | System-design reps |

## D. Architecture and system design

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| D1 | Event-driven patterns (outbox, inbox, saga, CQRS) | Covered (new) | `conga-gaps.md` §6, general knowledge, not verified against sources | Explicit in the ad | P0 | Day 13 |
| D2 | Observability: logs, metrics (RED/USE), tracing (OpenTelemetry), SLOs, DLQ depth | Thin | One-line mention in `conga-gaps.md`; `README.md` monitoring patterns | Staff-level expectation | P1 | `observability.md` (optional), Day 20 |
| D3 | Staff-level system-design practice (timed reps) | Missing | Prompts listed in `conga-gaps.md` §9; no completed designs | Skill needs practice, not reading | P0 | Days 13, 22, 27 |

## E. Cloud, containers, delivery

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| E1 | Docker for Java (multi-stage builds, JVM memory in containers) | Thin | Mentions only | Needed for AKS/Helm answers | P1 | Day 21 |
| E2 | Kubernetes, AKS Workload Identity, KEDA, Helm | Covered (new) | `conga-gaps.md` §4 (verified parts) | Named in the ad | P1 | Day 21 |
| E3 | CI/CD (Jenkins, GitHub Actions) | Thin | `conga-gaps.md` §4.6, general knowledge only | Named in the ad | P1 | Day 21 |
| E4 | Git workflow, Maven/Gradle | Missing | None | Basic hygiene questions | P2 | optional |
| E5 | BDD (Cucumber/Gherkin), contract testing (Pact) | Thin | Outline in `conga-gaps.md` §7.5 | Ad lists BDD/TDD | P1 | Day 19 |

## F. Coding rounds

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| F1 | Tree DFS/BFS drills (level order, LCA, validate BST) | Missing | Pattern text in `algorithmic-patterns.md`; `RedBlackTreeMap.java` | Glassdoor reports (other Conga roles) mention trees | P1 | Days 1, 23 |
| F2 | Classic DP (LCS, knapsack) | Thin | Kadane in `accenture-algo-theory-prep*.md`, palindrome DP in `algorithmic-patterns.md` | Same reports mention DP | P1 | Day 23 |
| F3 | Merge sort / quick sort from scratch, anagram grouping | Missing | None | Same reports | P1 | Days 2, 23 |
| F4 | OO design exercise (tic-tac-toe / parking lot) | Missing | SOLID theory only | Same reports | P1 | Day 24 |

## G. Soft skills and company knowledge

| # | Gap | Status | What exists today | Why it matters | Tier | Fix |
|---|---|---|---|---|---|---|
| G1 | **Nothing on Conga itself** (product, customers, domain, engineering blog) | Missing | None | "Why Conga" is a certain question; domain shapes system-design prompts | P1 | `conga-company-notes.md`, Day 26 |
| G2 | STAR stories for a Staff role (architecture influence, mentoring, incident, reversal, disagreement, ambiguity) | Thin | Outline in `conga-gaps.md` §8; `Tide.md` has the EM angle | Staff interviews weigh leadership | P0 | `star-stories.md`, Day 25 |
| G3 | Responsible AI-assisted development answer | Missing | None | Ad lists Copilot/ChatGPT as a plus | P1 | Day 26 |
| G4 | Questions to ask the interviewers | Missing | None | Standard closing round | P1 | Day 25 |

## H. Unverified or uncertain content already in the repo

These are not missing topics. They are things written down that I could not confirm:

| # | Item | Where | Action |
|---|---|---|---|
| H1 | Cassandra Accord / transactions status (docs page returned 404) | `conga-gaps.md` §1.6 | Check the Cassandra docs for your target version |
| H2 | Numeric Cassandra partition-size guidance, tombstone thresholds | `conga-gaps.md` §1.2, §1.4 | Check Cassandra docs |
| H3 | Redis 8.2 consumer-group-aware trimming/deletion option names | `conga-gaps.md` §2.2 | Check the Redis command pages before quoting |
| H4 | OpenSearch concepts (shards, analysers, refresh, `search_after`) | `conga-gaps.md` §5 | Verify against OpenSearch docs |
| H5 | Driver behaviour under virtual threads (JDBC, DataStax, Service Bus SDK, Lettuce/Jedis) and framework switches | `conga-gaps.md` §7.1.1 | Check each library's docs |
| H6 | Interview format for this Conga role | `conga-gaps.md` | Inferred from Glassdoor reports on other Conga roles. Ask the recruiter what the rounds are |
| H7 | Kubernetes: "liveness should not check dependencies" | `conga-gaps.md` §4.1 | Best practice, not found in the official pages I read |

## I. Housekeeping gaps in the repo itself

Observed from the file list and headings, not from the web:

| # | Issue | Evidence | Suggested fix |
|---|---|---|---|
| I1 | Two near-duplicate Accenture notes | `accenture-algo-theory-prep.md` (modified) and untracked `accenture-algo-theory-prep copy.md` (extra "Pattern Cheat Sheet" section) | Merge into one file, delete the copy |
| I2 | SOLID appears twice in `README.md` (a standalone section and again under OOP) and again in `short-cheat-sheet.md` | Heading list | Keep one full version, link from the others |
| I3 | Very large file with no table of contents | `README.md` is ~264 KB, 8,000+ lines | **Done 2026-10-02:** added a table of contents (21 top-level topics) under the title. Splitting by topic is still open |
| I4 | Filename typos | `java-design-patters.md`, `stateful-vs-sateless.md`, `real-time-comunications.md` | **Done 2026-10-02:** renamed with `git mv`, links updated |
| I5 | Two `RedBlackTreeMap.java` files (root and `src/`) | `diff` showed them identical | **Done 2026-10-02:** removed the root copy, kept `src/` |
| I6 | `out/` build output and `.iml` file in the repo | `out/` was already ignored; `java-cheatsheet.iml` was tracked | **Done 2026-10-02:** untracked the `.iml`; `.gitignore` now has `*.iml` and `.idea/` |
| I7 | No index of what is where | Folder listing | **Done 2026-10-02:** added `INDEX.md` at the repo root |
| I8 | `PO.md` (SQL for product owners) is off-topic for a Java Staff role | File content | Keep as P2 reference or move out of the interview path |

---

## Counts

| Category | Gaps | Of which P0 |
|---|---|---|
| A. Spring / Spring Boot | 13 | 2 |
| B. Concurrency / JVM | 6 | 4 |
| C. Data, messaging, search | 7 | 3 |
| D. Architecture / design | 3 | 2 |
| E. Cloud / delivery | 5 | 0 |
| F. Coding rounds | 4 | 0 |
| G. Soft skills / company | 4 | 1 |
| H. Unverified content | 7 | n/a |
| I. Housekeeping | 8 | n/a |

The schedule that closes these gaps is in `study-plan.md`. Tick each row off here when its file or exercise is done.
