# Additional Java Interview Topics

## Java Memory Model

The Java Memory Model (JMM) defines how threads interact through memory and specifies rules for when changes to variables by one thread become visible to other threads.

### Key Concepts

1. **Happens-Before Relationship**: Establishes ordering constraints for memory operations.
   - Program order: Each action in a thread happens-before every subsequent action in the same thread
   - Monitor lock: An unlock happens-before a subsequent lock of the same monitor
   - Volatile field: A write to a volatile field happens-before every subsequent read of that field
   - Thread start: A call to Thread.start() happens-before any actions in the started thread
   - Thread termination: All actions in a thread happen-before any other thread detects that thread has terminated

2. **Memory Visibility**: Without proper synchronization, threads may see stale or partially updated values.

3. **Atomic Operations**: Some operations are guaranteed to be performed atomically:
   - Reads and writes to reference variables
   - Reads and writes to primitive variables (except `long` and `double`)
   - Reads and writes declared as `volatile`

### Synchronization Mechanisms

1. **Synchronized Blocks and Methods**:
```java
synchronized(lock) {
    // Critical section
    sharedData++;
}

public synchronized void increment() {
    sharedData++;
}
```

2. **Volatile Variables**:
```java
private volatile boolean flag = false;

// Thread 1
flag = true;

// Thread 2
if (flag) {
    // This will see the update from Thread 1
}
```

3. **Atomic Classes**:
```java
private AtomicInteger counter = new AtomicInteger(0);

public void increment() {
    counter.incrementAndGet(); // Atomic operation
}
```

4. **java.util.concurrent Utilities**:
```java
private final Lock lock = new ReentrantLock();

public void increment() {
    lock.lock();
    try {
        sharedData++;
    } finally {
        lock.unlock();
    }
}
```

### Common Concurrency Issues

1. **Race Conditions**: When the behavior depends on the relative timing of events
2. **Deadlocks**: When two or more threads are blocked forever, waiting for each other
3. **Livelocks**: Threads are actively performing operations but not making progress
4. **Starvation**: A thread is unable to gain regular access to shared resources
5. **Thread Leaks**: Threads that are created but never terminated

## Functional Programming in Java

Java 8 introduced functional programming capabilities to the language through lambda expressions, functional interfaces, and the Stream API.

### Lambda Expressions

Lambda expressions enable treating functionality as a method argument:

```java
// Traditional anonymous class
Runnable traditional = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello world");
    }
};

// Lambda expression
Runnable lambda = () -> System.out.println("Hello world");
```

### Functional Interfaces

Interfaces with exactly one abstract method, which can be implemented using lambda expressions:

1. **Predicate<T>**: Takes a value and returns a boolean
```java
Predicate<String> isEmpty = s -> s.isEmpty();
boolean result = isEmpty.test(""); // true
```

2. **Function<T, R>**: Transforms an input into an output of a different type
```java
Function<String, Integer> length = s -> s.length();
int len = length.apply("hello"); // 5
```

3. **Consumer<T>**: Performs an operation on an input without returning a result
```java
Consumer<String> printer = s -> System.out.println(s);
printer.accept("hello"); // Prints "hello"
```

4. **Supplier<T>**: Provides a value without taking any input
```java
Supplier<Double> random = () -> Math.random();
double value = random.get(); // Random double
```

5. **BiFunction<T, U, R>**: Takes two inputs and produces one output
```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
int sum = add.apply(5, 3); // 8
```

### Method References

Shorthand notation for lambdas that call a single method:

```java
// Lambda expression
Function<String, Integer> length = s -> s.length();

// Method reference
Function<String, Integer> lengthRef = String::length;
```

Types of method references:
1. Static methods: `ClassName::methodName`
2. Instance methods of a particular object: `instance::methodName`
3. Instance methods of an arbitrary object of a specific type: `ClassName::methodName`
4. Constructor: `ClassName::new`

### Stream API

Enables functional-style operations on collections:

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

// Filtering and mapping
List<String> filteredNames = names.stream()
    .filter(name -> name.length() > 4)
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// [ALICE, CHARLIE, DAVID]

// Finding elements
Optional<String> firstNameWithA = names.stream()
    .filter(name -> name.startsWith("A"))
    .findFirst();
// Optional[Alice]

// Reducing to a single value
int totalLength = names.stream()
    .mapToInt(String::length)
    .sum();
