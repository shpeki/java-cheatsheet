# Load Balancing

A load balancer (LB) sits in front of a pool of backend servers and distributes incoming traffic across them. It exists to solve three problems at once: keep any single server from being overwhelmed, keep the system available when a server dies, and let you scale horizontally by adding more servers behind the same address.

---

## 1. Where a load balancer sits in the stack

Traffic is usually balanced at more than one layer on the way to a backend:

1. **DNS-level (GSLB — Global Server Load Balancing)** — services like Route 53 or GeoDNS return different IP addresses per client region, spreading load across data centers/regions before a single TCP connection even opens.
2. **Layer 4 (Transport)** — routes raw TCP/UDP packets based on IP + port, without looking at payload.
3. **Layer 7 (Application)** — terminates HTTP(S) and routes based on URL path, headers, cookies, or request body.

A request in a large system typically flows: **Client → DNS/GSLB → L4 LB (or anycast VIP) → L7 LB → backend service**, though many systems skip the layers they don't need.

### L4 vs L7 — comparison

| | Layer 4 | Layer 7 |
|---|---|---|
| Operates on | IP + TCP/UDP port | HTTP method, path, headers, cookies, body |
| Speed | Very fast — no payload parsing | Slower — must parse/terminate the protocol |
| Content-based routing | No | Yes (e.g., `/api/*` → service A, `/static/*` → CDN) |
| TLS termination | Passthrough only (usually) | Can terminate TLS, inspect decrypted content |
| Protocol awareness | Protocol-agnostic (any TCP/UDP traffic) | HTTP/HTTPS/gRPC-aware |
| Resource cost | Low CPU | Higher CPU (TLS handshake, parsing) |
| Examples | AWS NLB, Linux IPVS, Maglev | AWS ALB, NGINX, HAProxy, Envoy |

**Rule of thumb for interviews:** default to L7 when you need routing intelligence (microservices, path-based routing, canary releases); use L4 when you need raw throughput and don't care about HTTP semantics (e.g., a database proxy, a game server, generic TCP traffic).

---

## 2. Load balancing algorithms

### Round Robin
Cycles through servers in a fixed order.

```
Servers: A, B, C
Requests 1-6 → A, B, C, A, B, C
```
**Real-world use:** REST API gateways for stateless microservices (e.g., a product-catalog lookup, a currency-conversion API), DNS-level balancing across identical web servers, CDN edge nodes serving static assets — anywhere requests are short and roughly equal cost.

### Weighted Round Robin
Same rotation, but servers get a weight proportional to capacity.

```
A (weight 4, 8 vCPU), B (weight 2, 4 vCPU), C (weight 1, 2 vCPU)
Sequence: A, A, B, A, C, B, A   (≈4:2:1 ratio)
```
**Real-world use:** heterogeneous fleets — e.g., rolling out newer, larger instance types alongside older ones during a migration, or weighting CDN PoPs that differ in hardware generation.

### Least Connections
Sends each new request to whichever server currently has the fewest active connections.

```
State: A=3, B=1, C=5 active → next request → B
```
**Real-world use:** WebSocket/chat servers (Slack/Discord-style, connections open for hours and load varies per user), database connection proxies (PgBouncer/ProxySQL) where query cost varies wildly, gRPC streaming/transcoding workers with long-running jobs.

### Weighted Least Connections
Divides active connections by weight before comparing, so capacity and current load both matter.

```
A: 8 active / weight 4 = 2.0
B: 3 active / weight 2 = 1.5   ← lowest, wins
C: 2 active / weight 1 = 2.0
```
This is the default (or close to it) in most production L7 LBs — NGINX, HAProxy, and Envoy all support it — because it accounts for both connection duration *and* uneven server capacity.

### Least Response Time
A variant of least connections that also factors in recent average response latency, not just connection count — used when raw connection count doesn't reflect true load (e.g., some connections are idle keep-alives).

### IP Hash / Consistent Hashing
Hashes a key (client IP, session cookie, cache key) to deterministically pick a server, so the same client keeps landing on the same backend.

```
hash(client_IP) % 3 → deterministically maps to A, B, or C
```
Plain modulo hashing reshuffles almost every key when the server count changes (`% 3` → `% 4`). **Consistent hashing** places servers and keys on a hash ring so that adding/removing one server only remaps the keys nearest to it.

