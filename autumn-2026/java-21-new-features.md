# Java 21: What's New (Detailed)

JDK 21 reached General Availability on **19 September 2023** and is an LTS release from most vendors. ✅ (openjdk.org/projects/jdk/21)

**Evidence tags:** ✅ checked against the live JEP page, Javadoc or project page listed in [Sources](#sources). ⚠️ not confirmed against those pages (my knowledge or a search snippet). Code blocks are either quoted from a JEP (marked "JEP example") or written by me as illustrations (marked "illustration", compile-check them before relying on them).

**How to read this:** Part 1 is the map. Part 2 covers the features that are final in 21 and that you will use daily. Part 3 covers preview and incubator features and what became of each. Part 4 covers the small API additions. Part 5 covers behaviour changes and migration risks. Part 6 is interview questions.

---

## 1. The 15 JEPs at a glance

✅ Status as listed on the JDK 21 project page, plus what happened afterwards (from the JDK 22–25 project pages).

| JEP | Title | Status in 21 | Later |
|---|---|---|---|
| 444 | Virtual Threads | **Final** | Pinning on `synchronized` removed in 24 (JEP 491) |
| 431 | Sequenced Collections | **Final** | |
| 439 | Generational ZGC | **Final** | Generational became ZGC's default in 23 (JEP 474), non-generational removed in 24 (JEP 490) |
| 440 | Record Patterns | **Final** | |
| 441 | Pattern Matching for switch | **Final** | |
| 449 | Deprecate the Windows 32-bit x86 Port for Removal | Final | Removed in 24 (JEP 479) |
| 451 | Prepare to Disallow the Dynamic Loading of Agents | Final | |
| 452 | Key Encapsulation Mechanism API | **Final** | |
| 430 | String Templates | Preview | Re-previewed in 22 (JEP 459), **dropped before 23** (see §3.1) |
| 442 | Foreign Function & Memory API | Preview (third) | **Final in 22** (JEP 454) |
| 443 | Unnamed Patterns and Variables | Preview | **Final in 22** (JEP 456) |
| 445 | Unnamed Classes and Instance Main Methods | Preview | Renamed through 22–24, **final in 25** as JEP 512 |
| 446 | Scoped Values | Preview | **Final in 25** (JEP 506) |
| 453 | Structured Concurrency | Preview | Still preview in 25 (JEP 505, fifth preview) |
| 448 | Vector API | Incubator (sixth) | Still incubating in 25 (JEP 508, tenth) |

**Takeaway for an interview:** the language features that matter most in 21 are **virtual threads, record patterns and pattern-matching `switch`, and sequenced collections**. Everything marked preview needs `--enable-preview` and should not go into production code. ✅ (JEP 445 and JEP 442 pages state this for their features.)

---

## 2. Final features in 21

### 2.1 Virtual threads (JEP 444)

This topic already has a full verified write-up in [`conga-gaps.md` §7.1.1](conga-gaps.md). Summary only:

- A virtual thread is a lightweight `java.lang.Thread` scheduled by the JVM onto a small pool of platform "carrier" threads. When it blocks on I/O or a `java.util.concurrent` lock it unmounts, freeing the carrier.
- Create with `Thread.ofVirtual().start(r)`, `Thread.startVirtualThread(r)`, or `Executors.newVirtualThreadPerTaskExecutor()`. **Do not pool them**: one per task. (illustration)
  ```java
  try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<String>> results = urls.stream()
          .map(u -> executor.submit(() -> fetch(u)))
          .toList();
  }
  ```
- Good for I/O-bound, high-concurrency work. No benefit for CPU-bound work.
- **In 21, blocking inside `synchronized` pins the carrier.** ✅ JEP 491 (Java 24) fixed this: virtual threads now acquire, hold and release monitors independently of the carrier, and `Object.wait()` unmounts. Still pinning in 24: class loading, class initialization, and waiting for another thread's class initialization. `jdk.tracePinnedThreads` was removed; the `jdk.VirtualThreadPinned` JFR event remains.
- Also in 21: `ExecutorService` is now `AutoCloseable` (§4.5), which is what makes the `try` above work.
- Spring Boot switch: `spring.threads.virtual.enabled=true` (see [`spring-boot-flows.md`](spring-boot-flows.md)).

### 2.2 Sequenced Collections (JEP 431)

✅ **Problem:** the collections framework had no common type for "a collection with a defined encounter order". `List` and `Deque` are ordered, `SortedSet` and `LinkedHashSet` are ordered, but their common supertypes `Collection`/`Set` are not. First/last access and reverse iteration were inconsistent:

| Type | First | Last |
|---|---|---|
| `List` | `list.get(0)` | `list.get(list.size() - 1)` |
| `Deque` | `deque.getFirst()` | `deque.getLast()` |
| `LinkedHashSet` | `iterator().next()` | not available |

✅ **Three new interfaces** (JEP example):

```java
interface SequencedCollection<E> extends Collection<E> {
    SequencedCollection<E> reversed();
    void addFirst(E);
    void addLast(E);
    E getFirst();
    E getLast();
    E removeFirst();
    E removeLast();
}
interface SequencedSet<E> extends Set<E>, SequencedCollection<E> {
    SequencedSet<E> reversed();    // covariant override
}
interface SequencedMap<K,V> extends Map<K,V> {
    SequencedMap<K,V> reversed();
    SequencedSet<K> sequencedKeySet();
    SequencedCollection<V> sequencedValues();
    SequencedSet<Entry<K,V>> sequencedEntrySet();
    V putFirst(K, V);
    V putLast(K, V);
    Entry<K,V> firstEntry();
    Entry<K,V> lastEntry();
    Entry<K,V> pollFirstEntry();
    Entry<K,V> pollLastEntry();
}
```

✅ **Retrofitted hierarchy:**
- `List` and `Deque` now extend `SequencedCollection`.
- `LinkedHashSet` implements `SequencedSet`; `SortedSet` extends `SequencedSet`.
- `LinkedHashMap` implements `SequencedMap`; `SortedMap` extends `SequencedMap`.

✅ **New `Collections` wrappers:** `unmodifiableSequencedCollection`, `unmodifiableSequencedSet`, `unmodifiableSequencedMap`, plus `newSequencedSetFromMap(SequencedMap)`.

✅ **Behaviours to remember:**
- `reversed()` returns a **view**: modifications write through. `reversed().reversed()` on a `List` returns the original list. (`List` Javadoc)
- `getFirst()`/`removeLast()` on an empty list throw `NoSuchElementException`. `addFirst` on `List` calls `add(0, e)`. (`List` Javadoc)
- `List.of(...)`/`List.copyOf(...)` are unmodifiable, so mutators throw `UnsupportedOperationException`.
- On `LinkedHashSet`, `addFirst`/`addLast` **reposition** an element that is already present, fixing "a long-standing deficiency".
- `SortedSet`/`SortedMap` throw `UnsupportedOperationException` for explicit-positioning methods (`addFirst`, `putFirst`…), because order comes from the comparator.
- Compatibility: the JEP avoided covariant overrides that would break existing code, which is why the map views are named `sequencedKeySet()` etc. rather than overriding `keySet()` (same precedent as `navigableKeySet()` in Java 6).

Illustration:

```java
var list = new ArrayList<>(List.of(1, 2, 3));
list.getFirst();      // 1   (was list.get(0))
list.getLast();       // 3   (was list.get(list.size() - 1))
list.reversed();      // view [3, 2, 1]
var set = new LinkedHashSet<>(List.of("a", "b", "c"));
set.reversed().stream().toList();   // [c, b, a], previously painful
```

⚠️ **Source-compatibility trap** (worth checking in your codebase): code that defines its own `List` subtype or a helper with a `getFirst()`/`reversed()` name may now clash with the new default methods. The JEP discusses compatibility generally; I did not verify a concrete failing example.

### 2.3 Record Patterns (JEP 440)

✅ Deconstruct a record inline, in `instanceof` or `switch` (JEP examples):

```java
// Before
if (obj instanceof Point p) { int x = p.x(); int y = p.y(); }
// After
if (obj instanceof Point(int x, int y)) { /* x and y available */ }
```

Nesting:

```java
if (r instanceof Rectangle(ColoredPoint(Point(var x, var y), var c), var lr)) {
    System.out.println("Upper-left corner: " + x);
}
```

✅ Rules:
- `var` in a component position makes the compiler infer the component type; generic type arguments are inferred and propagate through nested patterns (`case MyPair(var f, var s)`).
- In a `switch`, record patterns must be **exhaustive**; analysis accounts for component types and sealed hierarchies.
- **Removed from the earlier preview:** record patterns in the header of an enhanced `for` loop. They may return later.
- Works hand in hand with JEP 441 below.

### 2.4 Pattern Matching for `switch` (JEP 441)

✅ Evolved through JEPs 406, 420, 427, 433 before becoming final.

```java
static String formatter(Object obj) {          // JEP example
    return switch (obj) {
        case Integer i -> String.format("int %d", i);
        case Long l    -> String.format("long %d", l);
        case String s  -> String.format("String %s", s);
        default        -> obj.toString();
    };
}
```

What you get:

| Feature | Detail |
|---|---|
| Pattern labels | `case Integer i`, `case Point(int x, int y)` |
| Guards | `case String s when s.length() == 1 -> ...` |
| `case null` | A dedicated label. Without it a `switch` on a pattern still throws `NullPointerException` on `null` ⚠️ (standard behaviour, not on the page I read) |
| Qualified enum constants | `case Suit.HEARTS -> ...` (new in the final version) |
| Exhaustiveness | Required for pattern switches. With a `sealed` hierarchy covering all permitted types, **no `default` is needed** |
| Dominance | A pattern cannot follow one that always matches first; compile error for unreachable code |
| Scoping | Pattern variables are flow-scoped to their arm |
| Failure | `MatchException` when matching fails at runtime, e.g. a record accessor throws |
| Compatibility | Existing `switch` code compiles unchanged; stricter checks apply only to pattern switches and switches with `null` labels |

**Changes from the last preview:** parenthesized patterns were removed; qualified enum constants were allowed. ✅

Combined illustration (sealed + records + guards, my code):

```java
sealed interface Shape permits Circle, Rect, Group {}
record Circle(double r) implements Shape {}
record Rect(double w, double h) implements Shape {}
record Group(Shape a, Shape b) implements Shape {}

static double area(Shape s) {
    return switch (s) {
        case Circle(double r)               -> Math.PI * r * r;
        case Rect(double w, double h) when w == h -> w * w;   // square
        case Rect(double w, double h)       -> w * h;
        case Group(Shape a, Shape b)        -> area(a) + area(b);
    };   // exhaustive: no default; adding a new permitted type breaks compilation here
}
```

The last point is the design payoff: a new `Shape` subtype turns every such `switch` into a compile error instead of a runtime bug.

### 2.5 Generational ZGC (JEP 439)

✅ Based on the weak generational hypothesis (young objects die young), ZGC now keeps a young and an old generation so it can collect the young one often and cheaply.

- **Enable in 21:** `-XX:+UseZGC -XX:+ZGenerational`. Plain `-XX:+UseZGC` stays non-generational in 21; "future releases will reverse these defaults".
- ✅ Later: JEP 474 (Java 23) made generational the default; JEP 490 (Java 24) removed the non-generational mode.
- Design changes ✅: colored pointers with metadata bits, store barriers feeding a remembered set, no multi-mapped memory, fast/slow-path barriers, double-buffered bitmap remembered sets, dense-region in-place aging, two-pass collection.
- Claim from the JEP ✅: on Apache Cassandra benchmarks, **about 4× throughput** than non-generational ZGC using **a quarter of the heap**, with sub-millisecond pauses. This is a vendor-chosen benchmark; measure your own workload.
- Non-goal: reference processing in the young generation.
- Risk: more complex; some non-generational workloads may degrade slightly.
- **Relevance to Conga:** Cassandra-adjacent services and large-heap, latency-sensitive services are the target audience.

### 2.6 Key Encapsulation Mechanism API (JEP 452)

✅ A KEM secures a symmetric key using public-key cryptography, without padding. New class `javax.crypto.KEM`:

- `KEM.getInstance("...")`
- `newEncapsulator(receiverPublicKey)` (sender side) → `encapsulate()` returns the shared secret plus an encapsulation message.
- `newDecapsulator(privateKey)` (receiver side) → `decapsulate(encapsulation)` recovers the shared secret.
- Shipped with DHKEM per RFC 9180. Motivation: post-quantum KEM candidates, Hybrid Public Key Encryption (HPKE), TLS configurations that need KEMs.
- JEP example uses placeholder algorithm names ("ABC", "ABC-KEM"); the JEP's own text shows `KEM.getInstance("RSA-KEM")` in another snippet.

### 2.7 Dynamic agent loading warning (JEP 451)

✅ JDK 21 prints `WARNING: A {Java,JVM TI} agent has been loaded dynamically` when an agent attaches to a running JVM.

- Flags: `-XX:+EnableDynamicAgentLoading` (allow, no warning) and `-XX:-EnableDynamicAgentLoading` (disallow; testable from JDK 9).
- **Unaffected:** `-javaagent` and `-agentlib` at startup.
- Hit by: profilers and APM tools that attach at runtime; mocking/bytecode libraries (Mockito/ByteBuddy) that self-attach. They must move to startup-time agent loading.
- Plan: in a future release dynamic loading will be disallowed by default (an `AgentLoadException`).
- **Practical:** if your test suite starts printing this warning on 21, configure Mockito as a `-javaagent` in the build rather than silencing it. ⚠️ (the Maven/Gradle details are mine, not from the JEP.)

### 2.8 Deprecation: Windows 32-bit x86 (JEP 449)

✅ Deprecated for removal in 21; removed in 24 (JEP 479).

---

## 3. Preview and incubator features in 21

All need `--enable-preview` (and `--release 21` when compiling) ⚠️ flag spelling is standard; the JEP pages I read state the requirement for JEP 445 and the preview APIs.

### 3.1 String Templates (JEP 430): did not survive

✅ In 21 you could write:

```java
String name = "Joan";
String info = STR."My name is \{name}";
```

with three processors: `STR` (interpolation), `FMT` (`java.util.Formatter` specifiers), `RAW` (an unprocessed `StringTemplate`). Custom processors implement `StringTemplate.Processor<R, E>` and receive **fragments** (the literals) and **values** (the evaluated expressions), which allowed a `QueryBuilder` that produced prepared SQL. Omitting the processor was a compile-time error, by design.

✅ It was re-previewed in 22 (JEP 459). It does **not appear** in the JEP lists of JDK 23, 24 or 25. A search result (nipafx / JDK bug tracker `JDK-8329949`, "Remove the String Templates preview feature") says it was dropped because the processor-centric design was confusing, lacked compositionality, and had no consensus on a redesign. ⚠️ I read only the search snippet for the reason.

**Interview angle:** an example of a preview feature being withdrawn. Do not use `STR."..."` in code; use `String.format`, `formatted` or concatenation.

### 3.2 Foreign Function & Memory API (JEP 442, third preview)

✅ A pure-Java alternative to JNI for calling native code and using off-heap memory.

- `Arena`: lifetime management. Four kinds: global, automatic (GC-managed), confined (deterministic, single thread), shared (multi-thread).
- `MemorySegment`: contiguous memory (native, mapped, or array-backed) with spatial and temporal safety.
- `Linker`: downcalls (Java→native) and upcalls (native→Java) via method handles.
- `SymbolLookup`, `FunctionDescriptor`: find functions and describe C signatures.
- Changes in this preview: memory management centralised in `Arena`, address-dereferencing layout paths, an optimisation option for short-lived functions, `VaList` removed.

JEP example (21 API; names changed in later versions):

```java
Linker linker = Linker.nativeLinker();
MethodHandle strlen = linker.downcallHandle(
    linker.defaultLookup().find("strlen").get(),
    FunctionDescriptor.of(JAVA_LONG, ADDRESS));
try (Arena arena = Arena.ofConfined()) {
    long len = (long) strlen.invoke(arena.allocateUtf8String("Hello"));
}
```

✅ **Final in 22 (JEP 454).** Changes: a linker option to pass heap segments to downcalls, an `Enable-Native-Access` JAR-manifest attribute, programmatic C descriptors, variable-length arrays, arbitrary charsets. Restricted methods warn unless `--enable-native-access=MODULE` is given. (`allocateUtf8String` is the 21 name; check the 22+ Javadoc for the renamed method before copying code across.) ⚠️ rename not verified.

### 3.3 Unnamed Patterns and Variables (JEP 443)

✅ `_` for things you must declare but do not use:

| Context | Example |
|---|---|
| Local variable | `int _ = q.remove();` |
| `catch` | `catch (NumberFormatException _) { }` |
| Lambda parameter | `_ -> "NODATA"` |
| Enhanced `for` | `for (Order _ : orders)` |
| try-with-resources | `try (var _ = ScopedContext.acquire())` |
| Unnamed pattern | `o instanceof Point(int x, _)` |
| Unnamed pattern variable | `o instanceof Point(int x, int _)` |

Restrictions ✅: `_` cannot stand alone at the top level (`r instanceof _` and `case _` are illegal); unnamed variables need an initializer and cannot be read afterwards. **Final in 22 (JEP 456), "without change".** ✅

### 3.4 Unnamed Classes and Instance `main` (JEP 445)

✅ Launch protocol: the first match of
1. `static void main(String[])` (non-private)
2. `static void main()`
3. instance `void main(String[])`
4. instance `void main()`

An unnamed class is created implicitly when a file contains methods outside any class: it is `final`, in the unnamed package, extends only `Object`, implements no interfaces, cannot be referenced by name, has only the default constructor, and needs a `main`. Wrap it in a class to graduate. If a "traditional" inherited `main` exists but another is selected, a warning is printed.

✅ **Final in 25 as JEP 512** (renamed "compact source files"); see [`java-25-new-features.md`](java-25-new-features.md) §2.2.

### 3.5 Scoped Values (JEP 446)

✅ "Implicit method parameters": share immutable data from a caller to its callees without passing it through every signature.

```java
static final ScopedValue<User> CURRENT = ScopedValue.newInstance();   // illustration
ScopedValue.where(CURRENT, user).run(() -> handler.handle(request));  // 21 API
// inside any callee: CURRENT.get()
```

✅ vs `ThreadLocal`: bounded lifetime (only during `run`), **immutable** (no `set`), cheap inheritance by child threads of a `StructuredTaskScope` (no copying). Nested rebinding is allowed and the outer binding returns afterwards. `isBound()` checks for a binding.

✅ **Final in 25 (JEP 506).** In 21 the API was `where(...).run(...)` / `call(...)`; see the 25 document for what changed.

### 3.6 Structured Concurrency (JEP 453)

✅ Treat a group of related subtasks in different threads as one unit of work.

JEP example (21 API):

```java
Response handle() throws ExecutionException, InterruptedException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Supplier<String>  user  = scope.fork(() -> findUser());
        Supplier<Integer> order = scope.fork(() -> fetchOrder());
        scope.join().throwIfFailed();
        return new Response(user.get(), order.get());
    }
}
```

- `fork(Callable)` returns a `Subtask` (not a `Future`); `join()`/`joinUntil(Instant)` wait; `shutdown()` cancels.
- Policies: `ShutdownOnFailure` (cancel siblings if one fails) and `ShutdownOnSuccess` (first success wins, for racing).
- Fixes the problems of unstructured `ExecutorService` code: leaked threads when one subtask fails, no cancellation propagation to siblings, no interruption propagation from parent to children, and a task/subtask relationship that exists only in your head.
- Observability: JSON thread dumps show the scope hierarchy.
- Pairs naturally with virtual threads.

✅ **Still preview in 25 (JEP 505)** with a redesigned API (`open()`, `Joiner`); see the 25 document.

### 3.7 Vector API (JEP 448, sixth incubator)

✅ Still incubating in 25 (tenth). It waits on Project Valhalla features. Mention only that it exists; it is not a mainstream backend skill.

---

## 4. Small API additions in 21

All ✅ from the Javadoc pages listed in Sources (`@since 21`).

### 4.1 `Math.clamp` (4 overloads)

```java
public static int    clamp(long   value, int    min, int    max)   // saturating long→int
public static long   clamp(long   value, long   min, long   max)
public static double clamp(double value, double min, double max)
public static float  clamp(float  value, float  min, float  max)
```
- Returns `min` if below, `max` if above, else the value. Throws `IllegalArgumentException` if `min > max` (for floating types also if either bound is NaN, or `min` is `+0.0` and `max` is `-0.0`).
- The `int` form with a `long` input is a **safe saturating cast**: `Math.clamp(bigLong, Integer.MIN_VALUE, Integer.MAX_VALUE)`.
- Floating forms: NaN in → NaN out; `-0.0` is strictly smaller than `+0.0`, so `clamp(-0.0, 0.0, 1.0)` returns `0.0`.

### 4.2 `StringBuilder.repeat`

```java
public StringBuilder repeat(int codePoint, int count)
public StringBuilder repeat(CharSequence cs, int count)
```
- Appends `count` copies. `count < 0` or an invalid code point → `IllegalArgumentException`. A `null` `CharSequence` appends `"null"` repeated. Example from the Javadoc: `sb.repeat('*', 10)`.

### 4.3 `String` additions

- `indexOf(int ch, int beginIndex, int endIndex)` and `indexOf(String str, int beginIndex, int endIndex)`: search within a range.
- `splitWithDelimiters(String regex, int limit)`: like `split` but the result **includes the delimiters** (so you can reassemble the original string). ⚠️ Details of how an empty trailing element or `limit` behave are on the Javadoc page, which I only skimmed through a summary.

### 4.4 `Character` emoji methods

`isEmoji`, `isEmojiPresentation`, `isEmojiModifier`, `isEmojiModifierBase`, `isEmojiComponent`, `isExtendedPictographic`, all static, taking an `int codePoint`.

### 4.5 `ExecutorService` is `AutoCloseable`

✅ `close()` is an orderly shutdown that **waits** for submitted tasks to finish. If interrupted while waiting it calls `shutdownNow()`, keeps waiting for running tasks, does not run queued tasks, and re-asserts the interrupt flag. Idempotent. Do not close the `ForkJoinPool` common pool explicitly.

### 4.6 `Collections`

`shuffle(List<?>, RandomGenerator)` (works with any `RandomGenerator`, not only `java.util.Random`), plus the sequenced wrappers from §2.2.

### 4.7 Others (⚠️ not verified)

I recall `Thread.join(Duration)`, `Thread.isVirtual()`, `HttpClient` becoming `AutoCloseable`, and `Locale`/CLDR changes, but could not confirm them because the Oracle release notes page returned HTTP 403. Check them in the Javadoc before quoting.

---

## 5. Behaviour changes and migration risks (21)

1. **Dynamic agent warning** (JEP 451): noisy logs from Mockito/ByteBuddy and APM tools; fix by startup agents. ✅
2. **New default methods on `List`/`Deque`/etc.** (JEP 431): possible name clashes in your own subtypes. ⚠️
3. **Virtual threads and `synchronized`/native frames** pin carriers in 21: prefer `ReentrantLock` around blocking calls, or move to 24+. ✅ (see §2.1)
4. **Preview APIs are not for production**: string templates, scoped values, structured concurrency and FFM changed or disappeared in later releases. ✅
5. **ZGC users:** the flag `-XX:+ZGenerational` is required in 21; after 24 it is unnecessary and the old mode is gone. ✅
6. **Spring/Boot:** Boot 4 requires Java 17+ and supports 21 virtual threads via a property ([`spring-boot-flows.md`](spring-boot-flows.md)). ✅

---

## 6. Interview questions (with short answers)

1. **What is the main thing Java 21 added for backend developers?** Virtual threads (final), plus pattern matching for `switch` and record patterns, plus sequenced collections.
2. **Why does a pattern `switch` over a sealed interface need no `default`?** The compiler knows the permitted subtypes; exhaustiveness is checked, and adding a subtype breaks compilation of every such `switch`.
3. **What does `case null` do?** Handles `null` in the switch; otherwise a `null` selector throws `NullPointerException`.
4. **What does `reversed()` return?** A live reverse-ordered view, not a copy; writes go through.
5. **What does `list.getFirst()` do on an empty list?** Throws `NoSuchElementException`.
6. **Can `SortedSet.addFirst` be called?** No, `UnsupportedOperationException`; order comes from the comparator.
7. **Virtual vs platform threads: when would you not use virtual threads?** CPU-bound work, pooled/limited-resource patterns where the pool is the limiter, code that blocks while holding a monitor on 21–23 (pinning).
8. **Why must you not pool virtual threads?** They are cheap; pools exist to limit expensive resources. Use a semaphore if you need to cap concurrency to a downstream.
9. **`ThreadLocal` vs `ScopedValue`?** Mutable/unbounded/expensive inheritance vs immutable/scoped/cheap. (Scoped values are final only in 25.)
10. **Which 21 features were preview, and what happened to them?** Table in §1: FFM final 22, unnamed final 22, compact main final 25, scoped values final 25, structured concurrency still preview, string templates withdrawn.
11. **How do you enable Generational ZGC in 21?** `-XX:+UseZGC -XX:+ZGenerational`.
12. **Your tests print "A Java agent has been loaded dynamically". Why, and what do you do?** JEP 451; register the agent at startup (`-javaagent`), or temporarily `-XX:+EnableDynamicAgentLoading`.
13. **What does `ExecutorService.close()` do?** Orderly shutdown plus wait; on interrupt, `shutdownNow()` and keep waiting for running tasks.

---

## Sources

All fetched 2026-10-03.

- JDK 21 project page (GA date, JEP list and statuses): https://openjdk.org/projects/jdk/21/
- JDK 22, 23, 24, 25 project pages (what became of each preview): https://openjdk.org/projects/jdk/22/ , https://openjdk.org/projects/jdk/23/ , https://openjdk.org/projects/jdk/24/ , https://openjdk.org/projects/jdk/25/
- JEP 431 Sequenced Collections: https://openjdk.org/jeps/431
- JEP 439 Generational ZGC: https://openjdk.org/jeps/439
- JEP 440 Record Patterns: https://openjdk.org/jeps/440
- JEP 441 Pattern Matching for switch: https://openjdk.org/jeps/441
- JEP 430 String Templates: https://openjdk.org/jeps/430 ; JEP 459: https://openjdk.org/jeps/459
- JEP 442 Foreign Function & Memory API (third preview): https://openjdk.org/jeps/442 ; JEP 454 (final): https://openjdk.org/jeps/454
- JEP 443 Unnamed Patterns and Variables: https://openjdk.org/jeps/443 ; JEP 456 (final): https://openjdk.org/jeps/456
- JEP 445 Unnamed Classes and Instance Main Methods: https://openjdk.org/jeps/445
- JEP 446 Scoped Values: https://openjdk.org/jeps/446
- JEP 451 Prepare to Disallow the Dynamic Loading of Agents: https://openjdk.org/jeps/451
- JEP 452 Key Encapsulation Mechanism API: https://openjdk.org/jeps/452
- JEP 453 Structured Concurrency: https://openjdk.org/jeps/453
- JEP 491 Synchronize Virtual Threads without Pinning: https://openjdk.org/jeps/491
- Javadoc, Java SE 21: `Math` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Math.html ; `StringBuilder` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/StringBuilder.html ; `String` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html ; `Character` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Character.html ; `ExecutorService` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html ; `List` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/List.html ; `Collections` https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Collections.html
- Why String Templates were dropped (search results only, not opened): https://bugs.openjdk.org/browse/JDK-8329949 , https://nipafx.dev/inside-java-newscast-71/ , https://www.infoq.com/news/2024/08/java-23-so-far
- Not reachable: Oracle JDK 21 release notes (HTTP 403). Items in §4.7 are therefore unverified.
- Related notes in this repo: [`conga-gaps.md`](conga-gaps.md) §7.1.1 (virtual threads), [`spring-boot-flows.md`](spring-boot-flows.md).