// 20
```

Key stream operations:
1. **Intermediate Operations**: `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`
2. **Terminal Operations**: `forEach`, `collect`, `reduce`, `count`, `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`

## Generics in Java

Generics enable types (classes and interfaces) to be parameters when defining classes, interfaces, and methods, providing compile-time type safety.

### Basic Syntax

```java
// Generic class
public class Box<T> {
    private T content;
    
    public Box(T content) {
        this.content = content;
    }
    
    public T getContent() {
        return content;
    }
}

// Usage
Box<String> stringBox = new Box<>("Hello");
String content = stringBox.getContent(); // No casting needed
```

### Type Erasure

Java generics are implemented using type erasure, which means generic type information is only available at compile time and is removed at runtime.

```java
// Before compilation
List<String> stringList = new ArrayList<>();
stringList.add("hello");
String s = stringList.get(0);

// After type erasure (conceptually)
List stringList = new ArrayList();
stringList.add("hello");
String s = (String) stringList.get(0);
```

### Bounded Type Parameters

Restricting the types that can be used as type arguments:

```java
// Upper bounded wildcard
public static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number number : numbers) {
        total += number.doubleValue();
    }
    return total;
}

// Lower bounded wildcard
public static void addIntegers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
}
```

### Wildcard Types

1. **Unbounded Wildcard** (`?`): When you want to use a generic type but don't care about the specific type
```java
public static void printList(List<?> list) {
    for (Object elem : list) {
        System.out.println(elem);
    }
}
```

2. **Upper Bounded Wildcard** (`? extends Type`): When you need to access values of a specific type
```java
public static double sumOfList(List<? extends Number> list) {
    double sum = 0;
    for (Number n : list) {
        sum += n.doubleValue();
    }
    return sum;
}
```

3. **Lower Bounded Wildcard** (`? super Type`): When you need to add values of a specific type
```java
public static void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
}
```

### PECS Principle

"Producer Extends, Consumer Super":
- Use `extends` when you need to get values from a structure (producer)
- Use `super` when you need to put values into a structure (consumer)
- Use unbounded wildcards when you don't need to know the type

## Java Reflection API

Reflection allows inspection and manipulation of classes, interfaces, fields, and methods at runtime.

### Key Classes in Reflection API

1. **Class<?>**: Represents classes and interfaces
2. **Field**: Represents class fields
3. **Method**: Represents class methods
4. **Constructor**: Represents class constructors
5. **Modifier**: Contains methods and constants for working with modifiers

### Common Reflection Operations

1. **Getting Class Information**:
```java
// From an object
Object obj = "Hello";
Class<?> cls = obj.getClass();

// From a class name
Class<?> stringClass = Class.forName("java.lang.String");

// From a class literal
Class<?> intClass = int.class;
Class<?> stringArrayClass = String[].class;
```

2. **Examining Fields**:
```java
Class<?> cls = Person.class;

// Get public fields
Field[] publicFields = cls.getFields();

// Get all fields (including private, protected)
Field[] allFields = cls.getDeclaredFields();

// Get specific field
Field nameField = cls.getDeclaredField("name");
nameField.setAccessible(true); // Allow access to private field
```

3. **Accessing and Modifying Fields**:
```java
Person person = new Person("John");
Field nameField = Person.class.getDeclaredField("name");
nameField.setAccessible(true);

String name = (String) nameField.get(person);
nameField.set(person, "Alice");
```

4. **Invoking Methods**:
```java
Person person = new Person("John");
Method method = Person.class.getDeclaredMethod("setName", String.class);
method.invoke(person, "Alice");
```

5. **Creating Objects**:
```java
// Using constructor
Constructor<Person> constructor = Person.class.getConstructor(String.class);
Person person = constructor.newInstance("John");

// Using Class.newInstance() (deprecated in Java 9)
Person person2 = Person.class.newInstance();
```

### Reflection Use Cases

1. **Frameworks**: JUnit, Spring, Hibernate heavily use reflection
2. **Debugging and Testing Tools**
3. **Runtime Type Introspection**
4. **Dynamic Proxy Creation**

### Drawbacks of Reflection

1. **Performance Overhead**: Slower than direct method calls
2. **Security Restrictions**: May be limited by security managers
3. **Loss of Compile-Time Checking**: Errors move from compile-time to runtime
4. **Exposure of Internal Representation**: Breaks encapsulation

## Design Patterns in Java

Design patterns are reusable solutions to common software design problems. Here are some of the most important ones for Java interviews:

### Creational Patterns

1. **Singleton Pattern**
   - Ensures a class has only one instance
   - Provides a global point of access to it

```java
public class Singleton {
    private static Singleton instance;
    
