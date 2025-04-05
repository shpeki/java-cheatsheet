# java-interview-cheatsheet
# SOLID Principles in Java

## 1. Single Responsibility Principle (SRP)

### Theory
The Single Responsibility Principle states that a class should have only one reason to change, meaning it should have only one job or responsibility.

### Benefits
- Easier to understand and maintain
- Reduces coupling between different functionalities
- Facilitates testing and debugging
- Increases code reusability

### Good Example
```java
// User repository - responsible for data access
public class UserRepository {
    private Database db;
    
    public UserRepository() {
        db = new Database();
    }
    
    public void save(User user) {
        db.saveData(user);
    }
    
    public User findById(String userId) {
        return (User) db.getData(userId);
    }
}

// Email service - responsible for communication
public class EmailService {
    public void sendEmail(String email, String message) {
        // Email sending logic
    }
}

// Report service - responsible for report generation
public class ReportService {
    public String generateUserReport(User user) {
        return "User Report for: " + user.getName();
    }
}
```

## 2. Open/Closed Principle (OCP)

### Theory
The Open/Closed Principle states that software entities (classes, modules, functions, etc.) should be open for extension but closed for modification. This means you should be able to add new functionality without changing existing code.

### Benefits
- Reduces the risk of breaking existing functionality
- Easier to maintain and adapt to changing requirements
- Promotes the use of interfaces and abstract classes
- Supports backward compatibility

### Bad Example
```java
public class Rectangle {
    public double width;
    public double height;
}

public class Circle {
    public double radius;
}

public class AreaCalculator {
    public double calculateArea(Object shape) {
        if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            return rectangle.width * rectangle.height;
        } 
        else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            return Math.PI * circle.radius * circle.radius;
        }
        // If we add a new shape, we need to modify this class
        return 0;
    }
}
```

### Good Example
```java
public interface Shape {
    double calculateArea();
}

public class Rectangle implements Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
}

public class Circle implements Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

// To add a new shape, we don't need to modify AreaCalculator
public class Triangle implements Shape {
    private double base;
    private double height;
    
    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
}

public class AreaCalculator {
    public double calculateArea(Shape shape) {
        return shape.calculateArea();
    }
}
```

## 3. Liskov Substitution Principle (LSP)

### Theory
The Liskov Substitution Principle states that objects of a superclass should be replaceable with objects of a subclass without affecting the correctness of the program. In other words, a subclass should override the parent class methods in a way that doesn't break functionality from a client's perspective.

### Benefits
- Ensures that inheritance is used correctly
- Improves code reusability
- Prevents unexpected behaviors when using polymorphism
- Helps maintain the "is-a" relationship in inheritance

### Bad Example
```java
public class Rectangle {
    protected int width;
    protected int height;
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getArea() {
        return width * height;
    }
}

// Square is a special type of Rectangle where width equals height
public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        super.setHeight(width); // Square's height must equal its width
    }
    
    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        super.setWidth(height); // Square's width must equal its height
    }
}

// This code breaks LSP
public void testRectangleArea(Rectangle rectangle) {
    rectangle.setWidth(5);
    rectangle.setHeight(4);
    // For a rectangle, we expect area = 5 * 4 = 20
    assert rectangle.getArea() == 20; // Fails if rectangle is a Square!
}
```

### Good Example
```java
public interface Shape {
    int getArea();
}

public class Rectangle implements Shape {
    protected int width;
    protected int height;
    
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    @Override
    public int getArea() {
        return width * height;
    }
}

public class Square implements Shape {
    private int side;
    
    public Square(int side) {
        this.side = side;
    }
    
    public void setSide(int side) {
        this.side = side;
    }
    
    @Override
    public int getArea() {
        return side * side;
    }
}

// No LSP violation
public void testShapeArea(Shape shape) {
    if (shape instanceof Rectangle) {
        Rectangle rectangle = (Rectangle) shape;
        rectangle.setWidth(5);
        rectangle.setHeight(4);
        assert rectangle.getArea() == 20; // Always true for Rectangle
    }
    else if (shape instanceof Square) {
        Square square = (Square) shape;
        square.setSide(5);
        assert square.getArea() == 25; // Always true for Square
    }
}
```

