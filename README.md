# 🏦 Java 21 Multithreaded Banking Simulator

[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A professional-grade demonstration of modern Java 21 concurrency, critical sections, and synchronized data access using virtual threads and explicit locking mechanisms.

## 🚀 Project Overview

This project is a sophisticated simulation of a banking system designed to demonstrate advanced concurrency principles in Java 21. It showcases the proper implementation of thread safety, critical sections, and mutual exclusion using modern Java APIs.

The simulator creates a multithreaded environment where thousands of banking operations (deposits, withdrawals, and transfers) are executed concurrently using Java 21's lightweight virtual threads. The system maintains data integrity and prevents race conditions through carefully designed synchronization mechanisms.

> 💡 **Educational Purpose**: This project was specifically designed as a learning resource for advanced Java developers and professionals preparing for Java 21 certification.

## 📚 Learning Goals

By exploring and working with this repository, you will gain deep understanding of:

- **Virtual Threads**: How to leverage Java 21's virtual thread API for scalable concurrency
- **Critical Sections**: Identifying and protecting shared mutable state
- **Lock API vs Synchronized**: Why `ReentrantLock` offers more flexibility than the `synchronized` keyword
- **Compound Operations**: Why thread-safe collections alone are not enough
- **Deadlock Prevention**: Techniques to prevent deadlocks in concurrent systems
- **Modern Testing**: How to validate correctness of concurrent code using JUnit 5

## ⚙️ Technologies Used

- **Java 21**: For virtual threads and modern language features
- **Virtual Threads**: Java's lightweight thread implementation
- **ReentrantLock**: Explicit lock management for critical sections
- **ConcurrentHashMap**: Thread-safe collection for managing accounts
- **JUnit 5**: For comprehensive testing of concurrent behavior
- **Maven**: For project management and dependency control

## 🧠 How It Works

The system consists of three core components:

1. **`BankAccount`**: Represents individual accounts and implements thread-safe operations using `ReentrantLock` for protecting critical sections.

```java
public boolean withdraw(double amount) {
    lock.lock();
    try {
        // Critical section: check and update balance atomically
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    } finally {
        lock.unlock();
    }
}
```

2. **`BankSystem`**: Manages the collection of accounts and coordinates transaction execution using `ConcurrentHashMap`.

3. **`TransactionTask`**: Represents banking operations that are executed by virtual threads.

The concurrency model uses explicit locking with `ReentrantLock` instead of the `synchronized` keyword, providing better control over lock acquisition and release, interruptibility, and fairness policies.

Virtual threads are spawned using the modern `Thread.startVirtualThread(...)` API rather than traditional thread pools or executors, demonstrating Java 21's capabilities for efficient concurrency.

## 🔬 How to Run the Tests

The project includes comprehensive JUnit 5 tests that validate the system's behavior under high concurrency:

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BankSystemConcurrencyTest
```

The test suite includes:
- Tests for concurrent deposits
- Tests for concurrent withdrawals
- Tests for concurrent transfers between accounts
- Tests for mixed workloads
- A demonstration of race conditions without proper synchronization

## 🛠️ Setup Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Building the Project

1. Clone the repository:
```bash
git clone https://github.com/yourusername/java21-banking-simulator.git
cd java21-banking-simulator
```

2. Build with Maven:
```bash
mvn clean install
```

3. Run the tests:
```bash
mvn test
```

## 👨‍🏫 Educational Value

This project is ideal for advanced Java learners because:

- It demonstrates **real-world concurrency challenges** that go beyond basic examples
- It uses **modern Java 21 features** that are becoming industry standard
- It shows the **advantages of explicit locking** over traditional synchronization
- It illustrates **deadlock prevention techniques** using lock ordering
- The test suite shows how to **validate concurrent code** for correctness
- The code is thoroughly documented with **educational comments**

The implementation is carefully designed to highlight the "why" behind each concurrency decision, making it an excellent resource for those preparing for Java certification or seeking to deepen their understanding of concurrent programming.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Created with ❤️ for Java developers who want to master modern concurrency.