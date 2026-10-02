# Java Design Patterns Reference Guide

A comprehensive guide to design patterns in Java, organized by pattern type with practical examples and use cases.

## Table of Contents

1. [Creational Patterns](#creational-patterns)
2. [Structural Patterns](#structural-patterns)
3. [Behavioral Patterns](#behavioral-patterns)

---

## Creational Patterns

Creational patterns provide various object creation mechanisms, which increase flexibility and reuse of existing code.

### 1. Singleton Pattern

**Purpose**: Ensures a class has only one instance and provides a global access point to it.

**When to Use**: 
- Database connections
- Logger instances
- Configuration objects
- Cache implementations

```java
public class Singleton {
    private static volatile Singleton instance;
    
    private Singleton() {
        // Private constructor prevents instantiation
    }
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    
    public void doSomething() {
        System.out.println("Singleton instance is working!");
    }
}

// Usage
public class SingletonDemo {
    public static void main(String[] args) {
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        
        System.out.println(s1 == s2); // true - same instance
        s1.doSomething();
    }
}
```

### 2. Factory Method Pattern

**Purpose**: Provides an interface for creating objects in a superclass, but allows subclasses to alter the type of objects that will be created.

**When to Use**:
- When you don't know beforehand the exact types of objects your code should work with
- When you want to provide users with a way to extend internal components

```java
// Product interface
interface Animal {
    void makeSound();
}

// Concrete products
class Dog implements Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
}

class Cat implements Animal {
    @Override
    public void makeSound() {
        System.out.println("Meow!");
    }
}

// Creator abstract class
abstract class AnimalFactory {
    public abstract Animal createAnimal();
    
    public void petAnimal() {
        Animal animal = createAnimal();
        animal.makeSound();
    }
}

// Concrete creators
class DogFactory extends AnimalFactory {
    @Override
    public Animal createAnimal() {
        return new Dog();
    }
}

class CatFactory extends AnimalFactory {
    @Override
    public Animal createAnimal() {
        return new Cat();
    }
}

// Usage
public class FactoryMethodDemo {
    public static void main(String[] args) {
        AnimalFactory dogFactory = new DogFactory();
        AnimalFactory catFactory = new CatFactory();
        
        dogFactory.petAnimal(); // Woof!
        catFactory.petAnimal(); // Meow!
    }
}
```

### 3. Abstract Factory Pattern

**Purpose**: Lets you produce families of related objects without specifying their concrete classes.

**When to Use**:
- When your code needs to work with various families of related products
- When you want to ensure products from a factory are compatible

```java
// Abstract products
interface Button {
    void paint();
}

interface Checkbox {
    void paint();
}

// Windows family
class WindowsButton implements Button {
    @Override
    public void paint() {
        System.out.println("Rendering Windows button");
    }
}

class WindowsCheckbox implements Checkbox {
    @Override
    public void paint() {
        System.out.println("Rendering Windows checkbox");
    }
}

// Mac family
class MacButton implements Button {
    @Override
    public void paint() {
        System.out.println("Rendering Mac button");
    }
}

class MacCheckbox implements Checkbox {
    @Override
    public void paint() {
        System.out.println("Rendering Mac checkbox");
    }
}

// Abstract factory
interface GUIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete factories
class WindowsFactory implements GUIFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }
    
    @Override
    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}

class MacFactory implements GUIFactory {
    @Override
    public Button createButton() {
        return new MacButton();
    }
    
    @Override
    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}

// Usage
public class AbstractFactoryDemo {
    public static void main(String[] args) {
        GUIFactory factory = new WindowsFactory();
        
        Button button = factory.createButton();
        Checkbox checkbox = factory.createCheckbox();
        
        button.paint();    // Rendering Windows button
        checkbox.paint();  // Rendering Windows checkbox
    }
}
```

### 4. Builder Pattern

**Purpose**: Lets you construct complex objects step by step. Allows you to produce different types and representations using the same construction code.

**When to Use**:
- Creating complex objects with many optional parameters
- When constructor has too many parameters
- When object creation involves multiple steps

```java
class Computer {
    private String CPU;
    private String RAM;
    private String storage;
    private String GPU;
    private boolean hasWiFi;
    private boolean hasBluetooth;
    
    private Computer(ComputerBuilder builder) {
        this.CPU = builder.CPU;
        this.RAM = builder.RAM;
        this.storage = builder.storage;
        this.GPU = builder.GPU;
        this.hasWiFi = builder.hasWiFi;
        this.hasBluetooth = builder.hasBluetooth;
    }
    
    public static class ComputerBuilder {
        // Required parameters
        private String CPU;
        private String RAM;
        
        // Optional parameters
        private String storage = "256GB SSD";
        private String GPU = "Integrated";
        private boolean hasWiFi = false;
        private boolean hasBluetooth = false;
        
        public ComputerBuilder(String CPU, String RAM) {
            this.CPU = CPU;
            this.RAM = RAM;
        }
        
        public ComputerBuilder setStorage(String storage) {
            this.storage = storage;
            return this;
        }
        
        public ComputerBuilder setGPU(String GPU) {
            this.GPU = GPU;
            return this;
        }
        
        public ComputerBuilder enableWiFi() {
            this.hasWiFi = true;
            return this;
        }
        
        public ComputerBuilder enableBluetooth() {
            this.hasBluetooth = true;
            return this;
        }
        
        public Computer build() {
            return new Computer(this);
        }
    }
    
    @Override
    public String toString() {
        return "Computer{" +
                "CPU='" + CPU + '\'' +
                ", RAM='" + RAM + '\'' +
                ", storage='" + storage + '\'' +
                ", GPU='" + GPU + '\'' +
                ", hasWiFi=" + hasWiFi +
                ", hasBluetooth=" + hasBluetooth +
                '}';
    }
}

// Usage
public class BuilderDemo {
    public static void main(String[] args) {
        Computer gamingPC = new Computer.ComputerBuilder("Intel i9", "32GB")
                .setStorage("1TB NVMe SSD")
                .setGPU("RTX 4080")
                .enableWiFi()
                .enableBluetooth()
                .build();
        
        Computer officePC = new Computer.ComputerBuilder("Intel i5", "16GB")
                .enableWiFi()
                .build();
        
        System.out.println(gamingPC);
        System.out.println(officePC);
    }
}
```

### 5. Prototype Pattern

**Purpose**: Lets you copy existing objects without making your code dependent on their classes.

**When to Use**:
- When object creation is expensive
- When you need to create objects similar to existing ones
- When you want to avoid subclassing

```java
abstract class Shape implements Cloneable {
    protected String type;
    protected int x, y;
    protected String color;
    
    public abstract void draw();
    
    @Override
    public Shape clone() {
        try {
            return (Shape) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Getters and setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setColor(String color) { this.color = color; }
    public String getType() { return type; }
}

class Circle extends Shape {
    private int radius;
    
    public Circle() {
        this.type = "Circle";
    }
    
    public Circle(Circle circle) {
        this.type = circle.type;
        this.x = circle.x;
        this.y = circle.y;
        this.color = circle.color;
        this.radius = circle.radius;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing " + color + " " + type + 
                          " at (" + x + "," + y + ") with radius " + radius);
    }
    
    public void setRadius(int radius) { this.radius = radius; }
    
    @Override
    public Circle clone() {
        return new Circle(this);
    }
}

class Rectangle extends Shape {
    private int width, height;
    
    public Rectangle() {
        this.type = "Rectangle";
    }
    
    public Rectangle(Rectangle rectangle) {
        this.type = rectangle.type;
        this.x = rectangle.x;
        this.y = rectangle.y;
        this.color = rectangle.color;
        this.width = rectangle.width;
        this.height = rectangle.height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing " + color + " " + type + 
                          " at (" + x + "," + y + ") with size " + width + "x" + height);
    }
    
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    
    @Override
    public Rectangle clone() {
        return new Rectangle(this);
    }
}

// Usage
public class PrototypeDemo {
    public static void main(String[] args) {
        Circle circle1 = new Circle();
        circle1.setX(10);
        circle1.setY(20);
        circle1.setColor("Red");
        circle1.setRadius(5);
        
        Circle circle2 = circle1.clone();
        circle2.setColor("Blue");
        circle2.setX(30);
        
        circle1.draw(); // Drawing Red Circle at (10,20) with radius 5
        circle2.draw(); // Drawing Blue Circle at (30,20) with radius 5
    }
}
```

---

## Structural Patterns

Structural patterns explain how to assemble objects and classes into larger structures while keeping these structures flexible and efficient.

### 1. Adapter Pattern

**Purpose**: Allows objects with incompatible interfaces to collaborate.

**When to Use**:
- When you want to use existing class with incompatible interface
- When you want to create reusable class that cooperates with unrelated classes

```java
// Target interface
interface MediaPlayer {
    void play(String audioType, String fileName);
}

// Adaptee classes
class Mp4Player {
    public void playMp4(String fileName) {
        System.out.println("Playing MP4 file: " + fileName);
    }
}

class VlcPlayer {
    public void playVlc(String fileName) {
        System.out.println("Playing VLC file: " + fileName);
    }
}

// Adapter
class MediaAdapter implements MediaPlayer {
    private Mp4Player mp4Player;
    private VlcPlayer vlcPlayer;
    
    public MediaAdapter(String audioType) {
        if (audioType.equalsIgnoreCase("mp4")) {
            mp4Player = new Mp4Player();
        } else if (audioType.equalsIgnoreCase("vlc")) {
            vlcPlayer = new VlcPlayer();
        }
    }
    
    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("mp4")) {
            mp4Player.playMp4(fileName);
        } else if (audioType.equalsIgnoreCase("vlc")) {
            vlcPlayer.playVlc(fileName);
        }
    }
}

// Context
class AudioPlayer implements MediaPlayer {
    private MediaAdapter mediaAdapter;
    
    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing MP3 file: " + fileName);
        } else if (audioType.equalsIgnoreCase("mp4") || 
                   audioType.equalsIgnoreCase("vlc")) {
            mediaAdapter = new MediaAdapter(audioType);
            mediaAdapter.play(audioType, fileName);
        } else {
            System.out.println("Invalid media. " + audioType + " format not supported");
        }
    }
}

// Usage
public class AdapterDemo {
    public static void main(String[] args) {
        AudioPlayer audioPlayer = new AudioPlayer();
        
        audioPlayer.play("mp3", "song.mp3");   // Playing MP3 file: song.mp3
        audioPlayer.play("mp4", "video.mp4");  // Playing MP4 file: video.mp4
        audioPlayer.play("vlc", "movie.vlc");  // Playing VLC file: movie.vlc
        audioPlayer.play("avi", "clip.avi");   // Invalid media. avi format not supported
    }
}
```

### 2. Decorator Pattern

**Purpose**: Lets you attach new behaviors to objects by placing these objects inside special wrapper objects that contain the behaviors.

**When to Use**:
- When you want to add behavior to objects without altering their structure
- When extension by subclassing would be impractical

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
        return 2.0;
    }
    
    @Override
    public String getDescription() {
        return "Simple Coffee";
    }
}

// Base decorator
abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
    
    @Override
    public double getCost() {
        return coffee.getCost();
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription();
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
        return super.getDescription() + ", Milk";
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
        return super.getDescription() + ", Sugar";
    }
}

