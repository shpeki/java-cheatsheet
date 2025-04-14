# Java Interview Cheat Sheet

## SOLID Principles

| Principle | Description | Key Point |
|-----------|-------------|-----------|
| **S**ingle Responsibility | A class should have only one reason to change | Each class handles one functional area |
| **O**pen/Closed | Open for extension, closed for modification | Use interfaces and abstract classes |
| **L**iskov Substitution | Subtypes must be substitutable for their base types | Ensure subclasses don't break parent class behavior |
| **I**nterface Segregation | Client shouldn't depend on methods it doesn't use | Create specific interfaces rather than general ones |
| **D**ependency Inversion | Depend on abstractions, not concretions | High-level modules shouldn't depend on low-level modules |

## OOP Principles in Java

| Principle | Description | Example |
|-----------|-------------|---------|
| Encapsulation | Bundling data and methods that operate on the data | Private fields with getter/setter methods |
| Inheritance | A class can inherit properties from another class | `class Car extends Vehicle` |
| Polymorphism | Objects can take many forms depending on context | Method overriding and interfaces |
| Abstraction | Hide implementation details, show only functionality | Abstract classes and interfaces |

## Marker Interfaces

| Interface | Purpose | Note |
|-----------|---------|------|
| Serializable | Enable object serialization | No methods to implement |
| Cloneable | Allow object cloning via Object.clone() | Must still override clone() |
| RandomAccess | Indicate list supports fast random access | Used by Collections framework |
| Remote | Identify objects that can be accessed remotely | Used in RMI |

## Comparison of Nested Class Types

| Feature | Static Nested | Inner Class | Local Class | Anonymous Class |
|---------|---------------|------------|------------|-----------------|
| Access to outer class | Static members only | All members | All members | All members |
| Can be instantiated without outer class | Yes | No | N/A | N/A |
| Where declared | Inside outer class | Inside outer class | Inside a method/block | Inside an expression |
| Can have static members | Yes | No | No | No |
| Can have constructors | Yes | Yes | Yes | No |
| Can extend/implement | Yes | Yes | Yes | Yes (one only) |
| Access to local variables | No | No | Yes (final/effectively final) | Yes (final/effectively final) |
| Can be defined with modifiers | Yes | Yes | No | No |

## Pass-by-Value in Java
### Summary

- Java is strictly pass-by-value.
- For primitive types, a copy of the actual value is passed.
- For objects, a copy of the reference (memory address) is passed.
- You can modify the internal state of objects in methods, but you cannot change which object the original reference points to.
- Understanding this distinction is crucial for writing correct Java code and avoiding unexpected behaviors.

## Returning Multiple Values from Methods in Java
### Comparison of Approaches

| Approach | Type Safety | Semantic Clarity | Ease of Use | Best For |
|----------|------------|-----------------|-------------|----------|
| Custom Class | High | High | Medium | Related values with clear meaning |
| Record (Java 16+) | High | High | High | Same as custom class, with less boilerplate |
| Collection/Array | Medium | Low | Medium | Multiple values of the same type |
| Map | Low | Medium | Medium | Dynamic sets of named values |
| Output Parameters | Medium | Medium | Low | Performance-critical code |
| Library Classes | Medium | Low | High | Quick solutions without custom classes |

## Static vs Dynamic Polymorphism in Java

### Key Differences

| Feature | Static Polymorphism | Dynamic Polymorphism |
|---------|---------------------|----------------------|
| Resolution Time | Compile time | Runtime |
| Implementation | Method overloading | Method overriding |
| Binding | Early binding | Late binding |
| Performance | More efficient | Slightly less efficient |
| Flexibility | Less flexible | More flexible |
| Called via | Reference type | Object type |
| Method selection based on | Method signature and argument types | Actual object type |

## Interface vs Abstract Class in Java
### Key Differences

| Feature | Interface | Abstract Class |
|---------|-----------|----------------|
| Multiple Inheritance | Can implement multiple interfaces | Can extend only one abstract class |
| State | No state (only constants) | Can have instance fields |
| Constructor | No constructors | Can have constructors |
| Method Access | All methods are implicitly public | Methods can have any access modifier |
| Default Methods | Supported (since Java 8) | Always supported |
| Purpose | Defines a contract of what a class can do | Provides a common base with some functionality |
| Use Case | When unrelated classes would use the same method signatures | When classes share common state and behavior |
| Instantiation | Cannot be instantiated | Cannot be instantiated |

## JVM Memory Areas

### Stack Memory

- Contains method-specific values and references
- Fixed in size and cannot be dynamically expanded
- Automatically allocated and deallocated when a method is called and returns
- Faster access compared to heap memory
- Thread-safe as each thread has its own stack