    private Singleton() {
        // Private constructor
    }
    
    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}

// Thread-safe with double-checked locking
public class BetterSingleton {
    private static volatile BetterSingleton instance;
    
    private BetterSingleton() {}
    
    public static BetterSingleton getInstance() {
        if (instance == null) {
            synchronized (BetterSingleton.class) {
                if (instance == null) {
                    instance = new BetterSingleton();
                }
            }
        }
        return instance;
    }
}

// Best approach (Java 5+)
public enum EnumSingleton {
    INSTANCE;
    
    public void doSomething() {
        // Singleton operation
    }
}
```

2. **Factory Method Pattern**
   - Creates objects without specifying the exact class of object to be created
   - Defers instantiation to subclasses

```java
interface Product {
    void operate();
}

class ConcreteProductA implements Product {
    @Override
    public void operate() {
        System.out.println("ConcreteProductA operation");
    }
}

class ConcreteProductB implements Product {
    @Override
    public void operate() {
        System.out.println("ConcreteProductB operation");
    }
}

abstract class Creator {
    public abstract Product createProduct();
    
    public void someOperation() {
        Product product = createProduct();
        product.operate();
    }
}

class ConcreteCreatorA extends Creator {
    @Override
    public Product createProduct() {
        return new ConcreteProductA();
    }
}

class ConcreteCreatorB extends Creator {
    @Override
    public Product createProduct() {
        return new ConcreteProductB();
    }
}
```

3. **Builder Pattern**
   - Separates the construction of complex objects from their representation
   - Uses a step-by-step approach to build objects

```java
public class Person {
    private final String firstName;
    private final String lastName;
    private final int age;
    private final String address;
    private final String phone;
    
    private Person(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.age = builder.age;
        this.address = builder.address;
        this.phone = builder.phone;
    }
    
    public static class Builder {
        private final String firstName;
        private final String lastName;
        private int age;
        private String address;
        private String phone;
        
        public Builder(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        public Builder age(int age) {
            this.age = age;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Person build() {
            return new Person(this);
        }
    }
}

// Usage
Person person = new Person.Builder("John", "Doe")
    .age(30)
    .address("123 Street")
    .phone("555-1234")
    .build();
```

### Structural Patterns

1. **Adapter Pattern**
   - Allows incompatible interfaces to work together
   - Converts one interface to another

```java
// Target interface
interface MediaPlayer {
    void play(String audioType, String fileName);
}

// Adaptee interface
interface AdvancedMediaPlayer {
    void playVlc(String fileName);
    void playMp4(String fileName);
}

// Concrete adaptee
class VlcPlayer implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String fileName) {
        System.out.println("Playing vlc file: " + fileName);
    }
    
    @Override
    public void playMp4(String fileName) {
        // Do nothing
    }
}

// Adapter
class MediaAdapter implements MediaPlayer {
    private AdvancedMediaPlayer advancedMusicPlayer;
    
    public MediaAdapter(String audioType) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedMusicPlayer = new VlcPlayer();
        } else if (audioType.equalsIgnoreCase("mp4")) {
            advancedMusicPlayer = new Mp4Player();
        }
    }
    
    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedMusicPlayer.playVlc(fileName);
        } else if (audioType.equalsIgnoreCase("mp4")) {
            advancedMusicPlayer.playMp4(fileName);
        }
    }
}

// Client
class AudioPlayer implements MediaPlayer {
    private MediaAdapter mediaAdapter;
    
    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing mp3 file: " + fileName);
        } else if (audioType.equalsIgnoreCase("vlc") || audioType.equalsIgnoreCase("mp4")) {
            mediaAdapter = new MediaAdapter(audioType);
            mediaAdapter.play(audioType, fileName);
        } else {
            System.out.println("Invalid media. " + audioType + " format not supported");
        }
    }
}
```

2. **Decorator Pattern**
   - Attaches additional responsibilities to objects dynamically
   - Provides a flexible alternative to subclassing

```java
// Component interface
interface Coffee {
    double getCost();
    String getDescription();
}

// Concrete component
class SimpleCoffee implements Coffee {
    @Override
    public double getCost() {
        return 1.0;
    }
    
    @Override
    public String getDescription() {
        return "Simple coffee";
    }
}