class WhipDecorator extends CoffeeDecorator {
    public WhipDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 0.7;
    }
    
    @Override
    public String getDescription() {
        return super.getDescription() + ", Whip";
    }
}

// Usage
public class DecoratorDemo {
    public static void main(String[] args) {
        Coffee coffee = new SimpleCoffee();
        System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
        
        coffee = new MilkDecorator(coffee);
        System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
        
        coffee = new SugarDecorator(coffee);
        System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
        
        coffee = new WhipDecorator(coffee);
        System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
        
        // Output:
        // Simple Coffee - $2.0
        // Simple Coffee, Milk - $2.5
        // Simple Coffee, Milk, Sugar - $2.7
        // Simple Coffee, Milk, Sugar, Whip - $3.4
    }
}
```

### 3. Facade Pattern

**Purpose**: Provides a simplified interface to a library, framework, or any other complex set of classes.

**When to Use**:
- When you want to provide a simple interface to a complex subsystem
- When there are many dependencies between clients and implementation classes

```java
// Complex subsystem classes
class CPU {
    public void freeze() { System.out.println("CPU: Freezing..."); }
    public void jump(long position) { System.out.println("CPU: Jumping to " + position); }
    public void execute() { System.out.println("CPU: Executing..."); }
}

class Memory {
    public void load(long position, byte[] data) {
        System.out.println("Memory: Loading data at position " + position);
    }
}

