# Java 25: What's New (Detailed)

JDK 25 reached General Availability on **16 September 2025** and is an LTS release from most vendors. ✅ (openjdk.org/projects/jdk/25)

**Evidence tags:** ✅ checked against the live JEP page, project page or Javadoc listed in [Sources](#sources). ⚠️ not confirmed against those pages (my knowledge or a search snippet). Code blocks are either quoted from a JEP ("JEP example") or written by me ("illustration"); compile-check illustrations before relying on them.

**Scope note.** The user's reference point is Java 21 (the previous LTS: see [`java-21-new-features.md`](java-21-new-features.md)). JDK 25 itself has **18 JEPs**. Because most teams jump 21 → 25, Part 4 also lists what arrived in 22, 23 and 24 and is final by 25.

---

## 1. The 18 JEPs at a glance

✅ Status as listed on the JDK 25 project page.

| JEP | Title | Status | Theme |
|---|---|---|---|
| 506 | Scoped Values | **Final** | Concurrency |
| 505 | Structured Concurrency | Fifth preview | Concurrency |
| 512 | Compact Source Files and Instance Main Methods | **Final** | Language/onboarding |
| 513 | Flexible Constructor Bodies | **Final** | Language |
| 511 | Module Import Declarations | **Final** | Language |
| 507 | Primitive Types in Patterns, `instanceof`, and `switch` | Third preview | Language |
| 502 | Stable Values | Preview | Language/API |
| 519 | Compact Object Headers | **Final** (product option, opt-in) | JVM/memory |
| 521 | Generational Shenandoah | **Final** (opt-in mode) | GC |
| 514 | Ahead-of-Time Command-Line Ergonomics | **Final** | Startup |
| 515 | Ahead-of-Time Method Profiling | **Final** | Startup/warm-up |
| 509 | JFR CPU-Time Profiling | Experimental (Linux) | Observability |
| 518 | JFR Cooperative Sampling | **Final** | Observability |
| 520 | JFR Method Timing & Tracing | **Final** | Observability |
| 510 | Key Derivation Function API | **Final** | Security |
| 470 | PEM Encodings of Cryptographic Objects | Preview | Security |
| 508 | Vector API | Tenth incubator | Performance |
| 503 | Remove the 32-bit x86 Port | **Final** | Platform |

**Interview headline:** for a Java backend role the items to be able to explain are **scoped values (final), compact object headers, AOT cache (startup), flexible constructor bodies, compact source files, and the JFR additions**. Structured concurrency, stable values and primitive patterns are **still preview**: know what they are, do not ship them.

---

## 2. Language and API features

### 2.1 Scoped Values (JEP 506), final

✅ Safe, efficient sharing of **immutable** data from a method to its callees and to child threads, as an alternative to `ThreadLocal`. Evolved through JEPs 429, 446, 464, 481, 487.

```java
static final ScopedValue<String> NAME = ScopedValue.newInstance();   // JEP example

where(NAME, "value").run(() -> {
    String val = NAME.get();   // bound here and in every method called from here
});
// NAME is unbound again after run() completes
```

Semantics ✅:
- **Write-once, no `set()`.** Data flows one way, caller to callees.
- **Bounded lifetime:** exists only while `run`/`call` executes; cleanup is automatic.
- **Rebinding/nesting:** a callee can rebind the value for its own callees; after the inner scope exits the outer binding is restored.
  ```java
  void foo() { where(X, "hello").run(() -> bar()); }
  void bar() {
      System.out.println(X.get());                 // hello
      where(X, "goodbye").run(() -> baz());        // baz sees "goodbye"
      System.out.println(X.get());                 // hello again
  }
  ```
- `isBound()` tests for a binding (useful for re-entrancy detection).
- **Inheritance:** child threads started by `StructuredTaskScope` read the parent's bindings directly, with no copying.
- The API takes `Runnable`/`Callable`, not `AutoCloseable`, on purpose, so bindings stay consistent even under `StackOverflowError`.

**The only API change since the previous preview:** `ScopedValue.orElse` **no longer accepts `null`**. ✅ (If you coded against the preview, search for `orElse(null)`.)

**Scoped values vs `ThreadLocal` (JEP's argument)** ✅:

| | `ThreadLocal` | `ScopedValue` |
|---|---|---|
| Mutability | Any code can `set()` | Immutable once bound |
| Lifetime | Until removed or thread dies (leak risk) | The `run` call only |
| Inheritance to children | Copies every variable | Shared, near-free |
| Cost with millions of virtual threads | High | Low |

`ThreadLocal` remains appropriate for object pooling and long-lived thread caches (stated non-goal: no forced migration).

**Use cases:** request/tenant/user context, nested transactions, recursion-depth limits, graphics contexts. **Spring relevance:** security context and MDC-style data are the natural candidates; framework integration is ⚠️ not verified.

### 2.2 Compact Source Files and Instance Main Methods (JEP 512), final

✅ Lowers the ceremony for small programs without changing the language for large ones.

```java
void main() {
    IO.println("Hello, World!");
}
```

- **Launch protocol:** prefers `main(String[])`, falls back to `main()`; for instance methods the class is instantiated through its no-argument constructor.
- **Compact source file:** top-level fields and methods with no class; the compiler wraps them in an implicit `final` class in the unnamed package.
- **`java.lang.IO`** (new): `print(Object)`, `println(Object)`, `println()`, `readln(String prompt)`, `readln()`.
- **Automatic imports:** compact files get all public classes of `java.base` (54 packages, incl. `java.util`, `java.io`, `java.math`) as if by `import module java.base`.
- **Graduation:** add an explicit class wrapper and a module import and the code is unchanged.

Lineage ✅: preview as "unnamed classes" in 21 (JEP 445), renamed "implicitly declared classes" in 22, "simple source files" in 24, "compact source files" at final. **Backend relevance: low** (scripting, teaching, quick experiments with `java Foo.java`).

### 2.3 Flexible Constructor Bodies (JEP 513), final

✅ Statements may appear **before** `super(...)`/`this(...)`. Previewed in 22 (JEP 447), 23, 24; final in 25.

Two phases ✅: **prologue** (code before the explicit constructor call) and **epilogue** (after). "The prologues run bottom-up and then the epilogues run top-down".

In the prologue ("early construction context") you **may**:
- initialize fields declared in the same class (uninitialized ones);
- validate or transform arguments, as long as the code does not reference the instance;
- use the enclosing instance in nested classes.

You **may not**: refer to `this` explicitly or implicitly, access fields/methods of the current instance, implicitly create inner-class instances that capture `this`, or use `super` members.

Illustration (my code):

```java
class PositiveRange extends Range {
    PositiveRange(int lo, int hi) {
        if (lo < 0) throw new IllegalArgumentException("lo must be >= 0");   // fail fast, before super()
        int safeHi = Math.max(lo, hi);                                       // compute a value for super()
        super(lo, safeHi);
    }
}
```

Why it matters ✅:
1. **Fail-fast validation** before running superclass constructor work.
2. **Integrity:** set subclass fields before the superclass constructor runs, so an overridden method called from the superclass constructor does not see uninitialised fields. (That was the classic "virtual call in constructor" bug.)

Special cases ✅: **records**: canonical constructors still cannot call `this/super` explicitly; non-canonical record constructors get the flexibility for `this(...)`. **Enums**: statements allowed before `this(...)`, but no `super(...)`. Fully backward compatible.

### 2.4 Module Import Declarations (JEP 511), final

✅ `import module M;` imports, on demand, all public top-level types in packages **exported** by `M`, including those from modules it `requires transitive`.

```java
import module java.base;          // replaces java.util.*, java.util.function.*, java.util.stream.*, java.io.* ...
import module java.sql;
import java.sql.Date;             // resolves the java.util.Date / java.sql.Date clash
```

- **Ambiguity resolution** ✅ by specificity: single-type imports (most specific) beat on-demand imports, which beat module imports (least specific).
- `java.se` now `requires transitive java.base`, so `import module java.se;` reaches all standard APIs.
- Works in modular and non-modular code. JShell uses it instead of its own package list.
- Final with no change from the 24 preview (JEP 494).

⚠️ Style question for interviews: many teams will avoid broad module imports in production code for readability; the feature is aimed at compact files, JShell and small tools.

### 2.5 Primitive Types in Patterns, `instanceof`, `switch` (JEP 507), third preview

✅ No changes since the second preview. Needs `--enable-preview`.

- Primitive type patterns anywhere a pattern is allowed, e.g. `map.get("age") instanceof JsonNumber(int a)` narrows a `double` to `int` only if exact.
- `instanceof` with primitives is a safe-conversion test:
  ```java
  if (i instanceof byte b) { /* b usable without a cast; i fits in a byte */ }
  ```
- `switch` accepts `long`, `float`, `double` and `boolean` selectors (previously only `byte/short/char/int`). For floating types, case constants must match the selector type exactly:
  ```java
  switch (v) { case 0f -> 5f; case float x -> 7f + x; }   // JEP example
  ```
- **Exact conversions:** *unconditionally exact* (no runtime check, e.g. `int → long`) vs *conditionally exact* (needs a check, e.g. `long → int`).

### 2.6 Stable Values (JEP 502), preview

✅ A holder for immutable data that is initialized **at most once**, lazily, thread-safely, and which the JVM can treat as a constant after initialization (like `final`, but with deferred initialization).

API surface ✅: `StableValue.of()`, `orElseSet(Supplier)`, `StableValue.supplier(Supplier)`, `StableValue.list(int, Function)` (elements initialize on demand).

Illustration (my code, preview API in 25):

```java
class OrderService {
    private final StableValue<Logger> logger = StableValue.of();
    Logger logger() { return logger.orElseSet(() -> Logger.create(OrderService.class)); }
}
```

Replaces ✅: double-checked locking with `volatile` (boilerplate and easy to get wrong), and the holder-class idiom (which only works for static fields). Motivation: break monolithic start-up by initializing components when first needed. Requires `--enable-preview`. ⚠️ The JEP lists more API (`map`, `function`) that I did not verify.

### 2.7 Structured Concurrency (JEP 505), fifth preview

✅ Redesigned API. Needs `--enable-preview`.

```java
Response handle() throws InterruptedException {              // JEP example
    try (var scope = StructuredTaskScope.open()) {
        Subtask<String>  user  = scope.fork(() -> findUser());
        Subtask<Integer> order = scope.fork(() -> fetchOrder());
        scope.join();
        return new Response(user.get(), order.get());
    }
}
```

Changes versus the 21 API ✅:
- **No public constructors.** Use the static factories `open()` and `open(Joiner)`. `open()` has the default policy: wait for all subtasks to succeed or any to fail.
- **`Joiner`** objects define completion policy: `anySuccessfulResultOrThrow()`, `allSuccessfulOrThrow()`, `awaitAll()`, `allUntil(Predicate)`.
- `fork` returns a **`Subtask`**, not a `Future`: query it only after `join()`.
- A third `open` variant takes a configuration function (thread factory, timeout, scope name for observability).
- `ShutdownOnFailure`/`ShutdownOnSuccess` from 21 are replaced by joiners. ⚠️ (inferred from the joiner list; the page does not say those two classes were removed in so many words).

### 2.8 Vector API (JEP 508), tenth incubator

✅ Incubating until the needed Project Valhalla features reach preview. Changes: `VectorShuffle` can read/write `MemorySegment`; native math libraries are linked through the FFM API instead of custom C++ in HotSpot; `Float16` operations auto-vectorize on supporting x64 CPUs.

---

## 3. JVM, startup and observability

### 3.1 Compact Object Headers (JEP 519), final

✅ Cuts the object header from **96–128 bits to 64 bits**. Introduced experimentally in 24 (JEP 450); in 25 it is a normal product option.

- Enable: `-XX:+UseCompactObjectHeaders` (the `-XX:+UnlockExperimentalVMOptions` flag is no longer needed). **It is not on by default**: you opt in.
- Reported gains ✅: SPECjbb2015 **22% less heap and 8% less CPU time** in one configuration; **15% fewer GCs** with G1 and Parallel; a parallel JSON parser ran **10% faster**. Oracle's full test suite and "hundreds of services at Amazon" on JDK 21 and 17 backports were cited.
- Four header bits are reserved for Project Valhalla; further compression of class pointers and identity hash codes is possible later (Lilliput).
- **Why it matters:** many small objects (DTOs, boxed values, map entries) means large heap savings. Test it on your own workload, ideally with allocation-heavy services such as Cassandra-driver-based ones. ⚠️ Compatibility with specific agents/native code: not verified.

### 3.2 Generational Shenandoah (JEP 521), final

✅ `-XX:+UseShenandoahGC -XX:ShenandoahGCMode=generational`. No `-XX:+UnlockExperimentalVMOptions` any more (old command lines still work). **Single-generation remains Shenandoah's default.** Experimental in 24 (JEP 404). Benchmarked on DaCapo, SPECjbb2015, SPECjvm2008, Heapothesys.

### 3.3 AOT cache: ergonomics (JEP 514) and method profiling (JEP 515), both final

Background ✅: JDK 24's JEP 483 (AOT Class Loading & Linking) caches loaded-and-linked classes. Reported at the time: Spring PetClinic 3.2.0 start-up **4.486 s → 2.604 s (42% faster)**; cache size **130 MB**. Constraints: same JDK release and architecture, identical class path (JARs only), consistent module options, no JVMTI agents that rewrite classes, built-in class loaders only; falls back gracefully if the cache is unusable.

**JEP 514, one-step creation** ✅:

```bash
# Before (JEP 483, two steps)
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.example.App
java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp app.jar

# Now (25): one step
java -XX:AOTCacheOutput=app.aot -cp app.jar com.example.App ...

# Production, unchanged
java -XX:AOTCache=app.aot -cp app.jar com.example.App ...
```

New environment variable `JDK_AOT_VM_OPTIONS` passes options only to the cache-creation sub-invocation, not the training run.

**JEP 515, method profiles in the cache** ✅: the AOT cache now stores the **method profiles** the JVM would otherwise collect early in a run (invocation counts, observed receiver types), so the JIT can compile hot methods sooner: faster start **and** faster time-to-peak. Example from the JEP: a Stream-API program 90 ms → 73 ms (**19%**) for about 250 KB extra cache (2.5%). The JVM keeps profiling in production, so it can adapt if behaviour differs from training. No code changes.

**Practical recipe for a containerised service** (illustration, ⚠️ not tested): train in CI with representative load, bake `app.aot` into the image, start with `-XX:AOTCache=app.aot`; rebuild the cache on every JDK or classpath change. Pair with Kubernetes readiness probes and HPA scale-up speed (relevant to a Conga-type AKS deployment).

### 3.4 JFR improvements

**JEP 509 CPU-Time Profiling** (experimental, Linux only) ✅
- Uses the Linux kernel CPU timer (POSIX timers, kernel 2.6.12+) to sample "the stack of every thread running Java code at fixed intervals of CPU time".
- Fixes the old execution sampler's gaps: it saw only Java code (native CPU use was missed), did not report failed samples, and sub-sampled threads.
- New event `jdk.CPUTimeSample`, off by default:
  ```bash
  java -XX:StartFlightRecording=jdk.CPUTimeSample#enabled=true,filename=profile.jfr ...
  jfr view cpu-time-hot-methods profile.jfr
  ```
- Throttle: `throttle=10ms` (one sample per 10 ms of CPU) or `throttle=500/s` (the default rate).
- Companion event `jdk.CPUTimeSamplesLost` reports dropped samples.

**JEP 518 Cooperative Sampling** ✅
- The sampler thread now only records the target's **program counter and stack pointer** in a thread-local queue and resumes it; the stack trace is **rebuilt later at the target's next safepoint**, adjusting for safepoint bias.
- Benefits: removes risky stack-parsing heuristics and crash risk, simpler code, less sampler work, better scalability.
- Limits: Java code only (native code keeps the old path); some bias remains for e.g. HotSpot intrinsics. No new flags.

**JEP 520 Method Timing & Tracing** ✅
- Two events: `jdk.MethodTiming` (invocation count with min/average/max time) and `jdk.MethodTrace` (individual executions with stack trace and duration), implemented via **bytecode instrumentation**, no source changes.
- Filter grammar: `target (";" target)*` where a target is a class, a `class::method`, a method name, or an annotation. Examples: `java.util.HashMap::resize`, `java.io.FileDescriptor::<init>`, `::<clinit>`, `@jakarta.ws.rs.GET`.
- Configure by `-XX:StartFlightRecording:method-timing=...`, config files, `jcmd`, or JMX/`RemoteRecordingStream`. View with `jfr view`.
- Not a goal to stay under the usual sub-1% overhead when instrumenting.
- **Use:** "how long does `OrderRepository.save` really take in prod?" without a code change or APM agent.

---

## 4. Security and platform

### 4.1 Key Derivation Function API (JEP 510), final

✅ `javax.crypto.KDF` (plus SPI `KDFSpi` and `HKDFParameterSpec` with Extract / Expand / ExtractThenExpand). Preview in 24 (JEP 478).

```java
KDF hkdf = KDF.getInstance("HKDF-SHA256");                     // JEP example
AlgorithmParameterSpec params =
    HKDFParameterSpec.ofExtract().addIKM(initialKeyMaterial).addSalt(salt).thenExpand(info, 32);
SecretKey key = hkdf.deriveKey("AES", params);
```

Methods: `getInstance`, `deriveKey(String alg, AlgorithmParameterSpec)`, `deriveData(AlgorithmParameterSpec)`. Supports HPKE and post-quantum transitions; enables PKCS#11 hardware integration; paves the way for Argon2 beyond PBKDF2.

### 4.2 PEM Encodings of Cryptographic Objects (JEP 470), preview

✅ Needs `--enable-preview`. Classes in `java.security`: `PEMEncoder`, `PEMDecoder`, `PEMRecord`, and the sealed interface `DEREncodable`. Handles public/private keys (RSA, EC, EdDSA), X.509 certificates, CRLs and encrypted private keys.

```java
PEMEncoder encoder = PEMEncoder.of();                           // JEP example
String pem = encoder.withEncryption(password).encodeToString(privateKey);
PEMDecoder decoder = PEMDecoder.of();
PrivateKey key = decoder.withDecryption(password).decode(pem, PrivateKey.class);
```

A second preview exists as JEP 524 ✅ (named in the JEP 470 page; it is not part of JDK 25 itself).

### 4.3 Remove the 32-bit x86 Port (JEP 503)

✅ Removes the 32-bit x86 code paths and build support. Other 32-bit architectures (e.g. ARM32) are untouched. Reasons: maintenance cost versus value, Windows 10 (the last 32-bit Windows) reaching end of life in October 2025, no new 32-bit x86 hardware, Linux distributions dropping it. Deprecated in 24 (JEP 501), the Windows port removed in 24 (JEP 479).

### 4.4 Other changes reported for 25 (⚠️ from a search snippet only)

The Oracle JDK 25 release-notes page returned HTTP 403, so these come from search results and are unverified:
- `CharSequence` and `CharBuffer` gain `getChars(int, int, char[], int)`.
- `CompletableFuture` and `SubmissionPublisher` async methods without an explicit executor now always use the **ForkJoinPool common pool** (previously a new thread per task if the common pool's parallelism was below 2).
- The `vfork` launch mechanism for `Process` on Linux is deprecated.
- `Thread.stop()` is **not** removed in 25; it was proposed for removal in 26 (bug `JDK-8368237`). It has been deprecated for removal since 18 and throws `UnsupportedOperationException` unconditionally since 20.

---

## 5. What arrived between 21 and 25 (and is final or standard by 25)

Taken from the JDK 22/23/24 project pages ✅ (statuses) and the JEP pages I opened. If you are upgrading from 21 these matter as much as the 25 list.

### 5.1 Language and libraries

| Version | JEP | Feature | Notes |
|---|---|---|---|
| 22 | 456 | **Unnamed Variables & Patterns** final | `_`; "no change" from JEP 443 ✅ |
| 22 | 454 | **FFM API** final | `--enable-native-access`, `Enable-Native-Access` manifest attribute ✅ |
| 22 | 458 | Launch Multi-File Source-Code Programs | `java Prog.java` can compile several files |
| 23 | 467 | Markdown Documentation Comments | Javadoc in Markdown (`///`) |
| 24 | 485 | **Stream Gatherers** final | see below |
| 24 | 484 | **Class-File API** final | see below |
| 24 | 486 | Permanently Disable the Security Manager | |
| 24 | 496, 497 | Quantum-resistant ML-KEM and ML-DSA | |
| 24 | 498 | Warn on `sun.misc.Unsafe` memory-access methods | Deprecated for removal in 23 (JEP 471) |
| 24 | 472 | Prepare to Restrict the Use of JNI | |

**Stream Gatherers (JEP 485)** ✅: custom **intermediate** stream operations, the counterpart of `Collector` for terminal operations. A `Gatherer` has an optional **initializer** (state), an **integrator** (per element; returns a boolean "continue"), an optional **combiner** (parallel) and an optional **finisher**. Built-ins in `java.util.stream.Gatherers`: `fold`, `scan`, `windowFixed`, `windowSliding`, `mapConcurrent`. Use via `stream.gather(...)`.

```java
List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5)
        .gather(Gatherers.windowSliding(2))      // illustration
        .toList();                               // [[1,2],[2,3],[3,4],[4,5]]
```

`mapConcurrent` runs a function concurrently (pairs well with virtual threads) while keeping order. ⚠️ the ordering and concurrency-limit detail is from my recall of the API, not the page I read.

**Class-File API (JEP 484)** ✅: standard `java.lang.classfile` for parsing, generating and transforming class files, built on immutable **elements**, lambda-based **builders** and **transforms**, with pattern matching instead of visitors. Reason: the JDK used an internal ASM, and the class-file format now moves every six months, so frameworks bundling an old ASM fail on new class files. Relevant to Spring, Hibernate and Mockito as they drop their bundled ASM. ⚠️ which of those have migrated: not verified.

### 5.2 Concurrency

- **JEP 491, Synchronize Virtual Threads without Pinning (24)** ✅: virtual threads now acquire/hold/release monitors independently of the carrier. `Object.wait()` unmounts. Remaining pin cases: class loading, class initialization, waiting on another thread's class initialization. `jdk.tracePinnedThreads` removed; JFR `jdk.VirtualThreadPinned` stays for native-frame cases. Net effect: the "use `ReentrantLock` instead of `synchronized` with virtual threads" advice from 21 is largely obsolete on 24+. Monitor exit may be somewhat less efficient than a plain unpark.

### 5.3 JVM and GC

- **Generational ZGC** default in 23 (JEP 474), non-generational removed in 24 (JEP 490).
- **JEP 483 AOT class loading and linking** (24), see §3.3.
- **JEP 475 Late barrier expansion for G1** (24), **JEP 423 Region pinning for G1** (22).
- **JEP 493 Linking run-time images without JMODs** (24).

### 5.4 Still in preview at 25 (what to say if asked)

String Templates were **dropped** (not in 23/24/25 lists). Structured Concurrency (505), Primitive patterns (507), Stable Values (502), PEM (470) and the Vector API (508) remain preview/incubating. ✅

---

## 6. Upgrade checklist: 21 → 25

1. **Build/test on 25 first**, then decide on flags. Check frameworks: Spring Boot 4.1 documents Java 17+ and Jakarta EE 11 ✅ ([`spring-boot-flows.md`](spring-boot-flows.md)); confirm your exact libraries ⚠️.
2. **ZGC users:** drop `-XX:+ZGenerational`; non-generational mode is gone. ✅
3. **Virtual threads:** re-test without `synchronized` workarounds (JEP 491); keep an eye on class-initialization pinning. ✅
4. **Dynamic agent loading:** still warns on 21; ensure Mockito/APM use `-javaagent`. ✅ (JEP 451)
5. **Try compact object headers** in a staging environment: `-XX:+UseCompactObjectHeaders`; compare heap, GC count, latency. ✅ flag; results ⚠️ workload-specific.
6. **Try the AOT cache** in the container build (§3.3) for start-up and warm-up. ✅
7. **Scoped values:** you can use them in 25 without `--enable-preview`; do not use `orElse(null)`. ✅
8. **32-bit x86:** if any build agents or images are 32-bit x86, they cannot run 25. ✅
9. **JFR:** try `jdk.CPUTimeSample` (Linux) and method timing for production diagnosis. ✅
10. **`CompletableFuture` default executor change** (§4.4): ⚠️ verify in the release notes if you run on small containers with `ForkJoinPool` parallelism below 2.

---

## 7. Interview questions (with short answers)

1. **What are the headline features of Java 25 for a backend engineer?** Scoped values final, compact object headers, AOT cache improvements (JEP 514/515), flexible constructor bodies, JFR profiling additions; structured concurrency still preview.
2. **`ScopedValue` vs `ThreadLocal`?** Immutable, lexically bounded, cheap to inherit into structured child threads; `ThreadLocal` is mutable, unbounded, copied to children. ✅
3. **How do you bind and read a scoped value?** `ScopedValue.where(KEY, v).run(...)` then `KEY.get()` in callees. ✅
4. **What does a Java 25 constructor allow that older ones did not?** Statements before `super()`/`this()` that do not touch `this`: validation, argument computation, initialising subclass fields. ✅
5. **Why does that fix the "virtual call from a superclass constructor" bug?** The subclass can initialise its fields in the prologue, before the superclass constructor runs. ✅
6. **What is a compact source file?** A file with top-level methods/fields and no class, wrapped in an implicit class; plus `java.lang.IO` and automatic `java.base` imports. ✅
7. **What does `import module java.base;` do and how are clashes resolved?** On-demand import of all exported packages; single-type imports beat on-demand beat module imports. ✅
8. **What do compact object headers change and how do you turn them on?** 96–128-bit → 64-bit headers; `-XX:+UseCompactObjectHeaders`; opt-in. ✅
9. **How do you create an AOT cache in 25?** `java -XX:AOTCacheOutput=app.aot -cp app.jar App`, run with `-XX:AOTCache=app.aot`. ✅
10. **What is new in the cache in JEP 515?** Method profiles are stored, so the JIT warms up faster. ✅
11. **What are the constraints of the AOT cache?** Same JDK build/arch, same class path (JARs), consistent module options, no class-rewriting agents, built-in loaders only (from JEP 483). ✅
12. **How is JEP 509 different from the old JFR sampler?** CPU-time based, includes native CPU, reports lost samples; Linux-only and experimental. ✅
13. **What did JEP 518 change?** Cooperative sampling at safepoints instead of unsafe stack walking of suspended threads. ✅
14. **Which JDK 25 features are still preview?** Structured concurrency, primitive patterns, stable values, PEM encodings; Vector API incubating. ✅
15. **What happened to String Templates?** Previewed in 21 and 22, removed before 23. ✅ (reason ⚠️)
16. **Is Shenandoah generational by default?** No; opt-in via `-XX:ShenandoahGCMode=generational`. ✅
17. **What replaced the `ShutdownOnFailure` pattern in structured concurrency?** `Joiner` policies passed to `StructuredTaskScope.open(...)`. ✅ (class removal ⚠️)
18. **Why was the Class-File API added?** A stable, JDK-maintained API to replace internal ASM and prevent version skew as the class-file format moves every six months. ✅

---

## 8. Open items (to verify before relying on them)

- The Oracle JDK 25 release notes (HTTP 403): API additions, removals and TLS/security default changes are not covered beyond §4.4.
- Which frameworks (Spring, Hibernate, Mockito, Jackson, Cassandra driver) officially support 25 and which have adopted the Class-File API.
- Real measurements for compact object headers and the AOT cache on the Conga-style stack (Cassandra driver, Redis client, Service Bus SDK).
- `StableValue` API completeness (`map`, `function`) and whether any 25 JEP changed names after the pages I read.

---

## Sources

All fetched 2026-10-03.

- JDK 25 project page (GA date, 18 JEPs and statuses): https://openjdk.org/projects/jdk/25/
- JDK 22 / 23 / 24 project pages: https://openjdk.org/projects/jdk/22/ , https://openjdk.org/projects/jdk/23/ , https://openjdk.org/projects/jdk/24/
- JEP 506 Scoped Values: https://openjdk.org/jeps/506
- JEP 505 Structured Concurrency (Fifth Preview): https://openjdk.org/jeps/505
- JEP 512 Compact Source Files and Instance Main Methods: https://openjdk.org/jeps/512
- JEP 513 Flexible Constructor Bodies: https://openjdk.org/jeps/513
- JEP 511 Module Import Declarations: https://openjdk.org/jeps/511
- JEP 507 Primitive Types in Patterns, instanceof, and switch: https://openjdk.org/jeps/507
- JEP 502 Stable Values: https://openjdk.org/jeps/502
- JEP 519 Compact Object Headers: https://openjdk.org/jeps/519
- JEP 521 Generational Shenandoah: https://openjdk.org/jeps/521
- JEP 514 AOT Command-Line Ergonomics: https://openjdk.org/jeps/514
- JEP 515 AOT Method Profiling: https://openjdk.org/jeps/515
- JEP 483 AOT Class Loading & Linking: https://openjdk.org/jeps/483
- JEP 509 JFR CPU-Time Profiling: https://openjdk.org/jeps/509
- JEP 518 JFR Cooperative Sampling: https://openjdk.org/jeps/518
- JEP 520 JFR Method Timing & Tracing: https://openjdk.org/jeps/520
- JEP 510 Key Derivation Function API: https://openjdk.org/jeps/510
- JEP 470 PEM Encodings of Cryptographic Objects: https://openjdk.org/jeps/470
- JEP 508 Vector API (Tenth Incubator): https://openjdk.org/jeps/508
- JEP 503 Remove the 32-bit x86 Port: https://openjdk.org/jeps/503
- JEP 485 Stream Gatherers: https://openjdk.org/jeps/485
- JEP 484 Class-File API: https://openjdk.org/jeps/484
- JEP 491 Synchronize Virtual Threads without Pinning: https://openjdk.org/jeps/491
- JEP 456 Unnamed Variables & Patterns: https://openjdk.org/jeps/456 ; JEP 454 FFM API: https://openjdk.org/jeps/454 ; JEP 459 String Templates (Second Preview): https://openjdk.org/jeps/459
- Search results only, not opened (used for §4.4 and the String Templates reason): https://www.oracle.com/java/technologies/javase/25-relnote-issues.html (HTTP 403 when fetched), https://bugs.openjdk.org/browse/JDK-8368237 , https://bugs.openjdk.org/browse/JDK-8329949 , https://nipafx.dev/inside-java-newscast-71/
- Not reachable: Oracle "The Arrival of Java 25" blog (HTTP 403); `jdk.java.net/25/release-notes` (HTTP 404).
- Related notes in this repo: [`java-21-new-features.md`](java-21-new-features.md), [`conga-gaps.md`](conga-gaps.md) §7.1.1, [`spring-boot-flows.md`](spring-boot-flows.md).
