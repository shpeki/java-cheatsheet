# CreateFuture — System Design + Paired Live Coding (Code With Me) — Interview Prep

For: CreateFuture — Senior Software Engineer (Java), technical round
**Official format, as confirmed by CreateFuture's own interview logistics email:**
1. **Part 1 — System design + core values.**
2. **Part 2 — Live coding, paired programming**, run over **JetBrains Code With Me** (interviewer shares a link during the interview; you join via the **Toolbox App**, no IntelliJ license needed). Hiring managers are present to *support* you — framed collaboratively, not adversarially. **AI tools may be used, at the interviewer's discretion** — i.e. this is NOT "bring your own agentic coding setup" as earlier job-ad language suggested; whether/which AI tool is usable is the interviewer's call on the day, inside their shared environment.

⚠️ **This supersedes the "agentic AI on your own computer" framing from the job ad.** The job ad's "Using Agentic AI to develop and deploy solutions" describes the *role*, not necessarily this specific interview mechanic — the actual interview is a JetBrains Code With Me shared session, driven jointly with an interviewer, where AI-tool use is optional/interviewer-gated, not something you bring and drive solo. Prepare for **both**: solid unaided pair-programming ability first (that's the default), plus comfort narrating/using an AI tool if it's offered mid-session.

---

## 0. Sourcing note (read this first)