class HardDrive {
    public byte[] read(long lba, int size) {
        System.out.println("HardDrive: Reading " + size + " bytes from LBA " + lba);
        return new byte[size];
    }
}

// Facade
class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;
    
    public ComputerFacade() {
        this.cpu = new CPU();
        this.memory = new Memory();
        this.hardDrive = new HardDrive();
    }
    
    public void start() {
        System.out.println("Computer: Starting up...");
        cpu.freeze();
        memory.load(0, hardDrive.read(0, 1024));
        cpu.jump(0);
        cpu.execute();
        System.out.println("Computer: Started successfully!");
    }
}

// Usage
public class FacadeDemo {
    public static void main(String[] args) {
        ComputerFacade computer = new ComputerFacade();
        computer.start();
        
        // Output:
        // Computer: Starting up...
        // CPU: Freezing...
        // HardDrive: Reading 1024 bytes from LBA 0
        // Memory: Loading data at position 0
        // CPU: Jumping to 0
        // CPU: Executing...
        // Computer: Started successfully!
    }
}
```

### 4. Composite Pattern

**Purpose**: Lets you compose objects into tree structures and then work with these structures as if they were individual objects.

**When to Use**:
- When you need to implement a tree-like object structure
- When you want clients to treat individual objects and compositions uniformly

```java
import java.util.*;