// Decorator
abstract class CoffeeDecorator implements Coffee {
    protected final Coffee decoratedCoffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }
    
    @Override
    public double getCost() {
        return decoratedCoffee.getCost();
    }
    
    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription();
    }
}

// Concrete decorators
class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.5;
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", milk";
    }
}

class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.2;
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", sugar";
    }
}

// Usage
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);
System.out.println(coffee.getDescription() + " $" + coffee.getCost());
// "Simple coffee, milk, sugar $1.7"
```

### Behavioral Patterns

1. **Observer Pattern**
   - Defines a one-to-many dependency between objects
   - When one object changes state, all its dependents are notified

```java
import java.util.ArrayList;
import java.util.List;

// Subject interface
interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

// Observer interface
interface Observer {
    void update(float temp, float humidity, float pressure);
}

// Concrete subject
class WeatherData implements Subject {
    private List<Observer> observers;
    private float temperature;
    private float humidity;
    private float pressure;
    
    public WeatherData() {
        observers = new ArrayList<>();
    }
    
    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }
    
    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        notifyObservers();
    }
}

// Concrete observer
class DisplayElement implements Observer {
    private float temperature;
    private float humidity;
    private Subject weatherData;
    
    public DisplayElement(Subject weatherData) {
        this.weatherData = weatherData;
        weatherData.registerObserver(this);
    }
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        display();
    }
    
    public void display() {
        System.out.println("Current conditions: " + temperature + "F degrees and " + humidity + "% humidity");
    }
}

// Usage
WeatherData weatherData = new WeatherData();
DisplayElement currentDisplay = new DisplayElement(weatherData);
weatherData.setMeasurements(80, 65, 30.4f);
// "Current conditions: 80.0F degrees and 65.0% humidity"
```

2. **Strategy Pattern**
   - Defines a family of algorithms
   - Encapsulates each algorithm
   - Makes them interchangeable within that family

```java
// Strategy interface
interface PaymentStrategy {
    void pay(int amount);
}

// Concrete strategies
class CreditCardStrategy implements PaymentStrategy {
    private String name;
    private String cardNumber;
    
    public CreditCardStrategy(String name, String cardNumber) {
        this.name = name;
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void pay(int amount) {
        System.out.println(amount + " paid with credit card");
    }
}

class PayPalStrategy implements PaymentStrategy {
    private String email;
    
    public PayPalStrategy(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(int amount) {
        System.out.println(amount + " paid using PayPal");
    }
}

// Context
class ShoppingCart {
    private PaymentStrategy paymentStrategy;
    
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public void checkout(int amount) {
        paymentStrategy.pay(amount);
    }
}

// Usage
ShoppingCart cart = new ShoppingCart();
cart.setPaymentStrategy(new CreditCardStrategy("John Doe", "1234567890"));
cart.checkout(100);
// "100 paid with credit card"

cart.setPaymentStrategy(new PayPalStrategy("john@example.com"));
cart.checkout(200);
// "200 paid using PayPal"
```

## Java 8+ New Features

Java has seen significant updates since Java 8, introducing many new features that modern Java developers should be familiar with.

### Java 8 (2014)

1. **Lambda Expressions**
2. **Stream API**
3. **Optional**
```java
Optional<String> optional = Optional.of("value");
optional.ifPresent(System.out::println);
String result = optional.orElse("default");
```

4. **Default Methods in Interfaces**
```java
interface Vehicle {
    void start();
    
    default void stop() {
        System.out.println("Default stop implementation");
    }
}
```

5. **Method References**

6. **New Date and Time API**
```java
LocalDate date = LocalDate.now();
LocalTime time = LocalTime.now();
LocalDateTime dateTime = LocalDateTime.now();
ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
```

### Java 9 (2017)

1. **Module System (Project Jigsaw)**
```java
// module-info.java
module com.mycompany.app {
    requires java.sql;
    exports com.mycompany.app.api;
}
```

2. **Factory Methods for Collections**
```java
List<String> list = List.of("a", "b", "c");
Set<String> set = Set.of("a", "b", "c");
Map<String, Integer> map = Map.of("a", 1, "b", 2);
```

3. **Private Methods in Interfaces**
```java
interface MyInterface {
    default void defaultMethod() {
        privateMethod();
    }
    