- **Confirmed (job ad, dev.bg + Greenhouse listings, cross-checked):** backend stack is Java, RESTful APIs, Kafka, AWS, CI/CD.
- **Confirmed (CreateFuture's own interview-logistics message, given directly to you):** two-part interview — (1) system design + core values, (2) live paired coding over JetBrains Code With Me, AI tools interviewer's discretion, hiring managers there to support.
- **Confirmed:** CreateFuture is the rebrand of **xDesign** (same Glassdoor entity, E1753969) — if you search further, search "xDesign" too, more reviews exist under that name.
- **Unverified / single source (you supplied it, from Glassdoor):** *"Mostly focused on scenarios in a scalable social networking platform (only a part of it, focusing on a specific problem rather than entire platform). It was followed by a coding challenge, really nice and easy. Interviewers also encourage you to do your best."* — I could not independently re-pull this Glassdoor page (blocked by anti-bot/403 on this session's tools), so treat it as a single anonymous data point, not a pattern confirmed across many reviews. Directionally it's consistent with a senior IC round: partial-system design + friendly, not brutally hard, coding round — and now corroborated in tone ("hiring managers there to support you") by CreateFuture's own logistics message.
- I could not find additional CreateFuture/xDesign-specific interview questions beyond that one Glassdoor snippet, and I could not find public candidate accounts specifically describing the Code With Me mechanic at CreateFuture (Code With Me setup logistics below are sourced from JetBrains' own product documentation, not CreateFuture-specific reports). The prep below is: (a) that data point taken seriously, (b) generic senior-Java system-design patterns most likely to map to "a specific problem inside a social platform," given their actual stack (Kafka, AWS, REST), and (c) practical setup + etiquette prep for the Code With Me paired-coding mechanic specifically.

---

## 0.5 Action items before the interview (logistics)

1. **Install JetBrains Toolbox App** now, not on the day: https://www.jetbrains.com/toolbox-app/ — it installs the **Code With Me Client** for you; no IntelliJ license required to join a shared session as a guest.
2. **Do a dry run.** Code With Me is JetBrains' real-time collaborative editing/screen-share tool (like a shared IntelliJ session) — launch Toolbox once beforehand, let it fully update, and confirm your machine can actually open the Code With Me client without install friction on interview day (corporate firewalls / MDM machines are the usual failure mode — test *before* the interview, not during it).
3. **Check audio/video/network separately** — Code With Me typically pairs with a separate call (Zoom/Meet/Teams) for voice+video while code is shared via the Code With Me link; confirm which combination CreateFuture is using if not stated.
4. **Expect to work inside their shared IntelliJ project**, not your own local repo/agentic setup — so heavy customization of your own editor environment won't transfer. Focus prep on Java fluency and communication, not tooling.
5. Since **AI tool use is "at the interviewer's discretion,"** don't assume you'll have one. Prepare to solve the live-coding problem **unaided** as the baseline case, and treat any offered AI assistance as a bonus you narrate well (see §7, still relevant if offered) rather than something you can lean on.

---

## 1. What "a specific problem in a scalable social platform" likely means

Interviewers scoping down from "design Twitter/Facebook" to *one slice* is a common senior-level technique — it tests depth over breadth and avoids a 45-minute whiteboard-the-universe exercise. Likely slices, roughly in order of how well they map to CreateFuture's actual stack (Kafka + REST APIs + AWS):

| Slice | Why it fits their stack | Core tension to design around |
|---|---|---|
| **News feed / timeline generation** | Classic Kafka use case (fan-out via event stream) | Fan-out-on-write vs fan-out-on-read; celebrity/hot-key problem |
| **Notification service** (likes, comments, follows → push/email/in-app) | Event-driven, Kafka topic per event type, REST for delivery status | At-least-once delivery, dedup, ordering, rate-limiting per user |
| **Follow/friend graph + counts** | REST API + DB design question, ORM-relevant (they call out ORM explicitly) | Read-heavy fan-out counts, eventual consistency of counters |
| **Rate limiter for API abuse** (e.g. posting, likes) | Generic but very common "specific problem" carve-out | Algorithm choice (token bucket/sliding window), distributed state (Redis) |
| **Comment/like counters at scale** | DB hot-row problem, classic senior-level probe | Write contention, approximate counts, async aggregation |
| **Direct messaging / chat delivery** | Ordering + delivery guarantees, WebSocket vs poll | Message ordering, offline delivery, read receipts |

**Prep priority:** know **news feed generation** and **notification service** cold (Kafka-flavored, matches their stack exactly), then rate limiter and follow-graph as secondary.

---

## 2. Structure for the system-design portion (use this out loud)

Since it's scoped to "a specific problem," don't waste time on a full requirements-gathering monologue for the whole platform — clarify fast, then go deep on the one slice:

1. **Clarify scope in under 2 minutes.** E.g. if given "design the news feed": *"Just to scope this — are we generating the feed, or also handling the write path (post creation)? Read-heavy or write-heavy assumption? Rough scale — are we talking millions of DAU or should I default to a generic large-scale assumption?"*
2. **State the core tension immediately.** For feed generation: *"The core trade-off here is fan-out-on-write (push) vs fan-out-on-read (pull) — I'll lean toward push for most users, pull for celebrity accounts with huge follower counts, and explain why."*
3. **High-level components, drawn/narrated.** API layer → message queue (Kafka topic per event) → fan-out worker → per-user feed cache (Redis sorted set is the standard answer) → read path.
4. **Deep-dive on the part they probe.** They will pick one component and push: "what if a celebrity has 50M followers?", "what if the queue backs up?", "how do you guarantee exactly-once?" — have a real answer, not just "we'd use Kafka."
5. **Close with trade-offs, not certainty.** *"If I had to pick one weak point in this design under real load, it'd be X — here's how I'd monitor for it."* Senior candidates are expected to self-critique.

---

## 3. Concrete design: News Feed (main prep target)

**Requirements (state these, don't assume silently):**
- Users post; followers see posts in reverse-chron (or ranked) feed
- Read-heavy (feed reads >> post writes) — assume ~100:1 or state you're assuming it
- Tolerate slight staleness (seconds) — not a real-time chat requirement

**High-level design:**
```
Post created → POST /posts (REST, writes to Posts DB, returns 201)
             → publish PostCreated event to Kafka topic "posts"
                                │
                                ▼
                     Fan-out Worker (consumer group)
                                │
              ┌─────────────────┴─────────────────┐
              ▼                                    ▼
    Regular user (few followers)          Celebrity (millions of followers)
    → push post ID into each              → DON'T fan out; mark post as
      follower's feed cache                 "celebrity post," merge at
      (Redis sorted set, score=timestamp)    read time instead

Read path: GET /feed/{userId}
  → read user's pre-computed feed (Redis ZREVRANGE, paginated)
  → merge in any celebrity posts they follow (fetched live, small set)
  → hydrate post IDs → Post DB (or cache) for full content
  → return page
```

**Key talking points to hit:**
- **Fan-out-on-write vs fan-out-on-read hybrid** — this is *the* answer interviewers want; naive full fan-out breaks on celebrity accounts (Kim Kardashian problem — one post = tens of millions of writes).
- **Kafka topic design:** partition by `userId` (author) or by a hash to preserve ordering per user's posts while spreading load; consumer group of fan-out workers scales horizontally.
- **Idempotency in the fan-out worker** — if a consumer crashes mid-fan-out and Kafka redelivers, pushing the same post ID into a Redis sorted set twice is harmless (ZADD is idempotent by member) — call this out, it's a nice detail that shows you think about failure modes, and it directly echoes your Payment Gateway idempotency prep.
- **Feed cache eviction/TTL:** Redis is not the source of truth — Posts DB is. Feed cache can be rebuilt from DB if lost; state this so you don't sound like you're treating cache as durable storage.
- **Read amplification fix:** paginate with cursor (last score seen), not offset — offset pagination degrades at scale.
- **Failure mode question they may ask:** *"Kafka consumer lag spikes, fan-out falls behind — what does the user see?"* → Answer: stale feed for seconds/minutes, not an error; that's an acceptable degradation given the requirements you stated up front. This is exactly the "fallback ≠ failure" framing from your Payment Gateway prep — reuse that vocabulary.

---

## 4. Concrete design: Notification Service (secondary prep target)

- Event sources (like, comment, follow, mention) → single Kafka topic (or per-type topics) → notification worker → fan-out to channels (push/email/in-app) via separate downstream services.
- **Dedup/rate-limit:** "User X liked your post" fired 50 times in a minute → batch/aggregate ("X and 12 others liked your post") rather than 50 separate notifications. This is the kind of "specific problem" a senior interview loves — the naive design floods users, the good design batches with a time window.
- **Delivery guarantees:** at-least-once from Kafka is fine if the notification write itself is idempotent (dedupe by event ID, same pattern as webhook dedup in your Payment Gateway doc).
- **User preferences/quiet hours:** a good candidate proactively mentions this — notification service needs a preferences lookup before sending, not just blast-and-forget.

---

## 5. Rate Limiter (have this ready as a fallback "specific problem")

- Algorithms to know cold, with trade-offs: **Fixed window** (simple, bursty at boundaries) → **Sliding window log** (accurate, memory-heavy) → **Sliding window counter** (good approximation, cheap) → **Token bucket** (allows bursts up to bucket size, industry standard — mention Stripe/AWS use this).
- Distributed state: Redis with `INCR` + `EXPIRE`, or a Lua script for atomicity (check-and-increment must be atomic or you get race conditions under concurrent requests — this is the detail that separates senior from mid-level answers).
- Where it sits: API Gateway/edge (cheap, protects backend) vs per-service (more precise, more expensive) — mention both and when you'd pick each.

---

## 6. The live-coding challenge itself

Data point: *"followed by a coding challenge, really nice and easy... interviewers encourage you to do your best."* Read as: **not adversarial, moderate difficulty, collaborative tone.** Treat it as a paired-programming exercise, not a gladiator match.

**What to actually prepare:**
- Comfortable, fast Java fundamentals: collections (`Map`/`Set` choice reasoning), Streams, basic algorithmic patterns (two-pointer, sliding window, BFS/DFS on a small graph, simple DP) — "easy" for a senior role likely means LeetCode Easy–Medium, solvable in 15-20 min with explanation.
- **Given the topic is social-platform-adjacent, plausible small coding problems:**
  - Implement a simple in-memory rate limiter (token bucket) — directly reusable from §5
  - Dedup/aggregate a stream of "like" events by user+post within a time window
  - Given a list of (follower, followee) pairs, compute mutual friends / friend-of-friend suggestions
  - Merge k sorted feeds into one sorted feed (classic merge-k-sorted-lists, feed-flavored)
  - LRU cache for a feed cache layer
- Practice narrating your thinking *before* typing — in a real pairing session with a hiring manager present, thinking out loud IS the deliverable, not just the final code (see §7).

### 6.1 What kind of exercise this is most likely to actually be

I could not find any CreateFuture/xDesign-specific report describing the exercise content itself — only the one Glassdoor line above. So this is **inference, not confirmed fact**, based on one strong signal: **CreateFuture is a client-services consultancy**, not a product company. Their engineers spend the bulk of their time extending and fixing *other people's* codebases under client pressure, not writing novel algorithms from scratch. Pair-programming rounds at consultancies very rarely look like LeetCode; they usually look like one of these three shapes, roughly in likelihood order:

1. **A small kata done test-first (TDD)** — e.g., build up a class method-by-method, writing a failing test, making it pass, refactoring, repeat. Tests real senior signals: do you write tests before or instead of code, do you take small verifiable steps, do you name things well under time pressure.
2. **Extend/fix a small pre-existing codebase** they hand you in the shared session — add a feature or fix a bug in a few unfamiliar classes. Tests how you read code you didn't write and communicate while orienting yourself — the single most consultancy-relevant skill there is.
3. **A scoped-down implementation of the system-design topic** from Part 1 (e.g. if you discussed feed fan-out, the coding half might be "implement the dedup/idempotency check for the fan-out worker").

**Preparation implication:** don't spend your prep time hunting for "the" algorithm puzzle. Spend it rehearsing the *process* — tests-first narration and comfortable unfamiliar-code navigation — since that transfers across all three shapes. §6.2 below is built for exactly that.

- **Code With Me specifics to expect:** it's a real-time shared IntelliJ session — the interviewer (or you) can type in the same file simultaneously, there's a shared cursor/selection, and typically a "follow" mode. Don't be thrown if the interviewer jumps in to type a line or nudge your cursor — that's normal pairing etiquette in this tool, not a takeover.

### 6.2 Kata practice — rehearse the process, not a specific puzzle

Run these **in your own IDE, out loud, on a timer**, before the interview. The goal isn't to memorize a solution — it's to build the muscle memory of narrating a TDD loop fluently under mild time pressure, so it's automatic instead of effortful during the real thing. Do at least 2-3 of these across a few days, not all in one sitting.

**How to run each one (the actual rehearsal, more important than the katas themselves):**
1. Say the requirement back in your own words before writing anything.
2. Write the **simplest failing test first** — say it out loud as you type it ("I'll start with the empty case...").
3. Write the minimum code to pass it. Resist the urge to solve the whole problem in one leap — that's what "not a gladiator match" means in practice.
4. Add the next test case, say why you picked it (edge case? boundary? the next obvious behavior?).
5. Refactor once you have 2-3 passing tests, narrating what you're cleaning up and why.
6. At the end, state the final complexity/trade-offs unprompted — don't wait to be asked.

**Kata 1 — FizzBuzz, but do it properly (10 min, warm-up only)**
Don't skip this one because it's "too easy" — the point is purely to rehearse the TDD rhythm (test → code → test → code) without the problem itself taking any brainpower, so you can focus entirely on narration mechanics.

**Kata 2 — String/rule parser (15-20 min, closest to "extend existing code")**
Build a simple `Calculator` or `DiscountRuleEngine` class incrementally: start with "returns 0 for empty input," then "adds two numbers," then "handles more than two operands," then "handles an invalid/malformed input gracefully." This shape — small class, growing via tests, one new behavior at a time — mirrors kata-shape #1 in §6.1 almost exactly.

**Kata 3 — Extend unfamiliar code (15-20 min, rehearses shape #2 in §6.1)**
Pick any small existing class from one of your own past projects (or write a throwaway 40-line class with 2-3 methods and no comments) and, **without re-reading it first**, narrate out loud as you figure out what it does, then add one new method or fix one deliberately-introduced bug. This specifically rehearses "orient yourself in code you didn't write while talking," which is the hardest part to fake live if you haven't practiced it.

**Kata 4 — One from your own system-design prep (10-15 min, rehearses shape #3 in §6.1)**
Implement just the dedup/idempotency check from §3/§4 in isolation (e.g. "given a list of like-events with timestamps, batch events for the same user+post within a 60-second window into one"). Small, testable, and directly reuses vocabulary you'll already be using in Part 1 — a nice link if the interviewer scopes the coding half off the design discussion.

**What "good" looks like across all four:** you talk more than you type in silence, each test you add has a one-sentence reason attached, and you flag your own next step before doing it ("next I want to handle the null case") rather than the interviewer having to ask what you're doing.

---

## 7. Pairing etiquette for Code With Me, and how to handle AI if it's offered

This round is explicitly **paired programming with a hiring manager present to support you**, not a solo timed test — the framing is closer to "day in the life" than "gauntlet." **AI tool use is at the interviewer's discretion**, meaning don't assume you'll have one open, and don't ask to bring your own agentic setup unless invited.

**Baseline prep (assume no AI tool):**
- Be fast and fluent in plain Java without assistance: collections, Streams, common patterns (two-pointer, sliding window, BFS/DFS, simple DP). "Easy" for a senior round likely means LeetCode Easy–Medium, solvable in 15–20 min *with* clear narration.
- **Think out loud continuously** — state your plan before typing, name the data structure you're reaching for and why, call out edge cases as you spot them. In a pair-programming format with an observer, silent typing reads as a weaker signal than a slower but narrated solution.
- **Treat the interviewer as a collaborator, not a grader** — ask clarifying questions naturally ("should this handle concurrent access?", "do I need to worry about nulls here?"), and if they offer a suggestion or jump in to type, accept it gracefully and build on it rather than defending your original approach.

**If an AI tool IS made available mid-session:**
- **You drive, the tool assists** — narrate your plan first, prompt for a specific scoped piece, then *read and critique* the output out loud before accepting it.
- **Catch and fix its mistakes visibly** — don't silently accept a subtly wrong suggestion; catching an edge-case bug it introduced is a stronger signal than never using it.
- **State trade-offs the tool doesn't surface** — e.g. "this is O(1) average with a HashMap but I'd flag thread-safety if this needed to be concurrent."
- **Prompting itself is being watched as a skill** — tight, scoped prompts beat vague ones; iterate rather than re-prompt from scratch.
- Since you don't control whether this happens or which tool, don't over-invest rehearsal time here — spend the bulk of your prep on the unaided baseline (above), and keep this as a "if offered, don't fumble it" mental checklist.

**Suggested rehearsal exercise (do this before the interview):** Pick one of the coding problems in §6 and do a timed 20-minute mock with a friend/rubber duck over screen-share (ideally via Code With Me itself, since you'll have it installed anyway per §0.5) where you talk through the whole solution out loud as if being observed — plan, edge cases, complexity — without silent typing gaps. That's the actual skill being tested here, independent of whether AI shows up.

---

## 8. Questions to ask them (shows seniority + genuine interest in the AI-native angle)

- "How does the team decide when agentic AI writes a first draft vs when someone free-hands it — is there a house style for reviewing AI-generated PRs differently from human ones?"
- "For the Kafka-based services on client projects — what's a recent example of a scaling problem the team actually hit (not hypothetical)?"
- "The job mentions integrating with third-party APIs like payment gateways — is that pattern common across most client engagements, or specific to one account?"

---

## 9. Quick-reference cheat sheet

- **Scoped system design ≠ full platform** — clarify fast, spend your time on the one slice's core tension
- **Feed generation:** fan-out-on-write for normal users, fan-out-on-read for celebrities (hybrid) — the answer they want
- **Notifications:** batch/aggregate rapid-fire events, dedupe by event ID (idempotent), respect user preferences
- **Rate limiter:** token bucket is the default good answer; atomicity of check-and-increment is the senior-level detail
- **Coding round:** "easy," collaborative tone, run over **JetBrains Code With Me** — optimize for clear narration over raw speed, hiring managers are there to support you
- **Install & dry-run JetBrains Toolbox (Code With Me client) before interview day** — don't learn the tool live
- **AI tool use is interviewer's discretion, not guaranteed** — prep the unaided baseline first; if AI appears, drive it, critique its output out loud, don't silently accept
- **Reuse your Payment Gateway vocabulary:** idempotency, "fallback ≠ failure," retry/backoff — these concepts map directly onto feed fan-out and notification delivery too

---

*Prepared for: CreateFuture Senior Software Engineer (Java) — Part 1 system design + core values, Part 2 paired live coding via JetBrains Code With Me. Cross-reference with `createfuture-Payment Gateway.md` for the resilience/integration round if both come up.*
