# Tide — Engineering Manager Interview Prep

**Role:** Engineering Manager, Sofia, Bulgaria · Full-time · Mid-Senior level
**Company:** Tide — SME (Small & Medium Enterprise) business banking fintech
**Salary range:** €68,400–€101,250 gross/year (negotiable)

---

## 1. Company & Role Context

### About Tide
- Fintech offering business banking + admin tools for SMEs: accounts, payments, invoicing, accounting
- 2M+ members across UK, India, Germany, France
- 2,800+ employees; offices in London, Sofia, Serbia, Romania, Lithuania, Hyderabad, Berlin, Paris, Luxembourg
- $300M+ raised in funding

### The role in one line
Directly manage a cross-functional, full-stack team of **8–12 engineers** owning a product domain end-to-end, paired with a Product Manager ("Product Engineering Duo"). Hands-on split between coding, design, and coaching — not a pure people-manager role.

### Core responsibilities
- Lead & directly manage 8–12 engineers
- Partner with a PM to align product and engineering priorities
- Own delivery: quality, stability, timelines, cross-team dependencies
- Own technical excellence & architecture within the team's domain
- Drive continuous improvement measured by **DORA metrics**
- Ensure engineer growth and wellbeing
- Stay hands-on — split time between design, coding, coaching; be the go-to person for hard problems