### Heap Memory

- Created when the JVM starts
- Stores objects and JRE classes
- Shared among all threads
- Not thread-safe
- Uses garbage collection for memory management
- Can be divided into generations for more efficient garbage collection

### Differences between Stack and Heap

| Feature | Stack | Heap |
|---------|-------|------|
| Memory allocation | Automatic | Manual and Garbage Collection |
| Deallocation | Automatic | Garbage Collection |
| Access speed | Faster | Slower |
| Thread safety | Each thread has its own stack | Shared among all threads |
| Memory efficiency | More efficient but limited size | Less efficient but larger |
| Data stored | Method calls, local variables, references | Objects, instance variables, class definitions |

## PostgreSQL Transaction Isolation Levels

| Isolation Level | Dirty Read | Non-repeatable Read | Phantom Read | Default |
|-----------------|------------|---------------------|--------------|---------|
| READ UNCOMMITTED | No (same as READ COMMITTED) | Yes | Yes | No |
| READ COMMITTED | No | Yes | Yes | Yes |
| REPEATABLE READ | No | No | No (PostgreSQL specific) | No |
| SERIALIZABLE | No | No | No | No |

## HashMap vs TreeMap in Java

| Feature | HashMap | TreeMap |
|---------|---------|---------|
| Order | Unordered | Sorted by keys |
| Implementation | Hash table | Red-Black Tree |
| Performance (avg) | O(1) for get/put | O(log n) for get/put |
| Null keys | One allowed | Not allowed |
| Use case | Fast lookups | Sorted data, range queries |
| Memory usage | Higher | Lower for structure |

## Java Memory Model and Synchronization

| Feature | Description |
|---------|-------------|
| Happens-Before | Defines memory visibility between threads |
| Volatile | Ensures visibility, not atomicity |
| Synchronized | Ensures both visibility and atomicity |
| Monitor | Each object has a lock used by synchronized |
| Atomic Classes | Thread-safe operations without locks |
| Visibility | Without synchronization, threads may see stale values |
| Reordering | JVM/CPU may reorder operations for optimization |

## Design Patterns in Java

| Pattern Type | Pattern Name | Purpose |
|--------------|--------------|---------|
| Creational | Singleton | Ensure a class has only one instance |
| Creational | Factory Method | Create objects without specifying class |
| Creational | Builder | Construct complex objects step by step |
| Structural | Adapter | Allow incompatible interfaces to work together |
| Structural | Decorator | Add responsibilities to objects dynamically |
| Behavioral | Observer | One-to-many dependency between objects |
| Behavioral | Strategy | Define family of algorithms, make interchangeable |

## Functional Programming in Java

| Feature | Description | Example |
|---------|-------------|---------|
| Lambda Expression | Anonymous function | `(a, b) -> a + b` |
| Functional Interface | Interface with one abstract method | `Predicate<T>`, `Function<T,R>` |
| Method Reference | Shorthand for simple lambdas | `String::length` |
| Stream API | Process sequences of elements | `list.stream().filter(n -> n > 10)` |
| Optional | Container for nullable value | `Optional.ofNullable(value)` |

## Java Generics

| Feature | Syntax | Description |
|---------|--------|-------------|
| Generic Class | `class Box<T>` | Type parameter for class |
| Bounded Type Parameter | `<T extends Number>` | Restrict to subtype |
| Unbounded Wildcard | `List<?>` | Any type |
| Upper Bounded Wildcard | `List<? extends Number>` | Number or subtype |
| Lower Bounded Wildcard | `List<? super Integer>` | Integer or supertype |
| Type Erasure | N/A | Generic info removed at runtime |
| PECS Principle | N/A | Producer Extends, Consumer Super |

## Java Testing with JUnit 5 and Mockito

| Feature | Description | Example |
|---------|-------------|---------|
| @Test | Mark method as test | `@Test void testMethod()` |
| @BeforeEach | Run before each test | `@BeforeEach void setup()` |
| @AfterEach | Run after each test | `@AfterEach void cleanup()` |
| assertEquals | Assert values are equal | `assertEquals(expected, actual)` |
| assertThrows | Assert exception thrown | `assertThrows(Exception.class, () -> method())` |
| Mock Creation | Create mock object | `Service service = mock(Service.class)` |
| Stubbing | Define mock behavior | `when(service.method()).thenReturn(value)` |
| Verification | Verify method called | `verify(service).method()` |
| Argument Matchers | Match method args | `any()`, `anyString()`, `eq(value)` |
| Argument Captor | Capture method args | `ArgumentCaptor<T> captor = ArgumentCaptor.forClass(T.class)` |

