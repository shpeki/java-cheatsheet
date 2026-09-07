# OfficeRnD — System Design Interview Prep

Roles: Senior Backend Developer (Operations Hub, Flex product) / Senior Software Engineer (Growth Hub, e-commerce). Stack: Node.js/TypeScript, React, MongoDB, Redis, AWS. Domain: multi-tenant SaaS for coworking/flex workspace operators.

---

## 1. What this interview is actually testing

Senior-level system design rounds at a mid-size SaaS company (176 people, ~1,000 B2B customers, real production scale, not FAANG-scale) are usually testing:

- Can you take an **ambiguous, business-shaped problem** (not "design Twitter") and turn it into a concrete architecture in ~45 minutes?
- Do you **ask clarifying questions** before diving in, or do you assume?
- Can you reason about **multi-tenancy, data modeling, and consistency** — since OfficeRnD's whole product is multi-tenant (many coworking operators, many locations, many members) — rather than pure hyperscale distributed-systems trivia?
- Do you make **explicit tradeoffs** (consistency vs. availability, normalized vs. denormalized, sync vs. async) and justify them for the business context, not recite theory?
- Can you talk fluently in **their actual stack** (Node/TS, MongoDB, Redis, AWS) rather than a generic whiteboard architecture?
- Ownership signal: do you think about monitoring, failure modes, rollout, and iteration — not just the happy-path diagram?

They are *not* likely to ask you to design something at Google/Netflix scale (billions of users). Expect **domain-shaped problems**: bookings, billing, multi-location inventory, notifications, e-commerce checkout — because that's literally the product.

---

## 2. A reusable framework to run the interview

Use this structure out loud, every time, regardless of the specific prompt. Interviewers are grading your *process* as much as your final diagram.

