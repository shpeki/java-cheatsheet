# Conga — Staff Software Engineer (Java), Sofia — Gap Coverage

Job: https://bg.linkedin.com/jobs/view/staff-software-engineer-java-at-conga-4454967579
Researched/verified: 2026-10-02 against official docs (links in each section and in "Sources").

**Stack from the ad:** Java 5+ yrs (Java 21 + Dropwizard a plus), Cassandra / NoSQL, Redis (caching + Streams), Azure Service Bus, Azure (AKS), Docker, Helm, OpenSearch/Elasticsearch, Jenkins + GitHub Actions, BDD/TDD, distributed + event-driven design.

**Interview sourcing:** no candidate report exists for this exact Sofia role. Question lists below are *inferred* from the job ad plus general Glassdoor reports for other Conga engineering roles (LRU cache, trees, DP, OOP, REST). Treat them as likely, not confirmed.

## Verification legend
- ✅ **Verified** against the cited official doc on 2026-10-02.
- ⚠️ **Not verified** here: general engineering knowledge or best practice. Check before quoting it as fact.

---

## 0. Gap map: what the repo covers vs. what is missing

| Area in the ad | Existing coverage in repo | Status |
|---|---|---|
| OOP, SOLID, design patterns | `README.md`, `java-design-patterns.md` | Covered |
| Core algorithms, LRU, rate limiter | `algorithmic-patterns.md`, `data-structures-cheatsheet.md`, `binary-search.md` | Covered |
| Load balancing, consistent hashing | `load-balancing.md`, `src/ConsistentHashRing.java` | Covered |
| Java memory model, concurrent collections | `additional-java-topics.md`, `README.md` | Partial: no executors / `CompletableFuture` / virtual threads depth |
| Idempotency, retries, webhooks | `createfuture-Payment Gateway.md` | Partial: Kafka-centric, not Service Bus / Redis Streams |
| **Cassandra data modelling** | none | **Gap** |
| **Redis caching + Redis Streams** | only sorted-set / rate-limiter mentions | **Gap** |
| **Azure Service Bus** | none | **Gap** |
| **AKS / Kubernetes / Helm** | none | **Gap** |
| **OpenSearch / Elasticsearch** | none | **Gap** |
| **Java 21, Dropwizard** | partial "New Java Features" | **Gap** |
| **Event-driven patterns (outbox, saga, CQRS)** | none | **Gap** |
| **Staff-level behaviours** | `Tide.md` (EM angle only) | Partial |
| **BDD/TDD, CI/CD** | JUnit/Mockito in `short-cheat-sheet.md` | Partial: no BDD, Jenkins, GitHub Actions |

---

## 1. Cassandra (highest priority)

Docs: https://cassandra.apache.org/doc/latest/