## Concurrent vs. Parallel Collections

| Collection Type | Thread-Safety | Description | Use Case |
|-----------------|---------------|-------------|----------|
| Vector, Hashtable | Synchronized | Legacy thread-safe collections with single lock | Low contention, backward compatibility |
| Collections.synchronizedX | Synchronized | Thread-safe wrappers with single lock | Simple multi-threaded scenarios |
| ConcurrentHashMap | Concurrent | Lock striping and non-blocking reads | High-concurrency environments |
| CopyOnWriteArrayList | Concurrent | Immutable snapshots with copy-on-write | Read-heavy workloads |
| ConcurrentSkipListMap | Concurrent | Sorted concurrent map | Sorted data with concurrency |

## Critical Section & Synchronization

| Feature | Description | Example |
|---------|-------------|---------|
| Critical Section | Code segment accessing shared resources | Counter increment operation |
| synchronized method | Locks on 'this' or class object | `public synchronized void method()` |
| synchronized block | Locks only specified object | `synchronized(lockObject) { }` |
| Lock interface | Explicit locking mechanism | `lock.lock(); try { } finally { lock.unlock(); }` |
| Atomic variables | Thread-safe without locks | `AtomicInteger counter = new AtomicInteger()` |
| volatile | Ensures visibility, not atomicity | `private volatile boolean flag` |

## equals() and hashCode()

| Aspect | Contract Rule | Notes |
|--------|---------------|-------|
| equals() consistency | Always returns same result for same objects | Unless information used in equals changes |
| equals() symmetry | If a.equals(b) then b.equals(a) | Equality works both ways |
| equals() transitivity | If a.equals(b) and b.equals(c) then a.equals(c) | Equality is transitive |
| equals() and null | x.equals(null) should return false | Should not throw exception |
| hashCode() consistency | Equal objects must have equal hash codes | Unequal objects should have different hash codes if possible |
| Implementation | Use all fields that affect equality | Override both methods together |

## Stream Operations

| Operation Type | Examples | Characteristics |
|----------------|----------|-----------------|
| Intermediate | filter, map, sorted, distinct | Lazy, returns a new stream |
| Terminal | collect, forEach, reduce, count | Eager, consumes the stream |
| Short-circuiting | limit, findFirst, anyMatch | May not process entire stream |

## Parallel Streams

| Aspect | Description | Best Practice |
|--------|-------------|---------------|
| Creation | `collection.parallelStream()` or `stream.parallel()` | Use for computationally intensive operations |
| Performance | Better for large datasets with independent operations | Benchmark to verify improvement |
| Thread Safety | Operations must be thread-safe | Avoid stateful lambdas |
| Ordering | May not preserve encounter order | Use `forEachOrdered` when order matters |
| Reduction | Use identity-based reduction for parallelism | Ensure accumulator and combiner functions are associative |

## Monolith vs. Microservices

| Aspect | Monolithic Architecture | Microservices Architecture |
|--------|-------------------------|----------------------------|
| Deployment | Single unit deployment | Independent service deployment |
| Scaling | Vertical (entire application) | Horizontal (per service) |
| Development | Simpler for small applications | Better for large, complex applications |
| Technology | Single technology stack | Can use different technologies per service |
| Data | Single, shared database | Database per service |
| Complexity | Simpler architecture, complex codebase | Complex architecture, simpler services |
| Team Structure | Suited for smaller teams | Multiple teams working independently |

## Protected vs. Package-Private Access

| Access Level | Within Class | Within Package | Subclass in Different Package | World |
|--------------|--------------|----------------|-------------------------------|-------|
| public | Yes | Yes | Yes | Yes |
| protected | Yes | Yes | Yes | No |
| package-private (default) | Yes | Yes | No | No |
| private | Yes | No | No | No |

## New Java Features

| Java Version | Feature | Example |
|--------------|---------|---------|
| Java 8 | Lambda Expressions | `(a, b) -> a + b` |
| Java 8 | Stream API | `list.stream().filter(x -> x > 0)` |
| Java 8 | Optional | `Optional.ofNullable(value)` |
| Java 8 | Default Methods | `default void method() { }` |
| Java 9 | Module System | `module com.example {}` |
| Java 9 | Collection Factory Methods | `List.of(1, 2, 3)` |
| Java 10 | Local Variable Type Inference | `var x = "hello"` |
| Java 14 | Switch Expressions | `yield value` |
| Java 16 | Records | `record Person(String name, int age) {}` |
| Java 17 | Sealed Classes | `sealed interface X permits Y, Z {}` |

# Java Interview Questions & Answers