    private void privateMethod() {
        System.out.println("Private method in interface");
    }
}
```

4. **Try-With-Resources Improvements**
```java
// Java 7
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    // Use reader
}

// Java 9
BufferedReader reader = new BufferedReader(new FileReader(file));
try (reader) {
    // Use reader
}
```

### Java 10 (2018)

1. **Local Variable Type Inference (var)**
```java
var list = new ArrayList<String>();  // Inferred as ArrayList<String>
var map = Map.of("key", "value");    // Inferred as Map<String, String>
```

### Java 11 (2018)

1. **String Methods**
```java
String multiline = "line1\nline2\nline3";
List<String> lines = multiline.lines().collect(Collectors.toList());

String whitespace = "  text  ";
String trimmed = whitespace.strip();
boolean blank = whitespace.isBlank();
```

2. **HTTP Client (Standard)**
```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://example.com"))
    .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

### Java 14 (2020)

1. **Switch Expressions**
```java
// Traditional switch statement
switch (day) {
    case MONDAY:
    case FRIDAY:
        System.out.println("Working day");
        break;
    case SATURDAY:
    case SUNDAY:
        System.out.println("Weekend");
        break;
}

// New switch expression
String result = switch (day) {
    case MONDAY, FRIDAY -> "Working day";
    case SATURDAY, SUNDAY -> "Weekend";
    default -> "Unknown";
};
```

### Java 16 (2021)

1. **Records**
```java
record Person(String name, int age) {
    // Canonical constructor, accessors, equals, hashCode, and toString
    // are automatically generated
    
    // You can add validation
    public Person {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
    
    // You can add methods
    public boolean isAdult() {
        return age >= 18;
    }
}

// Usage
Person person = new Person("John", 30);
String name = person.name();
int age = person.age();
```

### Java 17 (2021) - LTS

1. **Sealed Classes**
```java
// Define which classes can extend/implement
sealed interface Shape permits Circle, Rectangle, Square {
    double area();
}

// Must be final, sealed, or non-sealed
final class Circle implements Shape {
    private final double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

// Must be final, sealed, or non-sealed
sealed class Rectangle implements Shape permits Square {
    protected final double width;
    protected final double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double area() {
        return width * height;
    }
}

// Must be final, sealed, or non-sealed
final class Square extends Rectangle {
    public Square(double side) {
        super(side, side);
    }
}
```

2. **Pattern Matching for switch (Preview)**
```java
Object obj = "Hello";
String result = switch (obj) {
    case Integer i -> "Integer: " + i;
    case String s -> "String: " + s;
    case null -> "null";
    default -> "Unknown";
};
```

## Testing in Java

### JUnit 5

JUnit 5 is the latest version of the popular Java testing framework. It consists of several modules from three subprojects: JUnit Platform, JUnit Jupiter, and JUnit Vintage.

#### Basic Test Structure

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {
    @Test
    void addition() {
        Calculator calculator = new Calculator();
        assertEquals(5, calculator.add(2, 3), "2 + 3 should equal 5");
    }
    
    @Test
    void divisionByZero() {
        Calculator calculator = new Calculator();
        assertThrows(ArithmeticException.class, () -> {
            calculator.divide(10, 0);
        });
    }
}
```

#### JUnit 5 Annotations

1. **@Test**: Marks a method as a test method
2. **@BeforeEach**: Method will be executed before each test
3. **@AfterEach**: Method will be executed after each test
4. **@BeforeAll**: Static method executed once before all tests
5. **@AfterAll**: Static method executed once after all tests
6. **@Disabled**: Disables a test method or class
7. **@DisplayName**: Custom name for test display
8. **@ParameterizedTest**: Test that runs multiple times with different parameters
9. **@RepeatedTest**: Test that runs repeatedly

#### Assertion Methods

```java
// Basic assertions
assertEquals(expected, actual);
assertNotEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);
assertSame(expected, actual);
assertNotSame(expected, actual);
assertArrayEquals(expected, actual);

// Exception testing
assertThrows(NullPointerException.class, () -> { throw new NullPointerException(); });

// Grouped assertions
assertAll(
    () -> assertEquals(2, 1 + 1),
    () -> assertTrue(4 > 3),
    () -> assertNull(null)
);

// Timeout testing
assertTimeout(Duration.ofMillis(100), () -> {
    // Code that should complete within 100 ms
});
```

#### Parameterized Tests

```java
@ParameterizedTest
@ValueSource(strings = {"racecar", "radar", "able was I ere I saw elba"})
void palindromes(String candidate) {
    assertTrue(isPalindrome(candidate));
}