**Real-world use:**
- Memcached/Redis client-side sharding (`hash(cache_key)` picks the owning node)
- CDN cache servers, so the same URL keeps hitting the same edge cache and maximizes hit rate
- Stateful session servers without a shared session store (`hash(session_cookie)`)
- Sharded stateful services in general — e.g., Discord routing users to the shard hosting their guild/channel, or multiplayer game servers routing players to the shard hosting their match

**Google's Maglev** is a well-known production implementation: instead of a simple ring, each backend gets a "preference list" over a lookup table (typically a large prime-sized table, e.g. 65537 entries), and each table slot is assigned to a backend via consistent hashing. This gives near-perfectly even load distribution and minimal disruption on backend changes, while keeping per-packet lookup O(1). It's the design behind Google's network LB and is a good name to drop in a senior-level interview.

### Random / Power of Two Choices (P2C)
Pick 2 servers at random, route to whichever has fewer active connections (rather than checking *all* servers).

```
Random picks: B and C → B has 2 active, C has 5 active → send to B
```
**Real-world use:** Envoy's default LB policy is P2C for exactly this reason — at fleets of hundreds/thousands of backends (Kubernetes ingress, large service meshes), querying every backend's load before each request doesn't scale, but sampling 2 gets nearly the same load-smoothing effect with far less coordination overhead. Documented in engineering practice at Google and Netflix.

### Picking an algorithm — quick mapping

| Traffic shape | Algorithm |
|---|---|
| Uniform, short, stateless requests | Round Robin (Weighted, if servers differ) |
| Long-lived / variable-cost connections | Least Connections (Weighted, if capacity differs) |
| Need session or cache affinity | Consistent Hashing (Maglev-style at large scale) |
| Massive scale, want to avoid centralized load tracking | Power of Two Choices |

---

## 3. Health checking

A load balancer is only as good as its view of backend health.

- **Active checks** — the LB proactively probes each backend on an interval.
  - **TCP check** — confirms the port is open, but not that the app itself works.
  - **HTTP check** — GETs an endpoint like `/health`, which can itself verify downstream dependencies (DB connectivity, cache, disk space).
  - **gRPC health check** — uses the standard gRPC health-checking protocol for gRPC services.
  - Typical parameters: 5–10s interval, 2–3s timeout, healthy/unhealthy threshold of 2 consecutive results — this "flap damping" avoids yanking a server in and out of rotation on a single blip.
- **Passive checks** — the LB observes real traffic (error rates, timeouts, latency) and ejects a backend that degrades, without a separate probe. Often combined with active checks: passive checks catch problems fast, active checks confirm recovery before adding a server back.

---

## 4. TLS/SSL termination

The LB decrypts HTTPS at the edge and (usually) forwards plain HTTP to backends inside the trusted network.

- **Benefits:** centralized certificate management (one place to rotate certs), offloads CPU-expensive crypto from application servers, and lets an L7 LB inspect HTTP content for routing.
- **TLS passthrough** — the LB forwards encrypted traffic untouched (common at L4); backends terminate TLS themselves. Used when end-to-end encryption is required even inside the data center, or when the LB shouldn't see plaintext.
- **TLS re-encryption** — the LB terminates the client connection, then opens a *new* TLS connection to the backend. Used in zero-trust/compliance-sensitive environments where internal traffic must stay encrypted.

---

## 5. Making the load balancer itself highly available

The LB cannot become a single point of failure — this is the part interview candidates most often forget.

- **VRRP + Keepalived** — a classic pattern: two or more LB instances share a Virtual IP (VIP); Keepalived runs VRRP to elect a master, and if it dies, a backup takes over the VIP within seconds. Simple, battle-tested, widely used with HAProxy/NGINX pairs.
- **Anycast** — the same IP is announced from multiple locations via BGP; the network routes each client to the nearest/healthiest instance, and failover is handled by routing rather than a VIP handoff. Used by large-scale/global providers (Cloudflare, major CDNs).
- **Active-passive vs active-active** — active-passive keeps a hot standby idle until failover (simpler, wastes capacity); active-active runs multiple LB instances concurrently behind DNS/anycast/ECMP (better utilization, needs care around shared state).
- **Shared state, not local state** — if the LB needs sticky-session or rate-limit state, keep it in a shared store (e.g., Redis) rather than in one instance's memory, or use consistent hashing so state doesn't need to be shared at all.