### Requirements to map your experience against
- 10+ years server-side dev (Python, Java, C#, or similar)
- Backend framework experience (Spring/Spring Boot)
- Microservices + RESTful APIs
- Cloud-native, scalable, reliable systems
- Secure, well-tested, well-documented code; full lifecycle ownership
- Third-party/partner integrations
- Relational databases
- Agile practices
- Communicating technical concepts to non-technical stakeholders
- CI/CD in daily workflow
- **Direct people management experience** (not just tech lead)

### Tech stack (know this cold — expect it to come up)
| Layer | Tools |
|---|---|
| Backend | Python, Java, Spring Boot, JOOQ |
| Messaging / events | SNS + SQS, Kafka |
| Data stores | PostgreSQL (RDS / Aurora), heavy AWS usage |
| Infra | Docker, Terraform, ArgoCD, EKS |
| Observability | DataDog, automated SLOs |
| CI/CD & quality | GitHub, Semgrep, strong test culture (new joiners ship to prod in week 1) |
| Frontend / mobile | Angular (web), Flutter (mobile), some Swift/Kotlin |
| Data/BI | Fivetran, BigQuery, Looker |
| Risk/decisioning | Python, FastAPI, Faust, some ML |

---

## 2. Interview Process (from public candidate reports — may vary for EM level)

Reports are mostly from Senior Software Engineer interviews; expect an EM-track process to add a people-leadership round on top of this technical backbone.

1. **Recruiter screen** (~30 min) — background, motivation, logistics
2. **Technical / code-review round** (~1 hr) — review a medium-sized Java code snippet shared via Google Docs, write comments on issues/improvements (~40 min), then discuss with interviewers (~20 min)
3. **Manager / system design round** — architecture & system design discussion (~40 min) + candidate questions (~20 min)
4. **(Likely for EM)** Leadership/people-management round — delivery stories, conflict handling, hiring, metrics-driven management
5. **(Likely for EM)** Final/exec round — culture fit, seniority calibration, comp discussion

Reported topics that come up: authentication & authorization, distributed locks & concurrency, event-driven architecture, code quality/best practices.
Overall timeline reported: ~3–4 weeks, fully remote.

**Caveat:** Could not confirm an EM-specific breakdown from public sources — treat the above as your best-available baseline and confirm exact stages with your recruiter once scheduled.

---

## 3. Technical Prep

### 3.0 Distributed Systems Fundamentals — the foundation under everything else in this section

**Definition:** a set of independent computers that work together and appear to users as a single coherent system, communicating and coordinating only by passing messages over a network — no shared memory, no shared clock.

**Why it's fundamentally harder than a single machine:**
- **No shared clock** — nodes can't perfectly agree on "what happened first," which is why global ordering across machines is hard (this is exactly why Kafka only guarantees order per-partition, not globally)
- **Partial failure** — one node can crash while others keep running; the system must keep working or fail gracefully, not go down as a unit
- **Network unreliability** — messages can be delayed, dropped, duplicated, or reordered; you can't always distinguish "slow" from "dead"
- **No shared memory** — nodes only know what they're told via messages, so keeping data consistent takes real coordination protocols, not a shared variable read

**Core concepts that everything else in this section builds on:**
- **Consistency models** — strong consistency (everyone sees the same data instantly) vs. eventual consistency (nodes converge over time); this is CAP theorem territory — under a network partition, you must choose Consistency or Availability, not both
- **Consensus** — getting multiple nodes to agree on a value despite failures (Raft, Paxos — used internally by things like Kafka's controller election, etcd, ZooKeeper)
- **Replication** — copying data across nodes for durability/availability (Kafka's replication factor)
- **Partitioning/sharding** — splitting data across nodes for scale (Kafka's partitions)
- **Coordination & locking** — distributed locks, leader election, ensuring only one node does a critical task at a time
- **Idempotency & exactly-once semantics** — needed because networks retry/duplicate/drop messages, so operations must be safe to repeat
- **Failure detection & recovery** — heartbeats, timeouts, retries, circuit breakers

**Why this matters for Tide specifically:** a banking ledger split across microservices, multiple banking partners, and multiple countries is a textbook distributed systems problem — money must never be double-counted or lost despite machines failing and networks being unreliable. The saga pattern (3.4) exists because you can't do one ACID transaction across services on different machines; Kafka's per-partition ordering (3.5) exists because there's no global clock; exactly-once semantics matter because networks duplicate/drop messages.

**Tight definition to give if asked directly:** *"A system composed of multiple independent nodes that communicate over a network to achieve a common goal, and that has to handle partial failure, network unreliability, and the lack of a shared clock or memory as first-class concerns rather than edge cases."*

### 3.1 System Design — practice designing systems like Tide's actual domain
Practice out loud, on a whiteboard/doc, for each:
- Design a **ledger service** that records money movements for millions of SME accounts (double-entry, idempotency, auditability)
- Design an **invoicing microservice** (create/send/track invoices, third-party payment integrations, retries)
- Design an **event-driven payments pipeline** using Kafka/SNS+SQS (ordering guarantees, exactly-once vs at-least-once, dead-letter queues, replay)
- Design **reconciliation** between internal ledger and an external bank/payment processor
- Discuss **scaling a PostgreSQL-backed service** (read replicas, partitioning, connection pooling, Aurora specifics)

For each, be ready to talk about:
- API design (REST conventions, versioning, idempotency keys)
- Data modeling (normalization vs denormalization for a ledger)
- Consistency models (eventual consistency in event-driven systems, sagas vs distributed transactions)
- Failure modes & recovery (retries, circuit breakers, dead-letter queues)
- Observability (what would you monitor via DataDog/SLOs)
- Security (auth boundaries, secrets, PII handling — this is banking data)

### 3.2 Core CS/backend topics likely to be probed
- **Authentication & authorization**: OAuth2/OIDC flows, JWT, session vs token auth, role-based vs attribute-based access control
- **Distributed locks & concurrency**: optimistic vs pessimistic locking, database-level locks, distributed locks (Redis/DynamoDB-based), race conditions, idempotency
- **Event-driven architecture**: pub/sub vs queues, Kafka partitions & consumer groups, message ordering, exactly-once semantics, outbox pattern
- **Microservices patterns**: service boundaries, API gateways, service discovery, saga pattern for distributed transactions
- **Database fundamentals**: ACID, transactions, indexing, N+1 query problems, migrations at scale
- **CI/CD & testing**: test pyramid, contract testing for microservices, blue/green & canary deploys, feature flags

### 3.3 Code review exercise prep
- Practice reviewing a Java (Spring Boot) snippet cold: look for — null safety, exception handling, SOLID violations, missing tests, security issues (SQL injection, secrets in code), performance (N+1 queries, unnecessary loops), readability/naming, missing logging/observability hooks
- Be ready to explain *why* each comment matters, not just flag it — they're evaluating your reasoning and communication as much as the catch

### 3.3b Semgrep — static analysis, and how it compares to SonarQube (EGT's tool)

**What Semgrep is**: a static analysis tool (SAST — Static Application Security Testing) that finds bugs, security issues, and code-quality problems by scanning source code against pattern-based rules, without running the code.
- You write (or use pre-built) rules that look like the code pattern to catch — e.g. "any place calling `exec()` with unsanitized input," "SQL built via string concatenation instead of parameterized queries," "hardcoded secrets/API keys"
- It's syntax-aware, not just text-matching — understands functions, method calls, imports, so it isn't fooled by formatting differences
- Works across many languages (Java, Python, JS, Go, etc.) with one tool
- Typically wired into the **PR/CI pipeline** — blocks or flags a PR automatically before merge, rather than relying on a human reviewer to remember every rule

**How it compares to SonarQube**: both are static analysis tools that run in CI and both catch security vulnerabilities, code smells, and quality issues — conceptually the same job. The differences are more about approach and depth:
- **SonarQube** is broader by default — out of the box it covers code quality metrics (complexity, duplication, maintainability "technical debt" scoring), test coverage integration, and a large built-in rule catalog per language, with a dashboard/quality-gate model (a PR can be blocked if it drops below a quality threshold)
- **Semgrep** leans more toward being lightweight and highly customizable for **security-specific, custom pattern rules** — teams often like it because you can write a very specific rule fast (e.g. "never call this internal deprecated wrapper") and it runs quickly in CI without needing a persistent server/dashboard the way SonarQube typically does
- In practice, some orgs run both: SonarQube for overall code-quality gates, Semgrep for fast, custom security-pattern checks — they're not mutually exclusive

**Why this combination (Semgrep + strong test culture) matters at Tide**: it signals a "move fast without breaking things" culture — new hires can ship to production within their first week because automated gates (Semgrep + tests) catch the well-known classes of mistakes before a human even reviews the diff, rather than relying purely on senior engineers catching everything manually.

**How to answer if asked**: "At EGT we used SonarQube for static analysis — I understand Semgrep serves a similar purpose but is more lightweight and rule-driven, well suited for fast, custom security checks in CI. The underlying goal — catching quality/security issues automatically before human review — is the same, just a different tool for it."

### 3.4 Saga Pattern — likely to come up given their event-driven stack

**What it is:** a way to manage data consistency across multiple microservices/databases without a distributed (two-phase-commit) transaction. A business operation is broken into a sequence of local transactions, one per service; each commits locally and publishes an event that triggers the next step.

**Failure handling:** instead of a rollback, a saga runs **compensating transactions** — steps that undo the effect of previously completed steps, in reverse order. Example: placing a payment = (1) reserve funds in the ledger → (2) call the external payment rail → (3) post the final ledger entry. If step 2 fails, a compensating transaction releases the funds reserved in step 1.

**Two implementation styles:**
- **Choreography** — services react to each other's events, no central coordinator. Simple for a few steps, hard to trace as the chain grows.
- **Orchestration** — a central orchestrator explicitly sequences each participant and drives compensation on failure. Easier to reason about/monitor, adds a coordinating component.

**When to use it:** a transaction spans multiple services/databases, eventual (not strict immediate) consistency is acceptable, and you're already event-driven (Kafka/SNS+SQS — like Tide).

**When not to use it:** a single service/DB can handle the whole operation (just use a local transaction — simpler); you need strict immediate consistency; the steps aren't cleanly reversible (e.g. money already sent to an external bank rail can't be "un-sent" — compensation there means issuing a new offsetting transaction, not a true rollback, which needs careful design in a payments domain).

### 3.5 Kafka Deep Dive — partitioning, ordering, durability

**Core building blocks**
- **Topic** — a named stream of events (e.g. `payment-events`)
- **Partition** — a topic is split into 1+ partitions; each is an ordered, append-only log
- **Broker** — a Kafka server; a cluster has many, and partitions are spread across them
- **Producer** — writes messages to a topic
- **Consumer / Consumer Group** — reads messages; within a group, each partition is read by exactly one consumer, so a group parallelizes reads across partitions

**Partitioning = Kafka's version of sharding**
Same idea as sharding a database: split data horizontally to scale writes/reads across machines instead of one.
- Each message has an optional **key** + a value
- Kafka hashes the key to pick a partition: `hash(key) % number_of_partitions`
- No key → round-robin/sticky partitioning for even spread
- More partitions = more parallel consumers, but more overhead and looser ordering
- **Example for Tide's domain**: key ledger events by `account_id` → all events for one account always land in the same partition (ordered), while different accounts process in parallel across partitions

**Ordering guarantee — scoped, not global**
Kafka guarantees order **only within a single partition**, never across partitions of a topic.
- Same key → same partition → strictly ordered
- Different keys → likely different partitions → no ordering guarantee between them
- A 1-partition topic is fully ordered but loses all parallelism — a real tradeoff
- This is why key choice matters: group what must stay ordered relative to each other (e.g. one account's event chain); independent entities (different accounts) don't need cross-ordering

**Durability — guaranteed, but only if configured correctly**
Kafka is a distributed, replicated commit log — written to disk, not just memory.
- **Replication factor** — each partition is copied across N brokers (commonly 3): one "leader," others "followers"
- **`acks` on the producer** — the key durability lever:
  - `acks=0` — no confirmation waited for → fastest, but messages can be silently lost
  - `acks=1` — waits for the leader only → lost if the leader dies before followers replicate
  - `acks=all`/`-1` — waits for the leader + in-sync replicas → survives leader loss, strongest guarantee
- **`min.insync.replicas`** — pairs with `acks=all`: requires N replicas to have the data before the write counts as successful, or the producer gets an error instead of a false "success"
- **Combine `acks=all` + `min.insync.replicas≥2` + `replication.factor=3`** → message survives the loss of any one broker
- **Retention** — Kafka keeps messages for a configured time/size window (not "until consumed"), enabling replay; **log compaction** mode keeps only the latest value per key forever (good for "current state" topics)

**Delivery semantics — a deliberate configuration choice, not automatic**
- **At-most-once** — never duplicated, but can be lost (commit offset before processing; crash mid-processing = message gone)
- **At-least-once** — never lost, but can be duplicated (commit offset after processing; crash after processing but before commit = reprocessed on restart) — most common default
- **Exactly-once (EOS)** — via **idempotent producers** (dedupe retried writes with sequence numbers) + the **transactional API** (atomically write to multiple partitions/topics and commit consumer offsets as one unit)

**Interview-ready summary**: "Kafka guarantees order per-partition, not globally, and guarantees no message loss *if* configured with `acks=all`, adequate replication, and `min.insync.replicas` — but at-least-once vs exactly-once is a deliberate configuration choice. For a financial ledger, I'd want exactly-once semantics via idempotent producers + Kafka transactions, keyed by account ID for per-account ordering."

### 3.6 Observability — DataDog vs. Grafana, and Automated SLOs

**DataDog vs. a Grafana-based stack (like EGT's)**
- **Grafana is a visualization layer only** — it doesn't collect or store metrics itself; it queries a separate backend (Prometheus, InfluxDB, Loki, etc.) and renders dashboards from that data
- **DataDog is the whole pipeline in one product** — its own lightweight agent runs on each host/container and collects metrics, logs, and traces itself, stores them, and provides dashboards, alerting, and APM (distributed tracing) all under one roof — less "glue infra" for a platform team to assemble and run
- The real difference is **buy vs. build/maintain**: DataDog is commercial/managed (pay per host/data volume); a Grafana+Prometheus(+ELK/Kibana for logs) stack is typically self-hosted, free aside from your own infra cost — not really "better," just a different tradeoff
- Conceptually: if a Grafana setup reads metrics from Prometheus and uses Kibana/ELK separately for logs, DataDog is what you get if you replaced both pipelines with one managed vendor tool

**Automated SLOs**
- **SLO = Service Level Objective** — a target for how reliable a service should be, e.g. "99.9% of payment requests succeed in under 200ms over a rolling 30 days"
- "Automated" means the monitoring tool tracks this continuously rather than someone eyeballing dashboards:
  - It calculates your **error budget** — how much unreliability is "allowed" before breaching the SLO (99.9% uptime ≈ ~43 minutes of downtime/month)
  - It auto-alerts on **burn rate** — not just "an error happened," but "at this rate, you'll breach your monthly target by Thursday"
- Both DataDog and Grafana (via its SLO/Mimir features) support this now — it's not DataDog-exclusive, just something DataDog markets heavily
- **Key contrast with a typical threshold alert**: a normal alert says "CPU > 90%" or "error rate > 5% right now" (threshold-driven). An SLO/burn-rate alert says "given your error budget, you're on track to breach your reliability target" (reliability-target-driven)

**How to answer if asked about your monitoring experience**: "At EGT I used Grafana [+ metrics source] for dashboards and Kibana/ELK for logs — I understand DataDog plays a similar role but as a managed, unified platform with built-in SLO/error-budget tracking rather than assembled from separate open-source pieces." Honest, accurate, and shows the underlying concepts transfer even though the specific tool differs.

### 3.7 jOOQ — type-safe SQL, and how it compares to an ORM

**What it is**: jOOQ (Java Object Oriented Querying) is a Java library for writing type-safe SQL queries directly in Java code, generated from your actual database schema.
- jOOQ **generates Java classes from your DB schema** (tables, columns, types) — you write queries as Java method calls that mirror the schema instead of raw SQL strings
- Conceptually: instead of `"SELECT * FROM accounts WHERE balance > 100"` as a string, you write something like `select().from(ACCOUNTS).where(ACCOUNTS.BALANCE.gt(100))`, where `ACCOUNTS`/`BALANCE` are real generated Java objects, not string literals
- If a column is renamed/removed in the DB, the code **fails to compile** rather than failing silently at runtime — compile-time safety for SQL is the core value

**vs. plain JDBC**: JDBC = raw SQL strings by hand, no compile-time checking. jOOQ generates the boilerplate and catches schema mismatches at build time.

**vs. an ORM like Hibernate/JPA (the more important comparison)**: JPA/Hibernate maps *objects* to tables and tries to hide SQL behind an entity model (you write JPQL or use entities, and the ORM decides what SQL to generate). jOOQ takes the opposite philosophy — it **embraces SQL rather than abstracting it away**: you're still thinking in SQL-like terms, just via a type-safe, fluent Java API, with full control over the exact query generated. This matters most for complex queries (joins, aggregations, window functions), where ORMs can silently generate inefficient or unpredictable SQL (classic N+1 problem).

**Why this fits Tide's stack**: Java/Spring Boot + PostgreSQL (Aurora/RDS) + jOOQ is a deliberate choice for a domain like a **financial ledger** — you want precise control over exactly what SQL runs: predictable query plans, exact transaction boundaries, no ORM "magic" issuing surprise extra queries or suboptimal joins. jOOQ gives Spring Boot services that SQL-level control while staying type-safe and testable, a sensible fit for something as correctness-critical as money movements.

**Interview-ready summary**: "jOOQ is a type-safe SQL builder generated from the schema — unlike Hibernate/JPA, it doesn't hide SQL behind an object-mapping layer, it embraces it, so you get compile-time safety plus full control over the exact query, which matters a lot for a system like a ledger where predictable, precise SQL beats ORM convenience."

---

## 4. Domain Knowledge — Accounting Basics (relevant since Tide = banking + accounting for SMEs)

You don't need to become an accountant, but fluency in this vocabulary helps you speak credibly with Product/domain experts about what your team is building.

### General Ledger (GL)
- The master record of **all** financial transactions for a business, organized by account
- Built on **double-entry bookkeeping**: every transaction has a debit and an equal, offsetting credit
- Feeds the core financial statements: balance sheet, income statement, cash flow statement
- In a fintech like Tide, think of the GL as the ultimate source of truth that every payment/transaction event must eventually and correctly post to

### Chart of Accounts (CoA)
- The structured list of every account a business uses to categorize transactions in its GL
- Typically numbered and grouped into 5 categories: **Assets, Liabilities, Equity, Revenue, Expenses**
- Acts as the "schema" for the GL — set up before any transactions are recorded
- For an SME banking product, this maps to how a small business's transactions (a client payment, a subscription expense) get auto-categorized for their bookkeeping

### Other adjacent concepts worth a 5-minute refresher each
- **Double-entry bookkeeping**: every debit has a matching credit; keeps the books balanced
- **Reconciliation**: matching internal records (your ledger) against an external source of truth (bank statement, payment processor) to catch discrepancies — a very natural engineering topic (this is basically "eventual consistency reconciliation" applied to money)
- **Accounts Payable (AP) / Accounts Receivable (AR)**: money a business owes vs. is owed
- **Idempotency in payments**: why double-charging or double-crediting must never happen — ties directly into the "distributed locks & concurrency" technical topic above

### Why this matters for the interview
Tide's engineering org exists to serve this domain reliably at scale. If asked "why Tide" or about domain interest, you can credibly connect your backend/event-driven architecture experience to real accounting integrity problems (idempotency, reconciliation, auditability) rather than speaking only in generic system-design terms.

---

## 5. Domain Knowledge — Banking (UK & EU) *(flagged by a contact at Tide as a study priority)*

Tide isn't itself a licensed bank — knowing this structure cold will make you sound credible in a domain conversation.

### How Tide is actually regulated (fact-checked, current)
- Tide is a **business financial platform**, not a bank. It's regulated by the **FCA** as an **Electronic Money Institution (EMI)** (FRN 900843), plus a separate FRN for credit/insurance broking (718743).
- Customer funds sit with banking partners, not Tide itself:
  - **ClearBank Ltd** — powers Tide's current & savings accounts. ClearBank holds the actual banking license (authorized by the **PRA** and regulated by the **FCA**+PRA). Deposits get **FSCS protection up to £120,000** per depositor because they sit with a real bank.
  - **PrePay Technologies Ltd (PPT)** — powers Tide's e-money accounts (sort code 23-69-72); funds sit in a **safeguarding account** (not FSCS-protected — safeguarding vs. deposit insurance is an important distinction).
  - Together, Tide + ClearBank branded this "ClearBank Tide Business Banking" — Tide owns the UX/product, ClearBank owns the license, compliance, and payment-scheme access.
- **Why this matters for engineering**: your team's services likely integrate with these partner banks' APIs for ledger movements, payment execution, and safeguarding — reconciliation between Tide's internal ledger and the partner bank's records is a real, concrete engineering problem here, not hypothetical.

### UK banking fundamentals to know
- **Regulators**: FCA (conduct), PRA (prudential/bank safety)
- **Payment rails**: Faster Payments Service (FPS) — near-instant transfers; BACS — batch payments (e.g. payroll, direct debits), 3-day cycle; CHAPS — same-day high-value transfers
- **Account identifiers**: sort code (6 digits, bank/branch) + account number (8 digits)
- **Deposit protection**: FSCS — protects deposits at licensed banks up to £85,000 (up to £120,000 temporarily for some cases/joint accounts) — only applies where funds sit with a real bank, not under an EMI's own safeguarding
- **Open Banking (UK)**: framework (born from a CMA order on the 9 largest UK banks, aligned with PSD2) letting regulated third parties access account data (AIS) or initiate payments (PIS) via standardized APIs, with customer consent
- **KYC/AML**: Money Laundering Regulations 2017; business banking requires Companies House checks, UBO (ultimate beneficial owner) identification, ongoing transaction monitoring

### EU banking fundamentals to know
- **Regulators**: ECB (eurozone monetary/prudential oversight), EBA (EU-wide banking standards), national regulators per member state
- **SEPA (Single Euro Payments Area)**: standardized euro transfers across ~36 countries — SEPA Credit Transfer (standard transfers) and SEPA Direct Debit (recurring collections), all IBAN-based
- **PSD2 (Payment Services Directive 2)**: EU-wide equivalent/origin of Open Banking — mandates Strong Customer Authentication (SCA, i.e. 2FA on payments) and API access for licensed Third Party Providers (TPPs)
- **E-Money Directive (EMD2)**: the legal basis for EMI licenses (what Tide itself operates under)
- **Post-Brexit note**: UK EMI/banking licenses no longer "passport" into the EU — a UK fintech operating in EU markets (Germany, France per the job posting) needs a separate EU entity/license, which is a real cross-border compliance/engineering concern (data residency, separate ledgers per jurisdiction, etc.)
- **GDPR**: overlaps with banking — strict rules on handling customer financial and personal data, relevant to any service design questions involving PII

### How to actually use this in the interview
- If asked "why Tide" or about domain interest: mention you understand Tide operates as an EMI on top of partner banks (ClearBank/PPT), and that this "platform over regulated infrastructure" model creates interesting engineering problems (ledger reconciliation across systems, safeguarding accuracy, multi-jurisdiction compliance for UK vs. EU entities)
- If a system-design prompt touches payments: mention the real rails (FPS/BACS/CHAPS for UK, SEPA for EU) rather than generic "payment processor" language — it signals you did your homework
- Don't over-invest — you're not being hired as a compliance officer. The goal is fluency, not expertise: enough to ask smart questions and not sound lost when the domain comes up.

Sources:
- [Is Tide a bank? | Tide Business](https://www.tide.co/support/joining/what-is-tide/is-tide-a-bank/)
- [How Tide and ClearBank are delivering competition and innovation in SME banking](https://clear.bank/learn/insights/how-tide-and-clearbank-are-delivering-competition-and-innovation-in-sme-banking)

---

## 6. How Tide's Architecture Is Likely Organized (researched facts + my informed hypothesis)

### Confirmed, from Tide's own postings/docs
- **Org model**: domain-aligned, full-stack teams owning products end-to-end, each an EM+PM pair; engineers self-organize, standards shared via "Communities of Practice" rather than a central architecture mandate
- **Accounts Platform team**: owns "the platform layer at the centre of Tide's financial infrastructure" — the ledger is the authoritative record for all member finances. It currently also owns account opening/lifecycle, regulatory compliance functions, and member statements.
- **The ledger is actively being carved out into its own dedicated team**, separate from the rest of Accounts Platform, specifically because it needs different characteristics: high-volume, high-criticality, strict consistency, its own SLOs/on-call. (Good, concrete thing to reference/ask about in-interview — shows you did real homework, not just generic prep.)
- **Multi-jurisdiction**: the ledger supports UK, India, Germany, and France **through different banking partners per market** (UK confirmed as ClearBank + PrePay Technologies — see Section 5)
- **Tech stack for this domain**: Java/Spring Boot microservices, AWS (Aurora PostgreSQL), Docker/Terraform, event-driven/async processing, SLO-backed monitoring (DataDog and/or Coralogix depending on team)

### My hypothesis on the likely shape (not confirmed — my own inference, useful for framing questions/answers)
- **Ledger service** — append-only, double-entry, strongly consistent, Aurora/Postgres-backed with careful transaction isolation — the "must never be wrong" core
- **Accounts/lifecycle service** — KYC/onboarding, account opening/closing/status — talks to the ledger but isn't the ledger
- **Payments services (plural, likely per rail/market)** — UK (FPS/BACS/CHAPS) vs EU (SEPA), each talking to the relevant banking-partner API and emitting events back for ledger reconciliation
- **Banking-partner integration layer** — abstracts ClearBank, PPT, and EU partner(s) so the rest of the platform doesn't hardcode per-partner logic
- **Event backbone (Kafka + SNS/SQS)** connecting all of it — e.g. "payment executed" flows from a payments service into the ledger, and out to statements/notifications/BI
- **Reconciliation as a first-class process** — continuously comparing internal ledger state against each banking partner's actual records, flagging drift (this is the saga/compensating-transaction territory from Section 3.4)
- **Compliance/regulatory services**, likely split per jurisdiction given UK vs EU regulatory divergence post-Brexit, consuming ledger/account events for AML monitoring and audit trails

**How to use this**: don't present the hypothesis as fact — frame it as "here's how I'd guess this is structured, curious how close that is" when asking your questions; it signals systems thinking without overclaiming insider knowledge.

Sources:
- [Engineering Manager - Accounts Platform | Tide](https://job-boards.greenhouse.io/tide/jobs/7780973003)
- [Is Tide a bank? | Tide Business](https://www.tide.co/support/joining/what-is-tide/is-tide-a-bank/)

---

## 7. Engineering Management / Leadership Prep

### DORA Metrics — know these cold, you'll likely be asked how you've used them
The four key DevOps Research and Assessment metrics:
1. **Deployment Frequency** — how often the team ships to production
2. **Lead Time for Changes** — time from commit to running in production
3. **Change Failure Rate** — % of deploys causing a production failure
4. **Time to Restore Service (MTTR)** — how fast the team recovers from an incident

Performance tiers: Elite / High / Medium / Low, based on combined scores.
**Prep angle:** have a concrete story of how you measured one of these on your team (e.g., Lounge project) and what you did to move the needle — process change, tooling, test coverage, smaller PRs, etc.

### STAR stories to prepare (Situation, Task, Action, Result)
Prepare 3–5 stories covering:
1. **A delivery you drove end-to-end** — scope, tradeoffs, how you handled a slipping timeline or dependency
2. **An architecture/tech decision you owned** — the alternatives considered, why you chose what you did, the consequence
3. **A people/performance situation** — underperformance, conflict between engineers, or a hard feedback conversation
4. **Improving a team metric** — DORA-style improvement, or quality/velocity gain
5. **Growing someone** — mentoring an engineer to a promotion or new skill
6. **A production incident** — how you triaged, communicated, and what changed afterward (postmortem culture)

Base these on your actual work on the **Lounge project at EGT Digital** — concrete details beat generic answers.

### Management philosophy — be ready to articulate
- How hands-on you are day-to-day (% time coding/reviewing vs. pure people management)
- How you balance team delivery pressure vs. sustainable pace/wellbeing
- How you make architectural decisions — solo, or driven by the team (Tide explicitly mentions "Communities of Practice" and engineer self-organization — signal that they value collaborative technical culture, not top-down mandates)
- How you've grown as a manager from being an IC

### Questions to ask them (shows seniority + genuine evaluation of the role)
- What are the team's current DORA metrics, and what's the biggest bottleneck today?
- How much architectural autonomy does the EM actually have day-to-day vs. platform/cloud team constraints?
- What does "hands-on" look like in practice — expected coding time?
- How is the Product Engineering Duo relationship structured — who has final call on scope vs. technical approach?
- What does career growth look like for engineers on this team, and how is that supported?
- What's the on-call/incident load like for this domain?

---

## 8. Logistics & Comp Notes
- Salary range: €68,400–€101,250 gross/year, open to negotiation based on experience
- Benefits: 25 days annual leave, 3 paid volunteering/L&D days, extended parental leave, €500/yr L&D budget, health & dental insurance, wellbeing platform, Multisport card, food vouchers, WFH equipment allowance, flexible remote work, sabbatical leave, share options
- Work model: hybrid — remote supported, but in-person gatherings encouraged (Sofia office/tech hub)

---

## 9. Day-Before Checklist
- [ ] Re-read this doc, focus on the tech stack table and DORA definitions
- [ ] Review the Banking (UK & EU) section — ClearBank/PPT structure, FSCS, FPS/BACS/CHAPS, SEPA, PSD2
- [ ] Review distributed systems fundamentals (3.0), Semgrep vs. SonarQube (3.3b), the saga pattern (3.4), Kafka deep dive (3.5), observability/DataDog vs. Grafana (3.6), jOOQ vs. Hibernate/JPA (3.7), and the architecture hypothesis (Section 6) — practice explaining each out loud
- [ ] Rehearse 3 STAR stories out loud (timed to ~2 min each)
- [ ] Do one practice system design (pick the ledger or invoicing prompt above) with pen and paper, 30 min
- [ ] Do one cold code-review practice on a Java/Spring Boot snippet, 20 min
- [ ] Write down your 4–5 questions for them
- [ ] Re-read the job posting once more for exact phrasing to mirror in your answers