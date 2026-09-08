# OfficeRnD Tech Stack — with Java Equivalents

Everything below is grouped by layer. "Confirmed" = explicitly named in the two job postings; "Likely/typical" = not stated, but standard for this exact stack (Node/TS/React/Mongo/Redis) and worth knowing about, not worth assuming as fact in the interview.

---

## 1. Language & Runtime

| OfficeRnD (Node/JS) | Java Equivalent | Description |
|---|---|---|
| **JavaScript** | Java (the language) | Dynamically-typed, interpreted/JIT-compiled (V8) scripting language. Unlike Java, no compile step required to run it — but see TypeScript below, which reintroduces one. |
| **TypeScript** *(confirmed)* | Java itself, conceptually | A superset of JavaScript that adds static typing, interfaces, generics. Compiles ("transpiles") down to plain JS before running. This is your closest mental anchor to Java — structural typing rather than Java's nominal typing is the main mindset shift (an object "fits" a TS interface just by having the right shape, no `implements` keyword needed). |
| **Node.js** *(confirmed)* | JVM | The runtime that executes JS/compiled-TS outside a browser. Built on Google's V8 engine + libuv (gives it its event-loop, non-blocking I/O model). Single-threaded event loop vs. the JVM's real OS threads — this is the single biggest architectural mindset shift coming from Java. |

---

## 2. Backend Framework

| OfficeRnD (Node/JS) | Java Equivalent | Description |
|---|---|---|
| **Express** *(confirmed, acceptable option)* | Raw Servlets / a minimal framework like Javalin or Spark | Minimal, unopinionated HTTP framework. No built-in DI, no enforced structure — you wire routing, middleware, and everything else yourself. Much thinner than Spring. |
| **NestJS** *(confirmed, acceptable option)* | **Spring / Spring Boot** — closest architectural cousin | Explicitly modeled on Spring's philosophy: decorators (`@Controller`, `@Injectable`, `@Module`) mirror Spring annotations, has a real DI container, structured modules, guards (≈ Spring Security filters/interceptors), pipes (≈ validation/binding). If OfficeRnD uses NestJS, this will feel the most immediately familiar to you of anything in their stack. |
| REST APIs / distributed systems design *(confirmed requirement)* | Same concept, language-agnostic | No stack-specific equivalent needed — this is the same skill you already have from the Lounge facade/aggregator services. |

---

## 3. Frontend

| OfficeRnD (Node/JS) | Java Equivalent | Description |
|---|---|---|
| **React** *(confirmed)* | No direct backend-Java equivalent — closest conceptual cousin is a component-based UI toolkit like JavaFX/Vaadin, but for the web | Frontend library (not a full framework) for building UI as a tree of reusable components. Manages re-rendering via a virtual DOM diff when state changes. Solves a different problem than anything in Spring — it's UI rendering, not backend architecture. |
| **Redux / RTK Query** *(confirmed)* | Roughly: an in-memory application-state cache + a service layer, combined | Redux = centralized, predictable state container for the frontend app. RTK Query (part of Redux Toolkit) = data-fetching/caching layer, conceptually similar to what a typed HTTP client + local cache would give you server-side. |
| **React Query** *(confirmed)* | No close Java equivalent | Server-state caching/synchronization library for the frontend — handles refetching, caching, and stale-data invalidation for API calls from React components. |
| **React Hook Form** *(confirmed)* | No Java equivalent (frontend-only concern) | Form state/validation management for React forms. |

---

## 4. Databases & Caching

| OfficeRnD (Node/JS) | Java Equivalent | Description |
|---|---|---|
| **MongoDB** *(confirmed)* | Comparable to using **Spring Data MongoDB** in the Java world, or conceptually the NoSQL counterpart to what Hibernate/JPA does for relational DBs | Document-oriented NoSQL database — BSON documents map directly to JS/TS objects with no ORM translation layer, which is a big part of why it pairs naturally with Node. You'd use the MongoDB Node.js driver or an ODM like Mongoose. |
| **Redis** *(confirmed)* | **Same technology, same tool** — you already know this one | Not stack-specific at all — Redis is language-agnostic and you already use it via a Redis client on the Lounge project. Same use cases apply: caching, TTL tokens, distributed locks, rate limiting. The only difference is the client library (`ioredis`/`node-redis` instead of Jedis/Lettuce) and writing Lua scripts the same way either side. |

---

## 5. Auth / Security

| OfficeRnD (Node/JS, typical for this stack) | Java Equivalent (what you've used) | Description |
|---|---|---|
| **JWT** *(likely/typical, not confirmed)* | Same concept as your JWT RS256 work on Lounge | Token-based auth. Node side typically uses the `jsonwebtoken` library vs. Java's `jjwt`/Nimbus — same RS256/HS256 concepts, just a different library API. |
| **Passport.js or NestJS Guards** *(likely/typical, not confirmed)* | **Spring Security** | Authentication/authorization middleware. NestJS Guards are the closer structural analog (decorator-based, like Spring Security annotations); Passport.js is more like a plug-in strategy pattern for auth providers (local, OAuth, JWT strategies). |
| Encryption (AES etc.) *(not confirmed — general awareness worth having)* | Your AES-256-GCM work on Lounge | Node's built-in `crypto` module covers this natively, no separate library needed — conceptually the same primitives you already used via Java's `javax.crypto`. |

---

## 6. Infrastructure / DevOps — carries over almost 1:1

These aren't language-specific, so your existing experience transfers directly regardless of what OfficeRnD actually uses:

| Concern | Your Java-world experience | Notes |
|---|---|---|
| **Cloud** | AWS deployments (Lounge) | Job postings confirm AWS preferred (Azure/GCP acceptable) — direct transfer, just a different SDK (AWS SDK for JS vs. for Java). |
| **Containers/Orchestration** | Rancher, Lens (k8s), Jib | Fully transferable — k8s doesn't care what language is inside the container. Node apps typically use a plain `Dockerfile` instead of Jib (which is Java-specific for JVM image builds), but the orchestration layer is identical. |
| **CI/CD** | Jenkins pipelines, ArgoCD, Bitbucket | Fully transferable — pipeline concepts are language-agnostic, only the build step commands change (`npm run build` instead of Maven/Gradle). |
| **DB migrations** | Flyway | Node equivalent tools exist (e.g., `migrate-mongo` for MongoDB) — same concept, versioned migration scripts. |
| **Observability** | Kibana/ELK, distributed tracing | Fully transferable — logging/tracing patterns are the same, just different client libraries emitting the logs. |
| **Messaging** | MQTT, distributed caching (Oracle Coherence) | Concepts transfer; specific tools would differ if OfficeRnD uses something like SQS/EventBridge/Kafka instead — worth asking in the interview what their actual async/messaging layer is. |

---

## 7. Testing

| OfficeRnD (Node/JS, typical) | Java Equivalent | Description |
|---|---|---|
| **Jest or Mocha/Chai** *(likely/typical, not confirmed — job posting just says "unit and integration tests")* | JUnit / Mockito | Standard JS testing frameworks — Jest is the most common default in a Node/React shop (also handles React component testing via React Testing Library). |

---

## Bottom line for the interview

The stack differences are almost entirely at the **application/framework layer** (Express/NestJS instead of Spring, React instead of a Java UI framework, TypeScript instead of Java-the-language). Everything below that — Redis, AWS, Kubernetes, CI/CD, JWT/encryption concepts, distributed systems thinking, database modeling — is either literally the same tool or the same concept with a different client library. That's the honest, accurate way to frame "how hard is this transition" if it comes up directly: framework/syntax ramp-up, not a rebuild of your fundamentals.