---

## 6. Sticky sessions

When a backend holds in-memory state (an older-style app server, a WebSocket connection, a game session), you need repeat requests from the same client to land on the same backend:

- **Cookie-based affinity** — the LB sets/reads a cookie identifying the chosen backend. Preferred in practice because it works across NATs (many clients sharing one IP won't collide) and proxies.
- **IP-hash affinity** — hash the client IP to a backend. Simple, but breaks down behind NAT/shared IPs and on IP changes (mobile clients).
- **Consistent hashing** — see above; the most scalable way to get affinity without a centralized session table.

Where possible, the better architectural fix is to make backends stateless and move session state to a shared store (Redis, a database) — this removes the need for stickiness entirely and simplifies scaling and failover.

---

## 7. Real-world tools by layer

| Layer | Examples |
|---|---|
| DNS/GSLB | AWS Route 53, Cloudflare Load Balancing, GeoDNS |
| L4 | AWS Network Load Balancer (NLB), Linux IPVS, Google Maglev |
| L7 | AWS Application Load Balancer (ALB), NGINX, HAProxy, Envoy |
| Service mesh sidecar | Envoy (as used by Istio), Linkerd |

**NGINX** — event-driven, handles thousands of concurrent connections efficiently; common as an L7 reverse proxy/LB.
**HAProxy** — excels at very high-throughput L4/L7 balancing; a common pairing with Keepalived for HA.
**Envoy** — modern L7 proxy built for microservices/service meshes; defaults to Power of Two Choices, has rich observability and dynamic service discovery integration.

---

## 8. Other topics worth raising in a senior-level interview

- **Rate limiting / circuit breaking** at the LB layer, to protect backends from overload or abusive clients.
- **Service discovery integration** — how the LB learns about new/removed backends: static config vs. dynamic registration via Consul/etcd/Kubernetes Endpoints/EndpointSlices.
- **Observability** — track per-backend latency percentiles (p50/p95/p99), error rates, and active connection counts; this is also what feeds passive health checks.
- **Global load balancing** — beyond a single data center, GSLB combines DNS/anycast with per-region L4/L7 LBs to route users to the nearest healthy region and drain traffic away from a failing one.

---

## 9. How to structure the answer in an interview

1. **Clarify scope** — L4 or L7? Expected scale? Need for stickiness, TLS termination, rate limiting?
2. **State core responsibilities** — distribute load, track health, avoid being a SPOF.
3. **Pick an algorithm and justify it** against the traffic shape described above.
4. **Describe the request path** through DNS/GSLB → L4 → L7 → backend, only including the layers the requirements call for.
5. **Address the LB's own HA** — VRRP/Keepalived or anycast, shared vs. hash-based state.
6. **Mention extras** as depth signals — sticky sessions, rate limiting, service discovery, observability.
7. **Wrap up** — summarize, note what changes at 10x scale, and name a real tool only at the end, as a concrete instantiation of the design rather than the answer itself.

---

## Sources

- [System Design: Load Balancer Architecture — L4 vs L7, Nginx, HAProxy, Algorithms, Health Checks, SSL Termination](https://www.techinterview.org/post/3233474140/system-design-load-balancer-architecture-l4-l7-nginx-haproxy-round-robin-least-connections-health-checks-ssl-termination/)
- [Load Balancer Architecture: L4 vs L7 and Routing — Sujeet Jaiswal](https://sujeet.pro/articles/system-design/system-design-fundamentals/load-balancers-architecture.html)
- [Load balancing: system design interview concepts — IGotAnOffer](https://igotanoffer.com/blogs/tech/load-balancing-system-design-interview)
- [How to use Keepalived for high availability and load balancing — Pen Test Partners](https://www.pentestpartners.com/security-blog/how-to-use-keepalived-for-high-availability-and-load-balancing/)
- [The Architect's Guide to High Availability Load Balancing — Prepare.sh](https://prepare.sh/articles/the-architects-guide-to-high-availability-load-balancing)
- [Maglev: A Fast and Reliable Software Network Load Balancer — Google Research (USENIX NSDI '16)](https://research.google/pubs/maglev-a-fast-and-reliable-software-network-load-balancer/)
- [Deep Dive into Maglev, Google's Load Balancer](https://medium.com/swlh/deep-dive-into-maglev-googles-load-balancer-f5fa943d578c)
