# MiniBank — Object-Oriented Banking Application

**Course:** Object Oriented Programming (CEUE203)  
**Academic Year:** 2026 – 2027 | Semester – 3  
**Practical 14:** Final Capstone Project Work — MiniBank Assembly, Testing & Packaging  
**Milestone:** Milestone 3 Checkpoint & Final Viva

---

## 1. Project Overview

**MiniBank** is a console banking system built incrementally across 14 practicals throughout the semester. In Practical 14, the modules developed in prior labs are integrated into a single application featuring:

- **Layered Architecture:** Clear separation between Presentation (`MiniBank`), Business Logic (`BankService`), Persistence/Repository (`Repository<T>`, `AccountStore`), and Domain Model (`Account`, `Customer`).
- **Object Modeling & Polymorphism:** Abstract `Account` base class with `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount` specializations.
- **Robust Exception Handling:** Custom exception hierarchy rooted at `BankException`, including `InsufficientFundsException` and `InvalidAmountException`.
- **Thread Safety & Concurrency:** Synchronized account operations, deadlock-free transfers via strict lock ordering, and a worker pool (`TransactionProcessor`).
- **Persistence & Logging:** Object serialization (`StatePersister`) saving state across sessions, append-only transaction logs, and Java NIO reporting.
- **Reflection & Annotations:** Runtime annotations (`@Id`, `@Audit`) inspected dynamically by `Repository` and `AuditLogger`.
- **Unit Testing:** Standalone test suite using custom `@Test` annotations and assertions.

---

## 2. System Architecture

```
+-----------------------------------------------------------------+
|                    Presentation Layer (UI)                      |
|          service.MiniBank (Console Menu, Scanner Input)         |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                     Business Service Layer                      |
|   service.BankService (Coordinates Transactions, Batch, Audit)  |
+-----------------------------------------------------------------+
         |                           |                      |
         v                           v                      v
+------------------+     +-----------------------+   +-------------------+
| Persistence Layer|     |  Concurrency Engine   |   |  Utility Layer    |
| Repository<T>    |     | TransactionProcessor  |   | Validator         |
| AccountStore     |     | TransferManager       |   | AnnotationValidator|
| StatePersister   |     | TransactionTask       |   | StatementFormatter|
+------------------+     +-----------------------+   | AuditLogger       |
         |                                           +-------------------+
         v
+-----------------------------------------------------------------+
|                   Domain Model & Exceptions                     |
|  model.Account (SavingsAccount, CurrentAccount, FixedDeposit)   |
|  exception.BankException, InsufficientFundsException, etc.      |
+-----------------------------------------------------------------+
```

### Layer Responsibilities
- **Model (`model`):** Core business entities, account types, customer details, and marker/functional interfaces (`Transactable`, `InterestBearing`, `WithdrawRule`, `Premium`).
- **Service (`service`):** Contains `BankService` providing public methods (`openAccount`, `deposit`, `withdraw`, `transfer`, `statement`, `listAccounts`, `processBatch`).
- **Persistence (`service.Repository`, `service.StatePersister`):** In-memory generic repository with reflection-based primary key detection, along with disk serialization.
- **Utilities (`util`):** Input validation via regex, statement formatting, and runtime audit interception.
- **Exceptions (`exception`):** Domain exceptions reporting exact financial shortfalls and validation errors.

---

## 3. Practical Progression Map

| Practical | Topic & Core Focus | MiniBank Component |
|:---|:---|:---|
| **Practical 1** | Menu shell, Enums, Records | `MenuOption` enum, `BankInfo` record, interactive menu |
| **Practical 2** | Encapsulation & Basic Methods | `Account`, `Customer`, private balance with `deposit()` / `withdraw()` |
| **Practical 3** | Object Methods, Equality & Cloning | `toString()`, `equals()`, `hashCode()`, `Comparable<Account>`, `Address` |
| **Practical 4** | Regex Validation & String Formatting | `Validator` (mobile, PAN, IFSC), `StatementFormatter` |
| **Practical 5** | Inheritance & Polymorphism | Abstract `Account`, `SavingsAccount`, `CurrentAccount`, `FixedDepositAccount` |
| **Practical 6** | Interfaces & Lambdas | `Transactable`, `InterestBearing`, `WithdrawRule`, `Premium` |
| **Practical 7** | Custom Annotations & Reflection | `@Id`, `@Positive`, `@MaxLength`, `AnnotationValidator` |
| **Practical 8** | Custom Exception Hierarchy | `BankException`, `InsufficientFundsException`, `InvalidAmountException` |
| **Practical 9** | Multithreading & Race Conditions | `synchronized` account methods, `AccountWorker` |
| **Practical 10** | Thread Pools & Deadlock Prevention | `TransactionProcessor` (4 worker threads), `TransferManager` lock ordering |
| **Practical 11** | Serialization & Java NIO | `StatePersister` (`data/accounts.ser`), `TransactionLog`, `ReportGenerator` |
| **Practical 12** | Collections Framework | `AccountStore` (`HashMap`, `ArrayList`, `ConcurrentHashMap`), sorting |
| **Practical 13** | Generic Repository & Bounded Types | `Repository<T>`, `@SafeVarargs saveAll()`, `average(N[])` |
| **Practical 14** | **Capstone Integration & Packaging** | **`BankService`, wired menu, JUnit suite, `@Audit`, Runnable JAR** |

---

## 4. How to Build and Run