@ParameterizedTest
@CsvSource({"1, 1, 2", "5, 3, 8", "10, -2, 8"})
void add(int a, int b, int expected) {
    assertEquals(expected, Calculator.add(a, b));
}

@ParameterizedTest
@MethodSource("provideTestData")
void testWithMethodSource(String input, boolean expected) {
    assertEquals(expected, Validator.isValid(input));
}

static Stream<Arguments> provideTestData() {
    return Stream.of(
        Arguments.of("valid", true),
        Arguments.of("invalid", false),
        Arguments.of("", false)
    );
}
```

### Mockito

Mockito is a popular mocking framework that lets you create and configure mock objects. It's commonly used with JUnit.

#### Basic Mocking

```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    @Test
    void placeOrder() {
        // Create mock
        PaymentService paymentService = mock(PaymentService.class);
        
        // Set up behavior
        when(paymentService.processPayment(anyDouble())).thenReturn(true);
        
        // Create service with mock dependency
        OrderService orderService = new OrderService(paymentService);
        
        // Test the service
        boolean result = orderService.placeOrder("Product", 50.0);
        
        // Verify the result
        assertTrue(result);
        
        // Verify interactions with mock
        verify(paymentService).processPayment(50.0);
    }
}
```

#### Argument Matchers

```java
// Any value of the specified type
when(service.process(any(String.class))).thenReturn(result);

// Primitive matchers
when(service.process(anyInt())).thenReturn(result);
when(service.process(anyDouble())).thenReturn(result);
when(service.process(anyBoolean())).thenReturn(result);

// Collection matchers
when(service.process(anyList())).thenReturn(result);
when(service.process(anyMap())).thenReturn(result);

// Specific value matchers
when(service.process(eq("expected"))).thenReturn(result);
when(service.process(startsWith("prefix"))).thenReturn(result);
when(service.process(endsWith("suffix"))).thenReturn(result);
when(service.process(contains("substring"))).thenReturn(result);
```

#### Verifying Behavior

```java
// Verify method was called exactly once
verify(mockObject).someMethod();

// Verify method was called exactly n times
verify(mockObject, times(3)).someMethod();

// Verify method was never called
verify(mockObject, never()).someMethod();

// Verify method was called at least/at most n times
verify(mockObject, atLeast(2)).someMethod();
verify(mockObject, atMost(5)).someMethod();

// Verify order of method calls
InOrder inOrder = inOrder(mockObject1, mockObject2);
inOrder.verify(mockObject1).someMethod();
inOrder.verify(mockObject2).otherMethod();

// Verify no more interactions
verifyNoMoreInteractions(mockObject);
```

#### Stubbing Void Methods

```java
// For void methods that don't return anything
doNothing().when(mockObject).voidMethod();

// For void methods that should throw an exception
doThrow(new RuntimeException()).when(mockObject).voidMethod();
```

#### Capturing Arguments

```java
@Test
void captureArgument() {
    UserService userService = mock(UserService.class);
    
    // Create an argument captor
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    
    // Call the method that should pass a User object
    UserController controller = new UserController(userService);
    controller.createUser("John", "john@example.com");
    
    // Capture the argument passed to saveUser
    verify(userService).saveUser(userCaptor.capture());
    
    // Assert properties of the captured User object
    User capturedUser = userCaptor.getValue();
    assertEquals("John", capturedUser.getName());
    assertEquals("john@example.com", capturedUser.getEmail());
}
```

### Test-Driven Development (TDD)

TDD is a software development process that relies on a very short development cycle:

1. **Write a test** that defines a function or improvements to a function, which will fail initially because the function is not implemented or doesn't meet the requirements yet.
2. **Write the minimum amount of code** to pass the test.
3. **Refactor the code** to meet the desired standards while ensuring the tests still pass.

#### TDD Example

```java
// Step 1: Write the test first
@Test
void shouldAddTwoNumbers() {
    Calculator calculator = new Calculator();
    assertEquals(5, calculator.add(2, 3));
}

// Step 2: Implement the code to pass the test
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

// Step 3: Refactor if needed while keeping tests passing
```

### Test Doubles

Test doubles are objects that stand in for real objects in tests:

1. **Dummy**: Objects passed around but never used
2. **Fake**: Working implementations but with shortcuts (e.g., in-memory database)
3. **Stub**: Provide predetermined answers to calls
4. **Spy**: Record information about how they were called
5. **Mock**: Pre-programmed with expectations of calls and proper responses
