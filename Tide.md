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

## 6. Engineering Management / Leadership Prep

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

## 7. Logistics & Comp Notes
- Salary range: €68,400–€101,250 gross/year, open to negotiation based on experience
- Benefits: 25 days annual leave, 3 paid volunteering/L&D days, extended parental leave, €500/yr L&D budget, health & dental insurance, wellbeing platform, Multisport card, food vouchers, WFH equipment allowance, flexible remote work, sabbatical leave, share options
- Work model: hybrid — remote supported, but in-person gatherings encouraged (Sofia office/tech hub)

---

## 8. Day-Before Checklist
- [ ] Re-read this doc, focus on the tech stack table and DORA definitions
- [ ] Review the Banking (UK & EU) section — ClearBank/PPT structure, FSCS, FPS/BACS/CHAPS, SEPA, PSD2
- [ ] Rehearse 3 STAR stories out loud (timed to ~2 min each)
- [ ] Do one practice system design (pick the ledger or invoicing prompt above) with pen and paper, 30 min
- [ ] Do one cold code-review practice on a Java/Spring Boot snippet, 20 min
- [ ] Write down your 4–5 questions for them
- [ ] Re-read the job posting once more for exact phrasing to mirror in your answers