## Stack & Memory

### What is Stack Overflow in Java?
A StackOverflowError occurs when a program's call stack exceeds its allocated memory limit, typically due to excessive recursion without proper base cases. The JVM allocates a fixed stack size (usually 512KB-1MB) that can be modified with `-Xss` parameter.

### What is a Frame in Stack?
A stack frame (activation record) is a data structure created for each method invocation containing:
- Local variables and parameters
- Operand stack for intermediate calculations
- Frame data (runtime constant pool reference, return address, previous frame reference)
When a method completes, its frame is popped from the stack.

### How to Get OutOfMemoryError in Java?
OutOfMemoryError occurs when the JVM cannot allocate objects due to insufficient memory. Common types:
- Java Heap Space: Heap is full (`-Xmx` limit reached)
- Metaspace: Class metadata area is full
- GC Overhead Limit Exceeded: GC running constantly with minimal recovery
- Direct Buffer Memory: NIO direct buffers exceed limit
- Unable to Create New Native Thread: OS limit on thread creation reached

### What are Memory Leaks in Java?
Memory leaks occur when objects remain referenced but are no longer needed, preventing garbage collection. Common causes:
- Static collections growing indefinitely
- Unclosed resources (streams, connections)
- HashMap with mutable keys
- Non-static inner classes holding outer class references
- Underegistered listeners and callbacks
- ThreadLocal variables not removed
- Unbounded caches without eviction policies

## Concurrency

### What is Thread Starvation?
Thread starvation occurs when a thread cannot gain regular access to shared resources for extended periods. Causes include:
- Priority issues (high-priority threads monopolizing CPU)
- Unbalanced synchronization patterns
- Non-fair locks that don't ensure first-come-first-served access
- Long-running operations holding locks

### What is Main Memory in Java?
Main memory in Java refers to the heap where all objects are allocated. It's shared among all threads in the JVM. The Java Memory Model defines rules for when changes by one thread become visible to others. Proper synchronization mechanisms (synchronized, volatile, atomic classes) ensure consistency between threads and main memory.

### Where is Shared CPU Cache in Java?
The concept of "shared CPU cache" isn't Java-specific but refers to hardware caches in multi-core processors (L1, L2, L3). Java doesn't directly manage these, but the JVM memory model accounts for them:
- `volatile` ensures reads/writes go directly to main memory
- `synchronized` blocks establish memory barriers that flush caches
- Atomic variables use CPU instructions that work with cache coherence protocols

## Collections & Data Structures

### How HashMap Handles Collisions?
HashMap handles collisions (when different keys hash to the same bucket) using:
1. **Chaining**: Stores colliding entries in a linked list
2. **Tree Conversion** (Java 8+): If a bucket's linked list exceeds 8 entries, it's converted to a balanced red-black tree, improving worst-case performance from O(n) to O(log n)

### How HashMap Finds the Bucket for a Key?
HashMap finding a bucket follows these steps:
1. Calculate the key's hashCode()
2. Improve hash distribution: hash = hashCode ^ (hashCode >>> 16)
3. Determine bucket index: index = hash & (capacity - 1)
4. Access that bucket in the internal array
5. If collisions exist, search the list/tree for the exact key using equals()

### When to Use List vs HashMap?
**Use List when:**
- Order matters (sequence is important)
- You need access by position/index
- Duplicates are allowed or needed
- You frequently iterate through all elements

**Use HashMap when:**
- Fast key-based lookup is needed
- Associations between keys and values are required
- You need to check if a key exists quickly
- Duplicate keys should be eliminated

## Spring Framework

### How to Resolve Two Beans Implementing the Same Interface?
When multiple beans implement the same interface in Spring, resolve autowiring ambiguity with:
1. **@Qualifier**: Specify which bean to inject (`@Qualifier("beanName")`)
2. **@Primary**: Mark one implementation as the default
3. **Collections**: Inject all implementations (`List<Interface>` or `Map<String, Interface>`)
4. **Name-based autowiring**: Name the variable after the implementation
5. **@Resource**: Use JSR-250's name-based injection
6. **Custom qualifiers**: Create semantic qualifier annotations
7. **Conditional beans**: Register beans based on conditions
8. **Configuration classes**: Explicitly configure with @Bean methods

### What is Dependency Inversion?
Dependency Inversion Principle (DIP) states:
1. High-level modules should not depend on low-level modules; both should depend on abstractions
2. Abstractions should not depend on details; details should depend on abstractions

Benefits:
- Decoupling between components
- Flexibility to switch implementations
- Easier testing through mocking
- Better maintenance and extensibility

## Java Language Features