// Component
abstract class FileSystemItem {
    protected String name;
    
    public FileSystemItem(String name) {
        this.name = name;
    }
    
    public abstract void display(String indent);
    public abstract long getSize();
}

// Leaf
class File extends FileSystemItem {
    private long size;
    
    public File(String name, long size) {
        super(name);
        this.size = size;
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " bytes)");
    }
    
    @Override
    public long getSize() {
        return size;
    }
}

// Composite
class Directory extends FileSystemItem {
    private List<FileSystemItem> items = new ArrayList<>();
    
    public Directory(String name) {
        super(name);
    }
    
    public void add(FileSystemItem item) {
        items.add(item);
    }
    
    public void remove(FileSystemItem item) {
        items.remove(item);
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemItem item : items) {
            item.display(indent + "  ");
        }
    }
    
    @Override
    public long getSize() {
        return items.stream().mapToLong(FileSystemItem::getSize).sum();
    }
}

// Usage
public class CompositeDemo {
    public static void main(String[] args) {
        // Create files
        File file1 = new File("document.txt", 1024);
        File file2 = new File("image.jpg", 2048);
        File file3 = new File("config.xml", 512);
        
        // Create directories
        Directory root = new Directory("root");
        Directory docs = new Directory("documents");
        Directory images = new Directory("images");
        
        // Build tree structure
        root.add(file3);
        root.add(docs);
        root.add(images);
        
        docs.add(file1);
        images.add(file2);
        
        // Display structure
        root.display("");
        System.out.println("Total size: " + root.getSize() + " bytes");
        
        // Output:
        // 📁 root/
        //   📄 config.xml (512 bytes)
        //   📁 documents/
        //     📄 document.txt (1024 bytes)
        //   📁 images/
        //     📄 image.jpg (2048 bytes)
        // Total size: 3584 bytes
    }
}
```

### 5. Proxy Pattern

**Purpose**: Lets you provide a substitute or placeholder for another object. A proxy controls access to the original object.

**When to Use**:
- Lazy initialization (virtual proxy)
- Access control (protection proxy)
- Caching (caching proxy)
- Logging requests (logging proxy)

```java
// Subject interface
interface Image {
    void display();
}

// Real subject
class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();
    }
    
    private void loadFromDisk() {
        System.out.println("Loading image: " + filename);
        // Simulate expensive loading operation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void display() {
        System.out.println("Displaying image: " + filename);
    }
}

// Proxy
class ProxyImage implements Image {
    private RealImage realImage;
    private String filename;
    
