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