### Prerequisites
- JDK 17 or higher (tested with modern OpenJDK).

### 1. Compile Sources
From the project root:
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

### 2. Package into Runnable JAR
```powershell
jar cfm minibank.jar manifest.mf -C out .
```

### 3. Run Options

#### A. Interactive Console Menu
```powershell
java -jar minibank.jar
```
*(Alternative: `java -cp out service.MiniBank`)*

#### B. Automated Unit Test Suite
```powershell
java -jar minibank.jar --test
```
*(Alternative: `java -cp out test.MiniBankTest`)*

#### C. Comprehensive Demonstration Mode
```powershell
java -jar minibank.jar --demo
```

---

## 5. Console Menu Options

```
-----------------------------------------------------------------
                       MINIBANK MAIN MENU                        
-----------------------------------------------------------------
  1. Open Account
  2. Deposit Funds
  3. Withdraw Funds
  4. Transfer Funds (Deadlock-Free)
  5. View Account Statement
  6. List All Accounts
  7. Safe Batch Processing (TransactionProcessor Thread Pool)
  8. End-of-Day Summary Report (Java NIO)
  9. Exit & Save State
-----------------------------------------------------------------
```

1. **Open Account:** Prompts for type (Savings, Current, Fixed Deposit), holder name, and opening balance.
2. **Deposit Funds:** Credits an existing account and logs the operation.
3. **Withdraw Funds:** Enforces minimum balance or overdraft limits, raising `InsufficientFundsException` with shortfall info when invalid.
4. **Transfer Funds:** Transfers money using strict lock ordering (smaller account number locked first) to prevent deadlock.
5. **View Account Statement:** Displays account details, balance, and calculated annual interest.
6. **List All Accounts:** Prints a formatted summary table of all stored accounts.
7. **Safe Batch Processing:** Submits concurrent tasks across 4 worker threads in the `TransactionProcessor` pool.
8. **End-of-Day Summary Report:** Inspects `logs/` via Java NIO and writes an aggregated reconciliation summary to `reports/end_of_day_report.txt`.
9. **Exit & Save State:** Serializes in-memory account data to `data/accounts.ser` on termination.

---

## 6. Unit Testing Suite (`test.MiniBankTest`)

The test suite validates core operations and safety invariants:

1. **`testDepositIncreasesBalance`**: Confirms deposit correctly increments balance.
2. **`testWithdrawalOverLimitThrowsException`**: Confirms `InsufficientFundsException` is raised when limits are exceeded.
3. **`testConcurrentDepositsKeepBalanceCorrect`**: Runs 10 concurrent threads each performing 100 deposits (1,000 total) to ensure thread synchronization prevents lost updates.
4. **`testTransferBetweenAccounts`**: Validates atomicity and correct debits/credits during inter-account transfers.
5. **`testStatePersistenceSaveAndLoad`**: Validates that serialization safely preserves account state to and from disk.

Execution command:
```powershell
java -cp out test.MiniBankTest
```

Expected output:
```
=================================================
        MINIBANK JUNIT TEST SUITE RUNNER         
=================================================
  Running: testDepositIncreasesBalance                ... [PASSED]
  Running: testWithdrawalOverLimitThrowsException     ... [PASSED]
  Running: testConcurrentDepositsKeepBalanceCorrect   ... [PASSED]
  Running: testTransferBetweenAccounts                ... [PASSED]
  Running: testStatePersistenceSaveAndLoad            ... [PASSED]
-------------------------------------------------
Test Results: Total: 5 | Passed: 5 | Failed: 0
=================================================
SUCCESS: All MiniBank unit tests passed successfully!
```

---

## 7. Viva Q&A Reference

### Q1: What is a layered architecture, and why is it useful in MiniBank?
**Answer:**  
A layered architecture divides the application into distinct tiers:
- **Presentation Layer (`MiniBank`):** Handles console UI, Scanner inputs, and menus.
- **Service Layer (`BankService`):** Coordinates business transactions, validations, and background tasks.
- **Persistence Layer (`Repository<T>`, `StatePersister`):** Stores and persists domain objects.
- **Model Layer (`Account`, `Customer`):** Defines data structure and core invariants.

**Benefits:**
- **Separation of Concerns:** Business logic is isolated from UI code. The UI could be swapped for a GUI or web frontend without rewriting banking logic.
- **Testability:** Service methods can be unit-tested without navigating a console menu.
- **Reusability:** The generic `Repository<T>` can store any entity (`Account`, `Customer`, etc.) without code duplication.

---

### Q2: How do you verify in JUnit that a method throws an expected exception?
**Answer:**  
Using `assertThrows`:
```java
assertThrows(InsufficientFundsException.class, () -> {
    service.withdraw(accountNumber, excessiveAmount);
});
```
`assertThrows` takes the expected exception class and an executable lambda. If the lambda throws the expected exception, the assertion passes and returns the exception instance. If no exception or an unexpected type is thrown, an `AssertionError` is raised.

---

### Q3: What advantages does packaging the application into a runnable JAR offer?
**Answer:**  
1. **Portability:** Bundles compiled bytecode, resources, and manifest metadata into a single distributable archive.
2. **Simplified Execution:** With `Main-Class: service.MiniBank` specified in `manifest.mf`, users execute the program with `java -jar minibank.jar` without configuring classpaths manually.
3. **Integrity:** Encapsulates the complete directory structure into an uncorrupted package ready for distribution.