    public ProxyImage(String filename) {
        this.filename = filename;
    }
    
    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

// Usage
public class ProxyDemo {
    public static void main(String[] args) {
        System.out.println("Creating proxy images...");
        Image image1 = new ProxyImage("photo1.jpg");
        Image image2 = new ProxyImage("photo2.jpg");
        
        System.out.println("\nFirst display calls:");
        image1.display(); // Loading happens here
        image2.display(); // Loading happens here
        
        System.out.println("\nSecond display calls:");
        image1.display(); // No loading, uses cached image
        image2.display(); // No loading, uses cached image
    }
}
```

---

## Behavioral Patterns

Behavioral patterns are concerned with algorithms and the assignment of responsibilities between objects.

### 1. Observer Pattern

**Purpose**: Lets you define a subscription mechanism to notify multiple objects about any events that happen to the object they're observing.

**When to Use**:
- When changes to one object require changing others, and you don't know how many objects need to be changed
- When an object should notify other objects without making assumptions about who these objects are

```java
import java.util.*;

// Observer interface
interface Observer {
    void update(String message);
}

// Subject interface
interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}

// Concrete subject
class NewsAgency implements Subject {
    private String news;
    private List<Observer> observers = new ArrayList<>();
    
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(news);
        }
    }
    
    public void setNews(String news) {
        this.news = news;
        notifyObservers();
    }
}

// Concrete observers
class NewsChannel implements Observer {
    private String name;
    private String news;
    
    public NewsChannel(String name) {
        this.name = name;
    }
    
    @Override
    public void update(String news) {
        this.news = news;
        System.out.println(name + " received news: " + news);
    }
}

class OnlinePortal implements Observer {
    private String name;
    private String news;
    
    public OnlinePortal(String name) {
        this.name = name;
    }
    
    @Override
    public void update(String news) {
        this.news = news;
        System.out.println(name + " portal updated with: " + news);
    }
}

// Usage
public class ObserverDemo {
    public static void main(String[] args) {
        NewsAgency agency = new NewsAgency();
        
        NewsChannel cnn = new NewsChannel("CNN");
        NewsChannel bbc = new NewsChannel("BBC");
        OnlinePortal yahoo = new OnlinePortal("Yahoo News");
        
        agency.addObserver(cnn);
        agency.addObserver(bbc);
        agency.addObserver(yahoo);
        
        agency.setNews("Breaking: New Java version released!");
        
        System.out.println("\nRemoving BBC...");
        agency.removeObserver(bbc);
        
        agency.setNews("Update: Performance improvements confirmed!");
        
        // Output:
        // CNN received news: Breaking: New Java version released!
        // BBC received news: Breaking: New Java version released!
        // Yahoo News portal updated with: Breaking: New Java version released!
        // 
        // Removing BBC...
        // CNN received news: Update: Performance improvements confirmed!
        // Yahoo News portal updated with: Update: Performance improvements confirmed!
    }
}
```

### 2. Strategy Pattern

**Purpose**: Lets you define a family of algorithms, put each of them into a separate class, and make their objects interchangeable.

**When to Use**:
- When you have multiple ways of performing a task
- When you want to avoid conditional statements for choosing behavior
- When you want to change algorithms at runtime

```java
// Strategy interface
interface PaymentStrategy {
    void pay(double amount);
}

// Concrete strategies
class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String holderName;
    
    public CreditCardPayment(String cardNumber, String holderName) {
        this.cardNumber = cardNumber;
        this.holderName = holderName;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Credit Card ending in " + 
                          cardNumber.substring(cardNumber.length() - 4));
    }
}

class PayPalPayment implements PaymentStrategy {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using PayPal account: " + email);
    }
}

class BankTransferPayment implements PaymentStrategy {
    private String accountNumber;
    
    public BankTransferPayment(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " via Bank Transfer from account: " + accountNumber);
    }
}

// Context
class ShoppingCart {
    private PaymentStrategy paymentStrategy;
    
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }
    
    public void checkout(double amount) {
        if (paymentStrategy == null) {
            System.out.println("Please select a payment method!");
            return;
        }
        paymentStrategy.pay(amount);
    }
}