### What Does Final Mean?
- **final class**: Cannot be extended/subclassed
- **final method**: Cannot be overridden in subclasses
- **final field**: Can only be assigned once (primitives cannot change value; references cannot point to a different object)
- **final parameter**: Cannot be reassigned within a method

### When to Use Abstract Class vs Interface?
**Use Abstract Class when:**
- You need to share code among related classes
- You want to provide partial implementation
- You need instance state (fields)
- You require access control beyond public
- You need constructors
- Your design represents an "is-a" relationship

**Use Interface when:**
- You want to define a contract without implementation constraints
- Multiple inheritance is needed
- The capability applies to unrelated classes
- Various classes will implement the contract differently
- You want to use functional programming (lambdas)
- You need to evolve the API without breaking implementations

### When to Use Static Methods?
Use static methods for:
- Utility functions independent of object state
- Factory methods that create objects
- Entry points (main method)
- Constants and configuration (with final)
- Helper methods for stateless operations
- Singleton access methods
- Operations on multiple objects of the same class

Avoid static methods when:
- The method needs object state
- The method might be overridden (polymorphism)
- Testing requires mocking
- The method calls instance methods
- State needs to be maintained between calls

## Databases

### What is an Index in a Database?
An index is a data structure that improves data retrieval speed by allowing the database to find rows without scanning the entire table. Types include:
- Primary indexes (for primary keys)
- Unique indexes (enforce uniqueness)
- Composite indexes (multiple columns)
- Clustered indexes (determine physical data order)
- Non-clustered indexes (separate from the data)
- Full-text indexes (optimized for text search)

Indexes speed up WHERE clauses, JOIN operations, and ORDER BY clauses but slow down write operations.

### Does the Order of Columns in Composite Indexes Matter?
Yes, column order in composite indexes is crucial:
- The leftmost prefix rule applies: queries must reference columns from left to right
- A composite index on (A, B, C) can be used for queries on A, A+B, or A+B+C
- It cannot be used for queries on just B, just C, or B+C
- The database sorts first by the first column, then by subsequent columns
- For optimal performance, put equality conditions before range conditions and consider column selectivity

## Microservices

### What is Service Discovery in Java?
Service discovery allows microservices to locate and communicate with each other without hardcoded locations. Java implementations include:
- **Spring Cloud Netflix Eureka**: Popular registry for Spring applications
- **Consul**: Service discovery with configuration management
- **Apache ZooKeeper**: With Apache Curator for service discovery
- **Kubernetes Service Discovery**: Built into the platform

Service discovery enables dynamic scaling, high availability, location transparency, and load balancing in distributed systems.

### What are Server-Sent Events?
Server-Sent Events (SSE) is a technology allowing servers to push updates to clients over HTTP. Unlike WebSockets, SSE is a one-way channel (server to client). Characteristics:
- Uses standard HTTP with text/event-stream content type
- Automatic reconnection built into the protocol
- Text-based format with data, event, and id fields
- Simpler than WebSockets for one-way communication

Java implementations include Servlet API, Spring MVC's SseEmitter, and Spring WebFlux's reactive support.

## Algorithms

### Checking if a String is a Palindrome
```java
public boolean isPalindrome(String str) {
    // Clean the string: remove non-alphanumeric characters and convert to lowercase
    String cleanStr = str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    
    // Use two pointers approach
    int left = 0;
    int right = cleanStr.length() - 1;
    
    while (left < right) {
        if (cleanStr.charAt(left) != cleanStr.charAt(right)) {
            return false;
        }
        left++;
        right--;
    }
    
    return true;
}
```

## Garbage Collection

### What is Garbage Collection in Java?
Garbage Collection (GC) is an automatic memory management process that identifies and reclaims memory from objects no longer in use. Java uses a generational approach:
- **Young Generation**: New objects allocated here, undergoes frequent Minor GC
- **Old Generation**: Long-lived objects promoted here, undergoes less frequent Major GC
- **Metaspace**: Stores class metadata (replaced PermGen in Java 8+)

Common GC algorithms include:
- Serial GC (single-threaded)
- Parallel GC (multi-threaded)
- CMS (Concurrent Mark Sweep)
- G1 (Garbage First)
- ZGC and Shenandoah (low-latency collectors)

### How to Force Garbage Collection?
You can request (but not force) garbage collection with:
```java
System.gc();
// or
Runtime.getRuntime().gc();
```

These are only hints to the JVM, which may ignore the request. In production, it's generally better to:
- Set appropriate heap sizes (-Xms and -Xmx)
- Eliminate memory leaks
- Use tools like VisualVM to monitor memory
- Use weak references for caches
- Let the JVM manage collection automatically
