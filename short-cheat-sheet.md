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