// Usage
public class StrategyDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        
        // Pay with credit card
        cart.setPaymentStrategy(new CreditCardPayment("1234567890123456", "John Doe"));
        cart.checkout(99.99);
        
        // Pay with PayPal
        cart.setPaymentStrategy(new PayPalPayment("john.doe@email.com"));
        cart.checkout(149.50);
        
        // Pay with bank transfer
        cart.setPaymentStrategy(new BankTransferPayment("ACC123456789"));
        cart.checkout(75.25);
        
        // Output:
        // Paid $99.99 using Credit Card ending in 3456
        // Paid $149.5 using PayPal account: john.doe@email.com
        // Paid $75.25 via Bank Transfer from account: ACC123456789
    }
}
```

### 3. Command Pattern

**Purpose**: Turns a request into a stand-alone object that contains all information about the request. This transformation lets you pass requests as method arguments, delay or queue a request's execution, and support undoable operations.

**When to Use**:
- When you want to queue operations, schedule their execution, or execute them remotely
- When you want to implement reversible operations (undo/redo)

```java
import java.util.*;

// Command interface
interface Command {
    void execute();
    void undo();
}

// Receiver
class Light {
    private boolean isOn = false;
    
    public void turnOn() {
        isOn = true;
        System.out.println("Light is ON");
    }
    
    public void turnOff() {
        isOn = false;
        System.out.println("Light is OFF");
    }
    
    public boolean isOn() {
        return isOn;
    }
}

// Concrete commands
class LightOnCommand implements Command {
    private Light light;
    
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.turnOn();
    }
    
    @Override
    public void undo() {
        light.turnOff();
    }
}

class LightOffCommand implements Command {
    private Light light;
    
    public LightOffCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.turnOff();
    }
    
    @Override
    public void undo() {
        light.turnOn();
    }
}

// Invoker
class RemoteControl {
    private Command[] onCommands;
    private Command[] offCommands;
    private Stack<Command> undoStack;
    
    public RemoteControl(int slots) {
        onCommands = new Command[slots];
        offCommands = new Command[slots];
        undoStack = new Stack<>();
        
        // Initialize with null commands
        Command noCommand = new Command() {
            @Override
            public void execute() {}
            @Override
            public void undo() {}
        };
        
        for (int i = 0; i < slots; i++) {
            onCommands[i] = noCommand;
            offCommands[i] = noCommand;
        }
    }
    
    public void setCommand(int slot, Command onCommand, Command offCommand) {
        onCommands[slot] = onCommand;
        offCommands[slot] = offCommand;
    }
    
    public void onButtonPressed(int slot) {
        onCommands[slot].execute();
        undoStack.push(onCommands[slot]);
    }
    
    public void offButtonPressed(int slot) {
        offCommands[slot].execute();
        undoStack.push(offCommands[slot]);
    }
    
    public void undoButtonPressed() {
        if (!undoStack.isEmpty()) {
            Command lastCommand = undoStack.pop();
            lastCommand.undo();
        }
    }
}

// Usage
public class CommandDemo {
    public static void main(String[] args) {
        // Create receiver
        Light livingRoomLight = new Light();
        
        // Create commands
        LightOnCommand livingRoomLightOn = new LightOnCommand(livingRoomLight);
        LightOffCommand livingRoomLightOff = new LightOffCommand(livingRoomLight);
        
        // Create invoker
        RemoteControl remote = new RemoteControl(7);
        
        // Set commands
        remote.setCommand(0, livingRoomLightOn, livingRoomLightOff);
        
        // Test the remote
        System.out.println("--- Remote Control Test ---");
        remote.onButtonPressed(0);   // Light is ON
        remote.offButtonPressed(0);  // Light is OFF
        remote.onButtonPressed(0);   // Light is ON
        
        System.out.println("\n--- Undo Operations ---");
        remote.undoButtonPressed();  // Light is OFF (undo last ON)
        remote.undoButtonPressed();  // Light is ON (undo last OFF)
        remote.undoButtonPressed();  // Light is OFF (undo last ON)
    }
}
```

### 4. Template Method Pattern

**Purpose**: Defines the skeleton of an algorithm in the superclass but lets subclasses override specific steps of the algorithm without changing its structure.

**When to Use**:
- When you have multiple classes with similar algorithms but different implementations of specific steps
- When you want to control the order of algorithm steps

```java
// Abstract class defining template method
abstract class DataProcessor {
    
