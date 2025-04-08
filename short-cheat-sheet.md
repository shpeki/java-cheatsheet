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

# Pass-by-Value in Java
## Summary

- Java is strictly pass-by-value.
- For primitive types, a copy of the actual value is passed.
- For objects, a copy of the reference (memory address) is passed.
- You can modify the internal state of objects in methods, but you cannot change which object the original reference points to.
- Understanding this distinction is crucial for writing correct Java code and avoiding unexpected behaviors.

# Returning Multiple Values from Methods in Java
## Comparison of Approaches

| Approach | Type Safety | Semantic Clarity | Ease of Use | Best For |
|----------|------------|-----------------|-------------|----------|
| Custom Class | High | High | Medium | Related values with clear meaning |
| Record (Java 16+) | High | High | High | Same as custom class, with less boilerplate |
| Collection/Array | Medium | Low | Medium | Multiple values of the same type |
| Map | Low | Medium | Medium | Dynamic sets of named values |
| Output Parameters | Medium | Medium | Low | Performance-critical code |
| Library Classes | Medium | Low | High | Quick solutions without custom classes |

# Static vs Dynamic Polymorphism in Java

## Key Differences

| Feature | Static Polymorphism | Dynamic Polymorphism |
|---------|---------------------|----------------------|
| Resolution Time | Compile time | Runtime |
| Implementation | Method overloading | Method overriding |
| Binding | Early binding | Late binding |
| Performance | More efficient | Slightly less efficient |
| Flexibility | Less flexible | More flexible |
| Called via | Reference type | Object type |
| Method selection based on | Method signature and argument types | Actual object type |


# Interface vs Abstract Class in Java
## Key Differences

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