### 1.1 Mental model
- ✅ **CAP:** Cassandra chooses Availability + Partition tolerance and compromises on consistency "to some extent". Guarantees listed in the docs: high scalability, high availability, durability, **eventual consistency**, **linearizable consistency via lightweight transactions (Paxos)**, and multi-table batches that succeed or fail entirely. ([guarantees](https://cassandra.apache.org/doc/latest/cassandra/architecture/guarantees.html))
- ✅ **Write path:** commit log (append-only, durability) → memtable (in-memory, sorted) → flushed to immutable **SSTables**. Commit-log sync is *batch* (ack after fsync) or *periodic* (ack immediately, fsync in the background; default 10 s). **Bloom filters** let reads skip SSTables that cannot hold the partition. **Compaction** merges SSTables (write amplification, but reclaims space and improves reads). ([storage engine](https://cassandra.apache.org/doc/latest/cassandra/architecture/storage-engine.html))
- ✅ **Replication / consistency:** RF = number of copies. Replicas are spread across distinct nodes (racks/DCs). Convergence uses read repair + hinted handoff + anti-entropy repair (Merkle trees). ([dynamo](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html))

### 1.2 Data modelling: query-first, not entity-first
- ✅ The docs say to "start with the query model" and create the tables that support it. **Denormalisation is normal** ("don't be afraid of it"). **No joins, no referential integrity, no cascading deletes.** Sort order is a design decision fixed by the clustering columns. A query that touches **a single partition** typically performs best. ([RDBMS vs Cassandra modelling](https://cassandra.apache.org/doc/latest/cassandra/developing/data-modeling/data-modeling_rdbms.html))
- ✅ **Primary key** = partition key (decides which replicas hold the row; rows with the same partition key are on the same replica set) + clustering columns (define in-partition order). Partitions must be sized "just right, not too big nor too small", and bad key choice creates hotspots. ([DDL](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html))

```cql
-- Query: "latest 50 orders for a customer"
CREATE TABLE orders_by_customer (
    customer_id uuid,
    order_ts    timestamp,
    order_id    uuid,
    total       decimal,
    PRIMARY KEY ((customer_id), order_ts, order_id)
) WITH CLUSTERING ORDER BY (order_ts DESC, order_id ASC);

SELECT * FROM orders_by_customer WHERE customer_id = ? LIMIT 50;
```
Need the same data by status? Write it to a second table (`orders_by_status`) from the application. Writes are cheap; there are no joins.

- ⚠️ **Bucketing** (`PRIMARY KEY ((tenant_id, day), event_ts, event_id)`) to bound partitions is standard practice. The docs I read give no numeric size limit. Commonly quoted rules of thumb (tens of MB to ~100 MB per partition) are community guidance, not from those pages.

### 1.3 Consistency levels
✅ Levels: `ONE/TWO/THREE`, `QUORUM` (n/2+1), `ALL`, `LOCAL_QUORUM`, `EACH_QUORUM`, `LOCAL_ONE`, `ANY` (writes only). Strong visibility when **W + R > RF** (e.g. RF=3, QUORUM write + QUORUM read). `LOCAL_QUORUM` keeps the quorum inside the local DC. ([dynamo](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html))

### 1.4 CQL behaviours worth knowing
✅ From the [DML docs](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html):
- No joins or sub-queries; a SELECT hits a single table.
- `INSERT`/`UPDATE` are upserts. `IF NOT EXISTS` / `IF` conditions use **Paxos** and cost performance, so use them sparingly.
- `ALLOW FILTERING` enables full scans with latency proportional to data volume. Queries needing it are rejected by default.
- `TTL` in seconds; an update resets the TTL.
- **BATCH:** LOGGED (default) gives atomicity across partitions at a performance cost. UNLOGGED skips that. COUNTER batches exist for counters.

✅ **Tombstones:** `gc_grace_seconds` default **864000 s (10 days)**. `default_time_to_live` default 0 (no expiry). ([DDL](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html))
⚠️ Tombstone-heavy reads (queue-like tables, frequent deletes, explicit nulls) degrading latency is well-known operational behaviour. I did not verify the exact warning/failure thresholds.

### 1.5 Materialized views: be careful what you claim
✅ The current docs do **not** label MVs "experimental". They list restrictions (no functions/casting/static columns, `IS NOT NULL` on non-key columns in the WHERE, view key = base key + at most one extra column) and **advise against deleting base columns that are not selected in the view**, because that can shadow missed updates received via hints/repair (CASSANDRA-13826). ([MVs](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/mvs.html))
⚠️ Many teams still prefer application-maintained query tables. That is operational folklore, so present it as an opinion.

### 1.6 Not verified
- ⚠️ **Accord / multi-partition transactions:** I could not load a docs page for this (404). Do not claim a status. Say "newer Cassandra versions are adding general-purpose transactions; I would check the version in use".
- ⚠️ DataStax Java driver 4.x specifics (`CqlSession`, prepared statements, `setIdempotent`, paging state, DC-aware load balancing) are from general knowledge. Check them against the driver docs.

### 1.7 Likely questions
1. "Design a Cassandra schema for X (audit log / message inbox / time series / activity feed)." Walk queries → tables → partition sizing.
2. "Why not add a secondary index or use `ALLOW FILTERING`?"
3. "Pick `QUORUM` vs `LOCAL_QUORUM` vs `ONE` and explain the latency/availability impact."
4. "Reads slowed down after many deletes. Why?" (Tombstones, `gc_grace_seconds`, compaction.)
5. "Multi-tenant system where one tenant is 1000x larger?" (Tenant + bucket in the partition key.)

---

## 2. Redis: caching and Redis Streams

### 2.1 Caching patterns
⚠️ Cache-aside, read-through, write-through, write-behind, stampede protection (single-flight, per-key lock, TTL jitter, serve-stale), penetration (negative caching, bloom filter), hot keys (local L1 cache) are general patterns I did not verify against a single source. Know them as design vocabulary.

✅ **Eviction** ([docs](https://redis.io/docs/latest/develop/reference/eviction/)):
- `maxmemory 0` (no limit) is the default on 64-bit systems.
- Policies: `noeviction`, `allkeys-lru`, `allkeys-lrm`, `allkeys-lfu`, `allkeys-random`, `volatile-lru`, `volatile-lrm`, `volatile-lfu`, `volatile-random`, `volatile-ttl`.
- `volatile-*` policies behave like `noeviction` if no keys have a TTL.
- Docs' rule of thumb: `allkeys-lru` is a good default if you have no reason to prefer another.
- LRU/LFU are **approximations** (sampling, `maxmemory-samples`). **LRM** (least recently modified) is available from **Redis 8.6**.
- Using one instance for both cache and persistent keys is discouraged. Prefer two instances.
- Monitor `keyspace_hits / (hits + misses)`, `evicted_keys`, `expired_keys`.

✅ **Redis Cluster** ([scaling docs](https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/)): **16384 hash slots**; multi-key operations/transactions/Lua only work when all keys are in the same slot, which you force with **hash tags** (`user:{123}:profile` and `user:{123}:account` share a slot). Replication is **asynchronous**, so the cluster **can lose writes that were acknowledged to the client**, notably during failover.

### 2.2 Redis Streams (explicitly in the ad)
✅ Source: [Streams docs](https://redis.io/docs/latest/develop/data-types/streams/), [XAUTOCLAIM](https://redis.io/docs/latest/commands/xautoclaim/), [XNACK](https://redis.io/docs/latest/commands/xnack/).

- Append-only log; entry ID `<ms>-<seq>` (auto-generated with `*`), monotonically increasing; `XADD` O(1). Partial IDs (`0-*`) supported on Redis 7+.
- **Consumer groups:** `XGROUP CREATE key group $|0 [MKSTREAM]`; `XREADGROUP GROUP g consumer COUNT n STREAMS key >` (`>` = messages never delivered to this group); reading with ID `0` re-reads that consumer's own pending entries.
- **PEL (Pending Entries List):** delivered-but-un-acked entries. `XACK` removes them. `XPENDING` inspects them (summary or detailed with idle time).
- **Recovery:** `XCLAIM` / `XAUTOCLAIM` reassign entries idle longer than `min-idle-time`.

```
XAUTOCLAIM key group consumer min-idle-time start [COUNT count] [JUSTID]
```
  - `XAUTOCLAIM` since **6.2.0**. `COUNT` defaults to 100. It scans at most `count × 10` PEL entries per call, so it can claim fewer than requested.
  - Returns a **cursor** for the next call (`0-0` = scan complete; keep polling, as older entries may become eligible later) + claimed entries + IDs deleted from the stream.
  - Claiming resets idle time, so only one consumer wins a given message at a given moment.
  - Claiming **increments the delivery counter**, unless `JUSTID` is used (then it does not). High delivery counts expose **poison messages**; the docs say to detect them by monitoring.
  - Since **7.0**, entries that no longer exist in the stream (trimmed/deleted) are removed from the PEL and returned in the reply.
- **Trimming:** `XADD ... MAXLEN [~] n`, `XTRIM`. `~` = approximate (faster). Retention is manual. ⚠️ I saw a docs note that Redis **8.2+** added consumer-group-aware trimming/deletion (`XDELEX`, `XACKDEL` = ack + delete). The exact option names/semantics should be checked before quoting.
- **Newer features (version-gated, check what the employer runs):** `XACKDEL`/`XDELEX` 8.2+, idempotent production via `XCFGSET ... IDMP` 8.6+, **`XNACK` 8.8.0**. `XNACK` releases pending messages back to the group immediately (modes `SILENT` decrements the delivery counter, `FAIL` keeps it, `FATAL` sets it to max for poison messages). The XNACK page lists it as **not supported on Redis Software / Redis Cloud** (standard). Do not assume a managed Redis has it.
- **Delivery guarantee:** at-least-once → handlers must be idempotent. ⚠️ Dead-letter-stream handling is a pattern you implement yourself using the delivery counter (not a built-in feature that I verified).

⚠️ Stream vs Kafka vs Service Bus is a design comparison from general knowledge. See §3.5.

### 2.3 Likely questions
1. "How do you make sure a Stream message isn't lost if a worker dies mid-processing?" (PEL + `XAUTOCLAIM` + idempotent handler.)
2. "Streams vs Kafka vs Service Bus: when would you pick each?"
3. "How do you prevent a cache stampede on a hot key?"
4. "What can you lose during a Redis Cluster failover?" (Acknowledged writes, asynchronous replication.)

---

## 3. Azure Service Bus

Sources: [quotas](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quotas), [settlement](https://learn.microsoft.com/en-us/azure/service-bus-messaging/message-transfers-locks-settlement), [DLQ](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues), [sessions](https://learn.microsoft.com/en-us/azure/service-bus-messaging/message-sessions), [duplicate detection](https://learn.microsoft.com/en-us/azure/service-bus-messaging/duplicate-detection), [Java quickstart](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues).

### 3.1 Tiers and limits ✅
| | Basic | Standard | Premium |
|---|---|---|---|
| Max message size | 256 KB | 256 KB | AMQP up to 100 MB (default 1 MB per entity, raisable); HTTP/SBMP 1 MB; batch 1 MB |
| Queue/topic size | 1–5 GB (80 GB if partitioned) | same | 80 GB |
| Topics & subscriptions | not supported | yes | yes |
| Sessions | **no** | yes | yes |
| Duplicate detection | **no** | yes | yes |

Other limits: up to 2,000 subscriptions per topic; 100 messages per transaction; 5,000 concurrent receive requests per entity; message ID and session ID max 128 chars.

⚠️ **SDK retirement:** Microsoft's page states that on **30 September 2026** the old libraries `WindowsAzure.ServiceBus`, `Microsoft.Azure.ServiceBus` and **`com.microsoft.azure.servicebus`** (Java) are retired and SBMP support ends. Use `com.azure:azure-messaging-servicebus`. (Today is 2026-10-02, so this just happened. A legacy library in the employer's codebase is plausible.)

### 3.2 Settlement semantics ✅
- **Receive modes:** *Receive-and-Delete* (settled when sent; a failed transfer loses the message) vs **Peek-Lock** (exclusive lock; you `Complete`, `Abandon`, `DeadLetter`, or `Defer`).
- Lock expiry or abandon → message returns to the **front** of the queue for redelivery and **delivery count** increments. When it exceeds **MaxDeliveryCount (default 10)** → moved to the DLQ with reason `MaxDeliveryCountExceeded`. This cannot be disabled (only raised).
- **Lock duration:** default **1 minute**, max **5 minutes**; renew via `RenewMessageLock` or auto-renew.
- Locks are **volatile**: lost on service/OS updates, entity property changes, or connection loss (`MessageLockLostException`). Design for idempotent handling, e.g. dedupe on `message-id`.
- **Settle before closing the receiver/connection**, or the settlement does not reach the service and the message is redelivered (and eventually dead-lettered). The service closes idle connections after 10 minutes.
- Sends are explicitly settled. Do not fire-and-forget async sends, and bound in-flight sends (e.g. a semaphore).

### 3.3 Dead-letter queue ✅
- Sub-queue of every queue/subscription; path `<queue>/$deadletterqueue` (`<topic>/Subscriptions/<sub>/$deadletterqueue`). Cannot be deleted or managed independently; **no automatic cleanup**.
- System dead-letter reasons: `MaxDeliveryCountExceeded`, `TTLExpiredException`, `HeaderSizeExceeded`, `Session ID is null`, `MaxTransferHopCountExceeded` (auto-forward limit 4), plus filter-evaluation errors if enabled.
- TTL expiry only dead-letters if dead-lettering on expiration is enabled. **Deferred messages are not purged/dead-lettered on expiry.**
- Applications can dead-letter explicitly with reason + description (put the exception type in the reason).
- **Transfer DLQ** (`$Transfer/$DeadLetterQueue`) holds messages that failed to auto-forward / send-via, on the *source* entity.
- Reprocessing: Service Bus Explorer in the portal can peek, edit and resend.

### 3.4 Sessions ✅
- Set `SessionId` on messages; a session receiver gets an **exclusive lock on the session**, so messages in a session are delivered in order to one receiver at a time while other receivers get other sessions. (Sequence numbers alone guarantee queue/extraction order, **not processing order**: that needs sessions.)
- Enabling sessions on an entity means clients **can no longer send/receive regular (non-session) messages**; only peeking still works.
- Session state (opaque blob, up to message size) lets another worker resume after a failure. It stays until cleared and counts toward the entity quota.
- If **one message in a session expires (TTL)**, the system drops or dead-letters **all** messages of that session (with an active listener).
- A dead-lettered message resubmitted later gets a new enqueue time and sequence number, so **its original order is lost**.
- Delivery count increments if the session lock expires after accept; not if you close the session without completing messages.

### 3.5 Duplicate detection ✅
Broker-side, based on the **application-set `MessageId`**. A duplicate send is reported as accepted but silently dropped. Window default **10 minutes**, min 20 s, max 7 days. A bigger window costs throughput. With partitioning, uniqueness is `MessageId + PartitionKey`. Scheduled and non-scheduled duplicates also collide. Anchor `MessageId` to the business process (e.g. `orderNumber.step`) so it is reproducible after a crash.

### 3.6 Java SDK ✅
`azure-messaging-servicebus` + `azure-identity`. Sender via `ServiceBusClientBuilder().fullyQualifiedNamespace(...).credential(new DefaultAzureCredentialBuilder().build()).sender().queueName(...)`. Receiver via `.processor().queueName(...).processMessage(...).processError(...).buildProcessorClient()`; `start()` / `close()`. `processError` should branch on `ServiceBusFailureReason` (entity disabled/not found/unauthorized = unrecoverable; `MESSAGE_LOCK_LOST`; `SERVICE_BUSY` = back off). Passwordless (Entra ID + RBAC roles Data Sender/Receiver/Owner) is recommended over connection strings. Role assignments can take a few minutes (up to ~8) to propagate.
⚠️ `maxConcurrentCalls`, `disableAutoComplete`, and Spring Cloud Azure usage are from general knowledge (docs mention Spring Cloud Azure as an option).

### 3.7 Comparison (⚠️ general design knowledge)
| | Service Bus | Redis Streams | Kafka |
|---|---|---|---|
| Model | Broker with per-message lifecycle (lock/settle/DLQ) | Log + consumer groups + PEL, memory-bound | Distributed log, offsets |
| Ordering | Per session | Single log, not across consumers | Per partition |
| Replay | Limited | Yes (by ID) | Native |
| Ops | Fully managed | You run Redis (or managed) | You run it (or managed) |

### 3.8 Likely questions
1. "Ordered processing per customer with multiple consumers?" (Sessions; why sequence numbers aren't enough.)
2. "Consumer crashes after processing but before completing?" (Lock expiry → redelivery → idempotent handler / dedupe on `message-id`.)
3. "A message keeps failing. What happens and what do you do?" (Delivery count → DLQ → inspect reason → fix → resubmit, noting ordering caveat.)
4. "Why did messages land in the DLQ though handlers 'succeeded'?" (Settled after receiver closed; lock lost; TTL; size.)
5. "Service Bus or Redis Streams for this workflow?"

---

## 4. Kubernetes / AKS / Helm / Docker

### 4.1 Probes ✅
Source: [K8s probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/), [pod lifecycle](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/).
- **Liveness** fail → kubelet kills/restarts the container (for hangs/deadlocks). **Readiness** fail → pod removed from Service endpoints (no restart). **Startup** probe → liveness/readiness are held off until it succeeds (slow-starting apps; e.g. `failureThreshold: 30`, `periodSeconds: 10`).
- Mechanisms: exec, httpGet (200–399 = success), tcpSocket, gRPC.
- Defaults: `initialDelaySeconds 0`, `periodSeconds 10`, `timeoutSeconds 1`, `successThreshold 1`, `failureThreshold 3`.
- ⚠️ The advice "do not make liveness depend on the database/downstream services, or you cause restart storms" is widely repeated best practice that I did not find stated in the pages above. Present it as practice, and know the reasoning (restarting the app does not fix a down dependency).

### 4.2 Pod lifecycle and shutdown ✅
- Phases: Pending, Running, Succeeded, Failed, Unknown. Restart policy default `Always`; restart delay backs off exponentially (100 ms, 200 ms … capped at **5 minutes**), which is what `CrashLoopBackOff` is.
- Termination: pod is removed from Service endpoints, `preStop` hook runs if configured, `SIGTERM` is sent, then `SIGKILL` after `terminationGracePeriodSeconds` (default **30 s**). A `preStop` sleep gives endpoint removal time to propagate. For consumers: stop pulling new messages, finish or abandon in-flight ones within the grace period.
- ⚠️ QoS classes (Guaranteed/Burstable/BestEffort), `OOMKilled` vs CPU throttling, JVM `-XX:MaxRAMPercentage`, container-aware heap sizing: general knowledge, verify specifics. Heap ≠ container memory (metaspace, threads, direct buffers).

### 4.3 AKS Workload Identity ✅
Source: [workload identity overview](https://learn.microsoft.com/en-us/azure/aks/workload-identity-overview).
- Pods get a Kubernetes **service account token (projected)**; the **AKS OIDC issuer** lets Microsoft Entra ID trust it and exchange it for an Entra token. No secrets in the pod.
- Needed: the pod label **`azure.workload.identity/use: "true"`** (required; without it pods fail after restart), a service account annotated `azure.workload.identity/client-id`, and a **federated identity credential** on the managed identity/app. Max **20 federated credentials per managed identity**. Virtual nodes add-on is not supported.
- **AKS Automatic**: preconfigured. **AKS Standard**: you must enable workload identity + the OIDC issuer.
- Java: `azure-identity` ≥ **1.9.0**; `DefaultAzureCredential` picks up `WorkloadIdentityCredential` via injected env vars (`AZURE_FEDERATED_TOKEN_FILE`, never hard-code the path). It replaces the older pod-managed identity.

### 4.4 KEDA + Service Bus ✅
Source: [KEDA scaler](https://keda.sh/docs/latest/scalers/azure-service-bus/). Scales replicas on **active message count** (`messageCount` default 5, `activationMessageCount` default 0) for a queue or topic/subscription. Auth via `TriggerAuthentication` with `podIdentity.provider: azure-workload`. KEDA does not create entities. Note the doc says the scaler needs a Shared Access Policy with **Manage** permissions to read metrics when using connection-string auth.

### 4.5 Helm ✅
Source: [Helm charts](https://helm.sh/docs/topics/charts/).
- Chart layout: `Chart.yaml` (required: `apiVersion: v2`, `name`, `version`; `appVersion` separate), `values.yaml`, `templates/`, `charts/` (dependencies), `crds/`, optional `values.schema.json`, `NOTES.txt`. Go templates + Sprig. Built-ins like `Release.Name`, `Release.Namespace`.
- Values via chart defaults, `--values file.yaml`, `--set`. Dependencies declared in `Chart.yaml`, pulled with `helm dependency update`; `condition`/`tags` enable/disable subcharts.
- Commands: `helm create`, `lint`, `package`, `install`, `upgrade`, `rollback`, `template`.
- ⚠️ Hooks (pre-install/post-upgrade jobs), `helm diff` plugin, OCI charts, Kustomize/Argo CD/Flux as alternatives: general knowledge.

### 4.6 CI/CD (⚠️ general knowledge)
Pipeline shape: build → unit tests → static analysis → build image → scan → push to ACR → Helm deploy to dev → integration/contract tests → promote. Jenkins: declarative `Jenkinsfile`, agents, shared libraries. GitHub Actions: workflows, reusable workflows, matrix, OIDC federation to Azure (no stored secrets). DB migrations must be backward compatible (expand/contract) for rolling deploys and rollbacks.

### 4.7 Likely questions
1. "Pod in `CrashLoopBackOff` / `OOMKilled`: how do you debug?" (`describe`, `logs --previous`, events, limits vs heap, probes.)
2. "Zero-downtime deploy of a queue consumer?" (Readiness, `preStop`, SIGTERM drain, grace period, idempotency for redelivery.)
3. "Liveness vs readiness vs startup: what breaks if liveness checks the DB?"
4. "How does a pod authenticate to Service Bus without a secret?" (Workload Identity flow above.)
5. "Config and secrets per environment with Helm?"

---

## 5. OpenSearch / Elasticsearch

- ✅ **Java client:** current official client is `org.opensearch.client:opensearch-java` (docs show **3.9.0**). **`ApacheHttpClient5Transport` is the default and recommended transport**; the **`RestClient` transport is deprecated** and will be removed. ([docs](https://docs.opensearch.org/latest/clients/java/)) Do not describe the old "High Level REST Client" as the current approach.
- ⚠️ Concepts from general knowledge (unverified here): Lucene-based; documents in indices split into primary/replica shards; inverted index and analysers; `text` vs `keyword`; `bool` query with cached `filter` context; BM25; near-real-time (refresh ~1 s); not a system of record (sync from the DB via events/outbox); alias swap for zero-downtime reindex; `search_after`/PIT for deep pagination; shard sizing. Typical design question: search over contracts/quotes for a multi-tenant SaaS (per-tenant index vs shared index with tenant filter/routing, permission filtering, indexing pipeline, consistency lag).

---

## 6. Event-driven and distributed-systems patterns (⚠️ general design knowledge)

| Pattern | Solves | Notes |
|---|---|---|
| **Transactional outbox** | DB update + event publish atomically (dual-write problem) | Write event to an `outbox` table in the same transaction; relay/poller or CDC publishes. Consumers must be idempotent (at-least-once) |
| **Idempotent consumer / inbox** | Duplicate delivery | Unique constraint on message ID before side effects (Service Bus `message-id`, Streams entry ID) |
| **Saga** | Multi-service business transaction without 2PC | Compensating actions. Orchestration vs choreography trade-off |
| **CQRS** | Separate read/write models | Read models in Cassandra query tables / OpenSearch, eventually consistent |
| **Event sourcing** | State = fold of events | Audit/replay vs schema evolution and snapshotting cost |
| **Circuit breaker, retry + backoff + jitter, bulkhead, timeout** | Downstream failure | Retry only idempotent operations; always set timeouts |
| **Backpressure** | Producer faster than consumer | Bounded queues, prefetch/concurrency limits |
| **Idempotency keys** | Safe command retries | See `createfuture-Payment Gateway.md` |

Fundamentals to explain from scratch: CAP/PACELC, delivery guarantees (exactly-once is really at-least-once + idempotency), eventual consistency, ordering and clocks (see vector clocks in `data-structures-cheatsheet.md`), quorum, leader election. Observability: structured logs with correlation IDs, RED/USE metrics, distributed tracing (OpenTelemetry), SLO alerts, DLQ depth, consumer lag.

---

## 7. Java depth not yet in the repo

### 7.1 Java 21 ✅
Source: [JDK 21](https://openjdk.org/projects/jdk/21/), [JEP 444](https://openjdk.org/jeps/444), [JEP 491](https://openjdk.org/jeps/491).
- **Final in 21:** virtual threads (JEP 444), sequenced collections (JEP 431), record patterns (JEP 440), pattern matching for `switch` (JEP 441), generational ZGC (JEP 439). JDK 21 is an LTS release from most vendors (GA 2023-09-19).
- **Still preview in 21:** structured concurrency (JEP 453), scoped values (JEP 446), string templates (JEP 430), unnamed patterns/variables (JEP 443). Do not present these as final.
- **Virtual threads (JEP 444):** keep thread-per-request style; **never pool them** (create one per task); to limit concurrency use a `Semaphore`. Improves **throughput, not per-request latency**; no benefit for CPU-bound work. Use `ThreadLocal` cautiously.
- **Pinning in 21:** a virtual thread pins its carrier inside `synchronized` blocks/methods and during native/foreign calls, so blocking I/O there blocks the OS thread; the JEP suggests `ReentrantLock` for frequent long-lived synchronized sections.
- **JEP 491 (Java 24)** removes nearly all `synchronized` pinning. Remaining pinning: class loading, class initialization, and waiting on class initialization. So "use `ReentrantLock`" is Java-21-specific advice; say so.

### 7.1.1 Virtual threads in depth (JEP 444, final in Java 21) ✅
Sources: [JEP 444](https://openjdk.org/jeps/444), [Oracle Java 21 virtual-threads guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html), [`Thread` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html), [`Executors` API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html). Items marked ⚠️ were not confirmed on those pages.

#### The problem they solve
- A platform thread is a thin wrapper around an **OS thread**. OS threads are costly, so you cannot have very many, which caps the number of concurrent requests in a thread-per-request server.
- The usual escape is asynchronous/reactive code (callbacks, `CompletableFuture` pipelines). It scales, but splits one request into stages, which hurts readability, debugging and profiling.
- Virtual threads keep the simple **thread-per-request** style and still scale: M virtual threads are scheduled on N OS threads (N ≪ M).

#### What a virtual thread is
- An instance of `java.lang.Thread` that is **not tied to a particular OS thread**. It is a user-mode thread scheduled by the JVM, not by the OS. A JVM can run millions of them.
- Cheap to create and cheap to block. Best for **I/O-bound** work with high concurrency (the JEP says benefits show when there are more than a few thousand concurrent tasks and the work is not CPU-bound).
- Existing code keeps working: `Thread`, `ExecutorService`, `Future`, blocking I/O and `java.util.concurrent` all work with them. The JEP's non-goals are to remove platform threads, to silently migrate apps, or to change Java's concurrency model.

#### How they run (scheduler, carriers, mounting)
- **Carrier thread** = the platform thread a virtual thread currently runs on. **Mount** = scheduler assigns the virtual thread to a carrier. **Unmount** = the virtual thread blocks (e.g. on I/O, `LockSupport.park`, `sleep`) and releases the carrier so other virtual threads can use it.
- No affinity: a virtual thread can run on **different carriers** over its lifetime. From inside, `Thread.currentThread()` always returns the virtual thread itself; the carrier's identity is hidden.
- **Scheduler:** a dedicated **work-stealing `ForkJoinPool` in FIFO mode**, separate from the common pool used by parallel streams (which is LIFO). Parallelism defaults to the number of available processors and is tunable via `-Djdk.virtualThreadScheduler.parallelism`. `-Djdk.virtualThreadScheduler.maxPoolSize` caps how far it can temporarily grow (the Oracle `Thread` page lists default **256**).
- **No time-slicing:** the scheduler does not preempt a virtual thread. A CPU-bound virtual thread keeps its carrier until it blocks, so many CPU-bound virtual threads gain nothing over a platform thread pool.
- **Memory:** stacks live in the **GC heap** as stack-chunk objects that grow and shrink, which is why deep call stacks are fine and shallow ones are tiny. Known limit in 21: G1 does not support humongous stack chunks, so a virtual thread whose stack reaches half a G1 region (region can be as small as 512 KB) may throw `StackOverflowError`.

#### Creating them
```java
// 1. Builder (name, factory, start vs unstarted)
Thread t = Thread.ofVirtual().name("worker-1").start(() -> handle(request));
Thread u = Thread.ofVirtual().name("worker-2").unstarted(() -> handle(request)); // start() later
ThreadFactory tf = Thread.ofVirtual().name("vt-", 0).factory();

// 2. Convenience
Thread.startVirtualThread(() -> handle(request));

// 3. Executor: one NEW virtual thread per task, unbounded, NOT a pool
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i ->
        executor.submit(() -> { Thread.sleep(Duration.ofSeconds(1)); return i; }));
} // try-with-resources: ExecutorService is AutoCloseable (waits for submitted tasks, ⚠️ not spelled out on the pages I read)
```
- `Thread.isVirtual()` tells you which kind you have. `Executors.newThreadPerTaskExecutor(ThreadFactory)` is the general form (also Java 21); `cancel(true)` on a returned `Future` interrupts the thread running the task.
- The JEP's example: 10,000 tasks that each sleep 1 s finish in about 10 s on a few carriers (≈10,000 tasks/s), versus ≈200 tasks/s with a fixed pool of 200 platform threads. Same code, far higher throughput. Sleep stands in for blocking I/O.

#### How they differ from platform threads
| | Virtual thread | Platform thread |
|---|---|---|
| Backed by | JVM scheduler on carrier threads | 1:1 OS thread |
| Daemon | **Always** daemon; `setDaemon(false)` throws `IllegalArgumentException`; does not keep the JVM alive | Configurable |
| Priority | Fixed `NORM_PRIORITY`; `setPriority` has no effect | Configurable |
| Thread group | Not an active member; `getThreadGroup()` returns a placeholder group named `"VirtualThreads"` | Real group |
| Name | Empty string unless set | Auto-generated |
| Count | Millions possible | Thousands |
| Pooling | **Never pool** | Pooling is normal |
| Visible in | JSON thread dumps, JFR | `jstack`, JMX `ThreadMXBean` |

#### Rules of use
1. **Do not pool them.** One virtual thread per task, for the task's whole life. Pooling them defeats the point.
2. **Limit concurrency with a `Semaphore`** (or similar), not by shrinking a pool. This matters because unbounded virtual threads can overwhelm a database, a Cassandra cluster or a downstream API that has a fixed capacity. ⚠️ Connection-pool sizing (e.g. HikariCP) then becomes the effective limiter.
3. **Write plain blocking code.** That is the point; do not wrap it in async constructs to "help" virtual threads.
4. **They improve throughput, not latency.** A single request is not faster. More requests are served concurrently.
5. **Not for CPU-bound work** (no time-slicing, more threads than cores does not help). The Streams/ForkJoin APIs remain the way to do data parallelism.
6. **Be careful with `ThreadLocal`.** Supported (`ThreadLocal`, `InheritableThreadLocal`), but do not use it to cache costly objects shared across tasks (the pooled-thread pattern); with millions of threads that allocates one copy each. `-Djdk.traceVirtualThreadLocals` prints a stack trace when a virtual thread sets a thread-local, to find offenders during migration. Prefer immutable shared objects.

#### Pinning (the main Java 21 gotcha)
- A virtual thread is **pinned** when it cannot unmount while blocked, so it blocks its carrier OS thread. In Java 21 that happens (a) inside a `synchronized` block/method and (b) inside a native method or foreign-function call.
- The scheduler **does not compensate** by adding parallelism for pinned threads, except temporary expansion up to `maxPoolSize` in some blocking cases. Many pinned threads can therefore starve the carrier pool.
- Short `synchronized` sections that do not block are fine. The problem is **frequent, long-lived pinning around blocking I/O**. Fix: use `ReentrantLock` (`lock(); try { io(); } finally { unlock(); }`).
- JDK I/O classes were changed to avoid pinning: `BufferedInputStream/OutputStream`, `BufferedReader/Writer`, `PrintStream/PrintWriter` use explicit locks (when used directly), encoders/decoders share the enclosing stream's lock, and default buffer sizes were reduced to cut per-thread heap use.
- `java.net` `Socket`/`ServerSocket`/`DatagramSocket`: blocking calls now **unmount** the virtual thread, and are **interruptible** in a virtual thread (interrupting one blocked on a socket unparks it and closes the socket). `LockSupport.park` on a virtual thread releases the carrier.
- **Detect pinning:** `-Djdk.tracePinnedThreads=full` (full stack, highlights native frames and monitors) or `=short` (problem frames only); JFR event `jdk.VirtualThreadPinned` (enabled by default, 20 ms threshold).
- **Version note:** [JEP 491](https://openjdk.org/jeps/491) (**Java 24**) removes nearly all `synchronized` pinning. Remaining cases: class loading, class initialization, waiting on class initialization. So "replace `synchronized` with `ReentrantLock`" is Java-21-era advice. Say that in an interview.

#### Observability and tooling
- **Thread dumps:** `jcmd <pid> Thread.dump_to_file -format=json <file>` (or `-format=text`). Plain `jstack` output does not show virtual threads in the same way. The new dump does not pause the app, and omits object addresses, locks and JNI/heap stats. `-Djdk.trackAllThreads=false` opts out of tracking for builder-created virtual threads (executor-created ones stay tracked).
- **JFR events:** `jdk.VirtualThreadStart` / `jdk.VirtualThreadEnd` (disabled by default), `jdk.VirtualThreadPinned`, `jdk.VirtualThreadSubmitFailed` (scheduling failed, e.g. resource exhaustion).
- **Limits:** `ThreadMXBean` (JMX) only covers platform threads; `Thread.getAllStackTraces()` now returns platform threads only. OS-level tools show far fewer OS threads than virtual threads.
- **Debugging/profiling** still works in the thread-per-request model, since stack traces are per request. That is a main selling point over reactive code.

#### Virtual threads vs the alternatives
| | Platform thread pool | Reactive / `CompletableFuture` | Virtual threads |
|---|---|---|---|
| Code style | Blocking, simple | Async, stage-based | Blocking, simple |
| Max concurrency | Thousands (OS-bound) | Very high | Very high |
| Debugging/stack traces | Good | Poor | Good |
| CPU-bound work | Good (pool sized to cores) | OK | No benefit |
| Back-pressure | Pool/queue size | Built into reactive streams | You add it (`Semaphore`, bounded queues) |

#### What is *not* final in Java 21 (do not overclaim)
- **Structured concurrency** (JEP 453) and **scoped values** (JEP 446) are **preview** in 21, so they need `--enable-preview` and can change. They are designed to complement virtual threads (task-scoped lifetimes, cheap per-task context) but are not part of the final virtual-thread feature.

#### ⚠️ Not verified here
- Framework switches (e.g. Spring Boot's setting to run request handling on virtual threads; Tomcat/Jetty virtual-thread executors; Dropwizard support). Check the version you would use.
- Whether specific drivers (JDBC drivers, the DataStax Cassandra driver, Azure Service Bus SDK, Lettuce/Jedis) pin or are virtual-thread-friendly. Many use `synchronized` or native/Netty event loops. Check each before promising a migration.

#### Likely interview questions
1. "What are virtual threads and why were they added?" (Thread-per-request at scale without reactive code; M:N scheduling.)
2. "How do they differ from platform threads and from a thread pool?" (Table above; never pool them.)
3. "What is pinning? How do you find it? How do you fix it?" (`synchronized` / native; `jdk.tracePinnedThreads`, JFR; `ReentrantLock`; fixed for `synchronized` in Java 24 by JEP 491.)
4. "Will virtual threads speed up my CPU-heavy service?" (No: throughput for I/O-bound concurrency, no time-slicing.)
5. "How do you stop 100k virtual threads from overwhelming the database?" (`Semaphore` / bounded connection pool; back-pressure is now your job.)
6. "What changes for `ThreadLocal`?" (Still works, but avoid expensive per-thread caches.)
7. "How do you monitor/debug them?" (`jcmd Thread.dump_to_file -format=json`, JFR events; `ThreadMXBean` does not cover them.)
8. "Virtual threads vs reactive: when would you still choose reactive?" (Needs for streaming back-pressure, existing reactive stack; or CPU-bound/pipeline composition.)

#### Mini-drill (do this unaided)
Write a program that calls a slow "downstream" (simulated by `Thread.sleep(200 ms)`) 10,000 times, once with `Executors.newFixedThreadPool(200)` and once with `newVirtualThreadPerTaskExecutor()`, time both, then add a `Semaphore(100)` to the virtual-thread version and explain what changed.

### 7.2 Dropwizard ✅
Source: [getting started](https://www.dropwizard.io/en/stable/getting-started.html).
- Bundles **Jetty** (embedded HTTP), **Jersey** (JAX-RS), **Jackson**, **Metrics**, Logback/SLF4J, Hibernate Validator, Apache HttpClient, JDBI, Liquibase, templating.
- **Dropwizard 5.0.x requires Java 17+.**
- Anatomy: `Configuration` subclass (YAML-bound, validation annotations), `Application<T>` with `initialize()` and `run()`, Jersey **Resource** classes (`@Path`, `@GET`, `@Produces`), `HealthCheck` classes. Built as a fat JAR; app on **8080**, admin/metrics on **8081**.
- ⚠️ Mapping to Spring (resource ↔ `@RestController`, `Configuration` ↔ `@ConfigurationProperties`, admin/health ↔ Actuator), "no DI container by default" and "fewer moving parts": general knowledge/opinion. If asked whether you used it, be honest and map concepts.

### 7.3 Concurrency (⚠️ general knowledge: the ad says "multi-threading")
Existing notes cover the JMM and concurrent collections. Add and drill:
- `ExecutorService` types, sizing (CPU-bound ≈ cores; I/O-bound higher), bounded queues + `RejectedExecutionHandler` (e.g. `CallerRunsPolicy` as backpressure), clean `shutdown()/awaitTermination`, thread names.
- `CompletableFuture`: `thenApply` vs `thenCompose` vs `thenCombine`, `allOf/anyOf`, `exceptionally/handle`, `orTimeout/completeOnTimeout`; avoid blocking tasks on the common pool.
- Locks and coordination: `ReentrantLock` (tryLock, fairness), `ReadWriteLock`, `StampedLock`, `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `Phaser`.
- Atomics/CAS, `LongAdder`, `volatile` (visibility/ordering, not atomicity), happens-before, `ConcurrentHashMap.compute/merge`.
- Deadlock/livelock/starvation and diagnosis with thread dumps. Producer–consumer with `BlockingQueue` coded unaided.

### 7.4 Performance (⚠️ general knowledge)
Measure first (JFR, async-profiler, metrics), find the bottleneck, change one thing, re-measure. Common wins: N+1 queries, missing indexes, connection pool sizing (HikariCP), batching, caching, non-blocking I/O, allocation churn, GC tuning (logs, heap dumps), JMH for micro-benchmarks.

### 7.5 BDD / TDD (⚠️ general knowledge)
TDD red/green/refactor with JUnit 5 + Mockito/AssertJ is already in `short-cheat-sheet.md`. Missing: BDD with Gherkin + Cucumber-JVM, **Testcontainers** for Cassandra/Redis integration tests, contract tests (Pact), test pyramid, testing async/event-driven code (Awaitility; redelivery and idempotency tests).

---

## 8. Staff-level behavioural and leadership prep

Prepare 5–6 STAR stories:
1. **Influencing architecture** across teams without authority (RFC/ADR, trade-offs, building consensus, outcome).
2. **Mentoring juniors** (concrete growth result, code review approach, pairing).
3. **A production incident** you led or fixed (timeline, root cause, blameless postmortem, systemic fix).
4. **A decision you reversed** or a trade-off you got wrong.
5. **A design disagreement** and how it resolved.
6. **Delivering through ambiguity**.

Also prepare: why Conga, what you know about its product (⚠️ not researched here: do that before the interview), and how you use AI-assisted tools responsibly (the ad lists Copilot/ChatGPT as a plus: where you trust them, where you verify).

---

## 9. Likely system-design prompts (⚠️ inferred, practise one end to end)

Frame within a multi-tenant, event-driven platform on Azure:
1. **Quote/contract approval workflow engine:** state machine, approvals, audit log (Cassandra), notifications (Service Bus topics), search (OpenSearch), idempotent commands.
2. **Document generation service:** async jobs, queue workers scaled by KEDA, retries/DLQ, status polling vs push (see `real-time-communications.md`).
3. **Multi-tenant rate limiter / throttling:** Redis, token bucket vs sliding window (see `data-structures-cheatsheet.md`), noisy-neighbour isolation.
4. **Audit/event log at scale:** write-heavy, time-bucketed Cassandra partitions, TTL retention, query-first tables.
5. **Notification service** (also reported for Payhawk, see `payhawk.md`): fan-out, preferences, dedupe, retries.

Narrate in ~45 min: clarify requirements + scale → API + data model → high-level architecture → deep dive 1–2 risky parts (consistency, ordering, failure handling) → bottlenecks, scaling, observability → trade-offs. See `load-balancing.md` §9.

---

## 10. Coding-round warm-ups (inferred from Glassdoor reports on other Conga roles)

Reported/likely: LRU cache, tree problems, a DP problem, merge sort (+ optimisation), tic-tac-toe design, arrays/anagrams.
Repo support: LRU in `data-structures-cheatsheet.md`; patterns in `algorithmic-patterns.md`; binary search in `binary-search.md`; `RedBlackTreeMap.java` for tree depth.
**Add drills:** tree DFS/BFS (level order, LCA, validate BST), a classic DP (climbing stairs, knapsack, LCS), anagram grouping with `Map<String, List<String>>`, merge/quick sort from scratch, an OO design exercise (tic-tac-toe or parking lot) with clean classes and SOLID.

---

## 11. Study plan (priority order)

| Priority | Topic | Time | Output |
|---|---|---|---|
| 1 | Cassandra modelling + consistency (§1) | 3–4 h | Design 2 schemas from queries on paper |
| 2 | Redis Streams + cache/eviction/cluster (§2) | 2–3 h | Local consumer-group demo: kill a worker, recover with `XAUTOCLAIM` |
| 3 | Service Bus: peek-lock, DLQ, sessions, dedupe + outbox (§3, §6) | 3 h | Explain the settlement → DLQ flow aloud |
| 4 | Concurrency drills (§7.3) | 3 h | Producer–consumer + `CompletableFuture` pipeline unaided |
| 5 | K8s probes/shutdown, Workload Identity, KEDA, Helm (§4) | 3 h | Explain probes, graceful shutdown, secretless auth to Service Bus |
| 6 | Java 21 + Dropwizard (§7.1–7.2) | 2 h | Short cheat-sheet entry |
| 7 | OpenSearch basics (§5) | 1–2 h | Explain inverted index, `keyword` vs `text` |
| 8 | System-design dry run (§9) | 2 × 45 min | One prompt, timed, out loud |
| 9 | STAR stories (§8) | 2 h | 5–6 written stories |
| 10 | Coding warm-ups (§10) | 3 h | LRU, tree, DP, anagram from memory |

## Honest-gap strategy for the interview
Where you lack hands-on experience (likely Cassandra, Service Bus, Redis Streams, Helm): say so plainly, then **map to what you know** (Kafka consumer groups ↔ Redis Streams groups; Kafka partitions ↔ Cassandra partitions / Service Bus sessions; Docker Compose ↔ K8s; Spring config ↔ Dropwizard config) and **reason from first principles** about failure modes. At staff level, clear reasoning beats tool trivia.

## Sources
**Cassandra:** [guarantees](https://cassandra.apache.org/doc/latest/cassandra/architecture/guarantees.html), [storage engine](https://cassandra.apache.org/doc/latest/cassandra/architecture/storage-engine.html), [dynamo / consistency](https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html), [DDL](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/ddl.html), [DML](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html), [materialized views](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/mvs.html), [RDBMS vs Cassandra modelling](https://cassandra.apache.org/doc/latest/cassandra/developing/data-modeling/data-modeling_rdbms.html)
**Redis:** [Streams](https://redis.io/docs/latest/develop/data-types/streams/), [XAUTOCLAIM](https://redis.io/docs/latest/commands/xautoclaim/), [XNACK](https://redis.io/docs/latest/commands/xnack/), [eviction](https://redis.io/docs/latest/develop/reference/eviction/), [Redis Cluster](https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/)
**Azure:** [Service Bus quotas](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quotas), [settlement](https://learn.microsoft.com/en-us/azure/service-bus-messaging/message-transfers-locks-settlement), [dead-letter queues](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues), [sessions](https://learn.microsoft.com/en-us/azure/service-bus-messaging/message-sessions), [duplicate detection](https://learn.microsoft.com/en-us/azure/service-bus-messaging/duplicate-detection), [Java quickstart](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues), [AKS workload identity](https://learn.microsoft.com/en-us/azure/aks/workload-identity-overview)
**Kubernetes / Helm / KEDA:** [probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/), [pod lifecycle](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/), [Helm charts](https://helm.sh/docs/topics/charts/), [KEDA Service Bus scaler](https://keda.sh/docs/latest/scalers/azure-service-bus/)
**Java / others:** [JDK 21](https://openjdk.org/projects/jdk/21/), [JEP 444](https://openjdk.org/jeps/444), [JEP 491](https://openjdk.org/jeps/491), [Oracle Java 21 virtual threads guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html), [`Thread` API (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html), [`Executors` API (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html), [Dropwizard getting started](https://www.dropwizard.io/en/stable/getting-started.html), [OpenSearch Java client](https://docs.opensearch.org/latest/clients/java/)
**Interview context:** [job posting](https://bg.linkedin.com/jobs/view/staff-software-engineer-java-at-conga-4454967579) (the original `linkedin.com/jobs/view/4454967579` link redirects here). The Glassdoor and GeeksforGeeks pages below were returned by web searches and are about **other Conga roles, not this one**. I read the search-result snippets, not the full pages.
- Glassdoor: [Software Engineer](https://www.glassdoor.co.in/Interview/Conga-Software-Engineer-Interview-Questions-EI_IE544762.0,5_KO6,23.htm), [Software Developer](https://www.glassdoor.co.in/Interview/Conga-Software-Developer-Interview-Questions-EI_IE544762.0,5_KO6,24.htm), [Software Engineering](https://www.glassdoor.com/Interview/Conga-Software-Engineering-Interview-Questions-EI_IE544762.0,5_KO6,26.htm), [Senior Software Engineer](https://www.glassdoor.com/Interview/Conga-Senior-Software-Engineer-Interview-Questions-EI_IE544762.0,5_KO6,30.htm), [Associate Software Engineer](https://www.glassdoor.com/Interview/Conga-Associate-Software-Engineer-Interview-Questions-EI_IE544762.0,5_KO6,33.htm), [Conga interview questions (2026)](https://www.glassdoor.com/Interview/Conga-Interview-Questions-E544762_P6.htm)
- GeeksforGeeks: [Conga interview experience, Associate Software Engineer (on-campus)](https://www.geeksforgeeks.org/conga-interview-experience-for-associate-software-engineer-on-campus/)

**Not used / failed:** the Cassandra transactions (Accord) docs page returned 404, so §1.6 makes no claim about it.