    // Template method - defines the algorithm structure
    public final void process() {
        readData();
        processData();
        saveData();
        
        // Hook method (optional step)
        if (shouldNotify()) {
            sendNotification();
        }
    }
    
    // Abstract methods to be implemented by subclasses
    protected abstract void readData();
    protected abstract void processData();
    protected abstract void saveData();
    
    // Hook method (optional override)
    protected boolean shouldNotify() {
        return true;
    }
    
    protected void sendNotification() {
        System.out.println("Sending notification: Data processing completed");
    }
}

// Concrete implementation for CSV processing
class CSVDataProcessor extends DataProcessor {
    
    @Override
    protected void readData() {
        System.out.println("Reading data from CSV file");
    }
    
    @Override
    protected void processData() {
        System.out.println("Processing CSV data: cleaning and validation");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving processed CSV data to database");
    }
}

// Concrete implementation for JSON processing
class JSONDataProcessor extends DataProcessor {
    
    @Override
    protected void readData() {
        System.out.println("Reading data from JSON file");
    }
    
    @Override
    protected void processData() {
        System.out.println("Processing JSON data: parsing and transformation");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving processed JSON data to cloud storage");
    }
    
    @Override
    protected boolean shouldNotify() {
        return false; // JSON processing doesn't need notifications
    }
}

// Concrete implementation for XML processing
class XMLDataProcessor extends DataProcessor {
    
    @Override
    protected void readData() {
        System.out.println("Reading data from XML file");
    }
    
    @Override
    protected void processData() {
        System.out.println("Processing XML data: schema validation and parsing");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving processed XML data to file system");
    }
    
    @Override
    protected void sendNotification() {
        System.out.println("Sending email notification: XML processing completed");
    }
}

// Usage
public class TemplateMethodDemo {
    public static void main(String[] args) {
        System.out.println("=== CSV Processing ===");
        DataProcessor csvProcessor = new CSVDataProcessor();
        csvProcessor.process();
        
        System.out.println("\n=== JSON Processing ===");
        DataProcessor jsonProcessor = new JSONDataProcessor();
        jsonProcessor.process();
        
        System.out.println("\n=== XML Processing ===");
        DataProcessor xmlProcessor = new XMLDataProcessor();
        xmlProcessor.process();
        
        // Output:
        // === CSV Processing ===
        // Reading data from CSV file
        // Processing CSV data: cleaning and validation
        // Saving processed CSV data to database
        // Sending notification: Data processing completed
        // 
        // === JSON Processing ===
        // Reading data from JSON file
        // Processing JSON data: parsing and transformation
        // Saving processed JSON data to cloud storage
        // 
        // === XML Processing ===
        // Reading data from XML file
        // Processing XML data: schema validation and parsing
        // Saving processed XML data to file system
        // Sending email notification: XML processing completed
    }
}
```

### 5. State Pattern

**Purpose**: Lets an object alter its behavior when its internal state changes. It appears as if the object changed its class.

**When to Use**:
- When an object's behavior depends on its state and must change at runtime
- When you have large conditional statements that depend on the object's state

```java
// State interface
interface VendingMachineState {
    void insertCoin();
    void selectProduct();
    void dispenseProduct();
    void refund();
}

// Context
class VendingMachine {
    private VendingMachineState idleState;
    private VendingMachineState hasMoneyState;
    private VendingMachineState dispensingState;
    private VendingMachineState outOfStockState;
    
    private VendingMachineState currentState;
    private int productCount = 5;
    
    public VendingMachine() {
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        dispensingState = new DispensingState(this);
        outOfStockState = new OutOfStockState(this);
        
        currentState = productCount > 0 ? idleState : outOfStockState;
    }
    
    public void insertCoin() { currentState.insertCoin(); }
    public void selectProduct() { currentState.selectProduct(); }
    public void dispenseProduct() { currentState.dispenseProduct(); }
    public void refund() { currentState.refund(); }
    
    public void setState(VendingMachineState state) { this.currentState = state; }
    
    public VendingMachineState getIdleState() { return idleState; }
    public VendingMachineState getHasMoneyState() { return hasMoneyState; }
    public VendingMachineState getDispensingState() { return dispensingState; }
    public VendingMachineState getOutOfStockState() { return outOfStockState; }
    