## 4. Interface Segregation Principle (ISP)

### Theory
The Interface Segregation Principle states that no client should be forced to depend on methods it does not use. In other words, many client-specific interfaces are better than one general-purpose interface.

### Benefits
- Reduces coupling between components
- Creates more focused and cohesive interfaces
- Prevents "fat interfaces" that have too many methods
- Improves code maintainability and readability

### Bad Example
```java
public interface Worker {
    void work();
    void eat();
    void sleep();
}

// Human worker uses all methods
public class HumanWorker implements Worker {
    @Override
    public void work() {
        System.out.println("Human working");
    }
    
    @Override
    public void eat() {
        System.out.println("Human eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Human sleeping");
    }
}

// Robot worker is forced to implement eat() and sleep(), which don't apply
public class RobotWorker implements Worker {
    @Override
    public void work() {
        System.out.println("Robot working");
    }
    
    @Override
    public void eat() {
        // Robots don't eat, but forced to implement this
        throw new UnsupportedOperationException("Robots don't eat");
    }
    
    @Override
    public void sleep() {
        // Robots don't sleep, but forced to implement this
        throw new UnsupportedOperationException("Robots don't sleep");
    }
}
```

### Good Example
```java
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

// Human worker implements all relevant interfaces
public class HumanWorker implements Workable, Eatable, Sleepable {
    @Override
    public void work() {
        System.out.println("Human working");
    }
    
    @Override
    public void eat() {
        System.out.println("Human eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Human sleeping");
    }
}

// Robot worker only implements the relevant interface
public class RobotWorker implements Workable {
    @Override
    public void work() {
        System.out.println("Robot working");
    }
}
```

## 5. Dependency Inversion Principle (DIP)

### Theory
The Dependency Inversion Principle states that high-level modules should not depend on low-level modules. Both should depend on abstractions. Additionally, abstractions should not depend on details; details should depend on abstractions.

### Benefits
- Reduces coupling between components
- Makes the system more modular and flexible
- Facilitates testing through dependency injection
- Enables easier replacement of implementations

### Bad Example
```java
// Low-level module
public class MySQLDatabase {
    public void save(String data) {
        System.out.println("Saving data to MySQL database: " + data);
    }
}

// High-level module depends directly on low-level module
public class UserService {
    private MySQLDatabase database;
    
    public UserService() {
        this.database = new MySQLDatabase(); // Direct dependency
    }
    
    public void saveUser(User user) {
        database.save(user.toString());
    }
}
```

### Good Example
```java
// Abstraction
public interface Database {
    void save(String data);
}

// Low-level module
public class MySQLDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving data to MySQL database: " + data);
    }
}

// Another implementation
public class MongoDatabase implements Database {
    @Override
    public void save(String data) {
        System.out.println("Saving data to MongoDB: " + data);
    }
}

// High-level module depends on abstraction
public class UserService {
    private Database database; // Depends on abstraction
    
    // Dependency is injected
    public UserService(Database database) {
        this.database = database;
    }
    
    public void saveUser(User user) {
        database.save(user.toString());
    }
}

// Usage
public class Application {
    public static void main(String[] args) {
        Database database = new MySQLDatabase(); // Or new MongoDatabase()
        UserService userService = new UserService(database);
        userService.saveUser(new User("John"));
    }
}
```

## SOLID Principles in Action

When all five SOLID principles are applied together, they lead to systems that are:

1. **Modular** - composed of separate, interchangeable components
2. **Testable** - easy to write unit tests for isolated components
3. **Maintainable** - changes to one component don't affect others
4. **Extensible** - new functionality can be added without modifying existing code
5. **Reusable** - components can be reused in different contexts

In a senior Java role, you'll be expected not only to understand these principles but to actively apply them when designing systems and reviewing code.