1. **Clarify requirements (3–5 min)**
   - Functional: what must the system do? What's explicitly out of scope?
   - Non-functional: scale (how many operators/locations/members/bookings — ask, don't assume), read vs. write ratio, latency expectations, consistency requirements, multi-tenancy needs.
   - Ask about **existing constraints**: "Would this live inside the current Flex monolith/services, or as a new service?" — shows awareness they likely have an existing system to integrate with, not a greenfield.

2. **Define the API / core entities (5 min)**
   - Sketch the main resources (e.g. `Booking`, `Resource`, `Member`, `Organization/Location`, `Invoice`) and 3–4 key endpoints or events.
   - State multi-tenancy explicitly: every entity scoped by `orgId`/`locationId`.

3. **High-level architecture (10 min)**
   - Draw client → API layer → service(s) → data store(s) → async/queue → downstream (billing, notifications, integrations).
   - Call out where Redis fits (cache, rate limiting, locks, pub/sub), where a queue fits (SQS/EventBridge-style), where MongoDB fits (source of truth) vs. a relational store if you'd argue for one (e.g., for strict billing ledgers).

4. **Deep dive into the hard part (15 min)**
   - Every prompt below has one "hard part" — concurrency on a shared resource, idempotent billing, eventual consistency across services, etc. Interviewers want you to *find* that hard part yourself and go deep, not spread thin across the whole system.

5. **Data modeling detail (5 min)**
   - Since the stack is MongoDB: talk schema design, indexing, embedding vs. referencing, and how you'd model multi-tenant data (shared collection + `orgId` index vs. collection-per-tenant vs. DB-per-tenant) with real tradeoffs.

6. **Scale, failure modes, and evolution (5 min)**
   - What breaks first as they 10x? How do you monitor it? What's your rollout/migration strategy for a schema change touching live tenants?

7. **Wrap-up**
   - Summarize the design in 3 sentences, state the tradeoffs you made explicitly, and note what you'd revisit with more time/information.

**Golden rule:** narrate your thinking constantly. A senior candidate who thinks out loud and self-corrects beats one who goes silent and draws a perfect diagram.

---

## 3. Likely system design prompts (domain-specific)

These map directly to what Operations Hub and Growth Hub actually build. Prepare all of them at "could talk for 15 minutes" depth.

### A. Meeting room / desk booking system (core Flex feature — very likely)
- Core challenge: **preventing double-booking of a shared resource** under concurrent requests.
- Talk about: optimistic vs. pessimistic locking, MongoDB transactions vs. a unique compound index on `(resourceId, timeSlot)`, or a Redis-based distributed lock for a short critical section before the DB write.
- Recurring bookings, time zones across locations, buffer time between bookings, cancellation/waitlist logic.
- Read-heavy calendar view vs. write-heavy booking action — consider caching the calendar view in Redis and invalidating on write.

### B. Multi-tenant billing / subscription & invoicing system
- Core challenge: **correctness and idempotency of money movement**, auditability.
- Idempotency keys on payment operations (critical — never double-charge on retry).
- Event-driven: booking/usage events → billing service consumes asynchronously → generates invoice line items → integrates with a payment processor (Stripe-style) via webhook.
- Why you might argue for a more relational/ACID-friendly store (or Mongo multi-document transactions) for the ledger specifically, even though the rest of the system is MongoDB — show you don't apply one tool everywhere dogmatically.
- Reconciliation: what happens when a webhook is missed or arrives twice.

### C. Growth Hub e-commerce checkout (day passes, memberships, room bookings sold online)
- Core challenge: **inventory/availability check + payment + booking creation as one logical transaction across services**, plus a "frictionless, low-touch" UX (their own marketing language) meaning low latency.
- Saga-pattern style flow: reserve → charge → confirm, with compensating actions if a later step fails (e.g., release the hold if payment fails).
- Cart/session handling for anonymous users before they become "leads" (their lead-capture feature) — ties to their stated feature set.
- Rate limiting and bot protection on a public storefront (Redis token bucket).

### D. Multi-location, multi-tenant data model & analytics/reporting
- Core challenge: an operator with many locations wants aggregated occupancy/revenue analytics without slowing down operational writes.
- CQRS-ish split: operational writes to MongoDB, async projection into a read-optimized aggregate store (or precomputed rollups) for dashboards.
- Discuss tenant isolation strategies (shared collections with `orgId` indexing being the pragmatic default at this scale vs. DB-per-tenant for very large/enterprise customers) — connects to their "Enterprise Coworking Space Management" product tier.

### E. Notification system (booking confirmations, reminders, billing alerts)
- Core challenge: reliable, multi-channel (email/push/Teams — they integrate with MS Teams) delivery with retries, without duplicate sends.
- Queue-based fan-out, templating, per-tenant preferences, dead-letter queue for failed sends, idempotent send tracking.

### F. Visitor management / access control (visitor & delivery management feature)
- Core challenge: real-time state (who's currently checked in) at a physical location, syncing with door/access hardware or QR codes, potentially offline-tolerant if a location's connectivity drops.

### G. Third-party integrations layer (they explicitly value "integrations team" — seen in related job postings)
- Core challenge: many external systems (payment processors, access control hardware, calendar systems, accounting software) with different reliability/rate limits — design a resilient integration/adapter layer with retries, circuit breakers, and webhook verification.

**Prep tip:** pick 2 of these (booking + billing, or checkout + notifications) and actually sketch full architectures on paper beforehand, including a rough API contract and a MongoDB schema. Depth on 2 beats shallow familiarity with 7.

---

## 4. Data modeling deep-dive (MongoDB specifics — expect direct questions)

- **Embedding vs. referencing:** embed when data is read together and bounded (e.g., a booking's line items), reference when data is large/unbounded or shared across many parents (e.g., a `Member` referenced by many `Bookings`).
- **Schema for multi-tenancy:** default answer is a shared collection with `orgId` (and often `locationId`) as the leading field in every compound index, plus enforcing tenant scoping at the application/data-access layer (never trust a query without an explicit tenant filter). Mention the tradeoff vs. DB-per-tenant (better isolation/noisy-neighbor protection, worse operational overhead at scale) for when a customer is large enough to warrant it.
- **Indexing:** compound indexes ordered by (tenant, then filter fields, then sort field); be ready to reason about index selectivity and the classic "equality, sort, range" (ESR) rule for compound index field order.
- **Denormalization for read performance:** e.g., storing a denormalized `resourceName` on a `Booking` document to avoid a join-like lookup on every calendar render, and how you keep it in sync (event-driven update, or accept slight staleness).
- **Transactions:** MongoDB supports multi-document ACID transactions — know when you'd reach for them (e.g., booking creation + inventory decrement) vs. when eventual consistency via events is fine (e.g., updating an analytics rollup).
- **Aggregation pipeline:** be ready to describe how you'd compute e.g. "occupancy rate per location per week" — `$match` → `$group` → `$project`, and when you'd precompute this instead of querying live.

---

## 5. Redis — where it fits in every answer

- **Caching** hot reads (resource availability, dashboards) with a sensible TTL and explicit invalidation on write.
- **Distributed locks** for short critical sections (e.g., "hold this slot for 30 seconds while payment completes") via `SET NX PX` pattern.
- **Rate limiting** (token bucket / sliding window) for public APIs, especially the e-commerce storefront.
- **Pub/Sub or Streams** for lightweight real-time features (e.g., live "who's checked in" updates) — mention Redis Streams vs. a heavier queue (SQS) tradeoff: Streams for low-latency/low-durability-requirement fan-out, SQS/EventBridge for guaranteed-delivery async work like billing.

---

## 6. AWS / infrastructure talking points

- Standard managed-service architecture: API layer on ECS/EKS or Lambda, MongoDB likely via Atlas (not self-hosted, given team size), Redis via ElastiCache, async work via SQS/EventBridge/SNS, CloudFront for the storefront/static assets, S3 for file storage (documents, images).
- Multi-region is probably **not** required at their scale — be ready to say "I'd start single-region with multi-AZ for availability, and only reach for multi-region if a specific customer/compliance requirement demands it" — this is a *better* senior answer than reflexively over-engineering for global scale they don't have.
- Observability: mention structured logging, metrics (latency/error rate per endpoint), and alerting on the specific failure modes you identified (e.g., booking double-writes, missed webhooks) — ties back to their "debug production issues" and "ownership" language in the job posting.

---

## 7. Tradeoff vocabulary to use explicitly (senior signal)

Say these out loud rather than just applying them silently — interviewers are listening for the *reasoning*, not just the right answer:
- Consistency vs. availability for a given operation (e.g., booking creation should favor consistency/correctness over availability; a dashboard read can favor availability/staleness).
- Synchronous vs. asynchronous processing, and what user-facing latency budget forces the choice.
- Normalization vs. denormalization, and what you'd revisit if read patterns changed.
- Build vs. buy for a component (e.g., "I'd use Stripe/a payment processor rather than build payment handling in-house").
- Monolith vs. service boundary: given they have named teams (Operations Hub, Growth Hub) around business domains, a domain-driven service boundary answer will resonate — Growth Hub as an add-on service integrating with the core Flex system is literally how they've built it.

---

## 8. Questions to ask your interviewer

Asking sharp questions is part of a senior system design round.
- "Is this closer to a greenfield service or would I be extending an existing part of Operations Hub / Growth Hub?"
- "What's the current pain point that made this a live problem for the team?" (shows you're thinking about real prioritization, not abstract design)
- "How do you currently handle [multi-tenancy / eventual consistency / whatever came up] in the real system?" — great way to learn their actual architecture and calibrate your answer live.

---

## 9. Logistics recap (from the job postings)

- Both roles: 5+ years fullstack, Node.js/TypeScript, React, MongoDB, Redis, AWS, Sofia hybrid (min. 40% onsite/month).
- Operations Hub (Backend Developer): core Flex business logic — bookings/billing/member workflows.
- Growth Hub (Software Engineer): e-commerce add-on — checkout, lead-gen, revenue tooling for operators.
- Company: ~176 people, ~$22M ARR, ~1,000 customers, Sofia HQ + US/UK/AU presence, SOC 2/ISO 27001/GDPR certified.

---

## 10. Night-before checklist

- [ ] Sketch full architectures for **booking** and **billing/checkout** on paper, including API + MongoDB schema.
- [ ] Rehearse the 7-step framework (Section 2) out loud once, on a throwaway prompt, timed to ~45 min.
- [ ] Review MongoDB ESR indexing rule and multi-document transactions.
- [ ] Review Redis lock pattern (`SET NX PX`) and rate-limiting algorithms.
- [ ] Prepare 2–3 questions from Section 8, tailored to whichever team (Operations Hub vs. Growth Hub) you're actually interviewing with.
- [ ] Have one real "system I owned end-to-end" story ready, mapped to the ownership language in the job posting.