    public int getProductCount() { return productCount; }
    public void reduceProductCount() { productCount--; }
}

// Concrete states
class IdleState implements VendingMachineState {
    private VendingMachine machine;
    
    public IdleState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Coin inserted. Please select a product.");
        machine.setState(machine.getHasMoneyState());
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Please insert a coin first.");
    }
    
    @Override
    public void dispenseProduct() {
        System.out.println("Please insert a coin and select a product first.");
    }
    
    @Override
    public void refund() {
        System.out.println("No coin to refund.");
    }
}

class HasMoneyState implements VendingMachineState {
    private VendingMachine machine;
    
    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Coin already inserted. Please select a product.");
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Product selected. Dispensing...");
        machine.setState(machine.getDispensingState());
        machine.dispenseProduct();
    }
    
    @Override
    public void dispenseProduct() {
        System.out.println("Please select a product first.");
    }
    
    @Override
    public void refund() {
        System.out.println("Coin refunded.");
        machine.setState(machine.getIdleState());
    }
}

class DispensingState implements VendingMachineState {
    private VendingMachine machine;
    
    public DispensingState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Please wait, already dispensing product.");
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Please wait, already dispensing product.");
    }
    
    @Override
    public void dispenseProduct() {
        System.out.println("Product dispensed. Thank you!");
        machine.reduceProductCount();
        
        if (machine.getProductCount() <= 0) {
            machine.setState(machine.getOutOfStockState());
        } else {
            machine.setState(machine.getIdleState());
        }
    }
    
    @Override
    public void refund() {
        System.out.println("Cannot refund while dispensing.");
    }
}

class OutOfStockState implements VendingMachineState {
    private VendingMachine machine;
    
    public OutOfStockState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Machine is out of stock. Coin refunded.");
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Machine is out of stock.");
    }
    
    @Override
    public void dispenseProduct() {
        System.out.println("Machine is out of stock.");
    }
    
    @Override
    public void refund() {
        System.out.println("No coin to refund.");
    }
}

// Usage
public class StateDemo {
    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine();
        
        System.out.println("=== Normal Operation ===");
        machine.insertCoin();        // Coin inserted. Please select a product.
        machine.selectProduct();     // Product selected. Dispensing...
                                    // Product dispensed. Thank you!
        
        System.out.println("\n=== Try without coin ===");
        machine.selectProduct();     // Please insert a coin first.
        
        System.out.println("\n=== Insert coin and refund ===");
        machine.insertCoin();        // Coin inserted. Please select a product.
        machine.refund();           // Coin refunded.
        
        System.out.println("\n=== Drain stock ===");
        for (int i = 0; i < 4; i++) {
            machine.insertCoin();
            machine.selectProduct();
        }
        
        System.out.println("\n=== Try when out of stock ===");
        machine.insertCoin();       // Machine is out of stock. Coin refunded.
    }
}
```

---

## Summary

Design patterns provide proven solutions to common programming problems. Here's a quick reference for when to use each pattern:

### Creational Patterns
- **Singleton**: When you need exactly one instance (database connections, loggers)
- **Factory Method**: When you need to create objects without specifying their exact classes
- **Abstract Factory**: When you need to create families of related objects
- **Builder**: When constructing complex objects with many optional parameters
- **Prototype**: When object creation is expensive and you need similar objects

### Structural Patterns
- **Adapter**: When you need to use an existing class with an incompatible interface
- **Decorator**: When you want to add behavior to objects without altering their structure
- **Facade**: When you want to provide a simple interface to a complex subsystem
- **Composite**: When you need to work with tree-like object structures
- **Proxy**: When you need to control access to another object

### Behavioral Patterns
- **Observer**: When you need to notify multiple objects about state changes
- **Strategy**: When you have multiple ways to perform a task and want to choose at runtime
- **Command**: When you want to parameterize objects with operations or support undo
- **Template Method**: When you have algorithms with similar structure but different implementations
- **State**: When an object's behavior depends on its state and must change at runtime

Remember that design patterns are tools to solve specific problems. Don't force patterns where they're not needed - sometimes a simple solution is better than a complex pattern.
