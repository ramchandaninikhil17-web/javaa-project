# MiniBank — Object-Oriented Banking Application (Milestone 3 Capstone)

**Course:** Object Oriented Programming (CEUE203)  
**Academic Year:** 2026 – 2027 | Semester – 3  
**Practical 14 (Part B):** Final Capstone Project Work — MiniBank Final Assembly, Testing & Packaging  
**Milestone:** Milestone 3 Checkpoint & Final Viva

---

## 1. Project Overview

**MiniBank** is a console-based banking application built incrementally across the semester. In this final capstone lab (Practical 14), all modules developed throughout Practicals 1 to 13 are assembled into a robust, layered, thread-safe, and persistent banking system.

The application features:
- **Layered Architecture:** Clear separation between Presentation (`MiniBank`), Service (`BankService`), Persistence/Repository (`Repository<T>`, `AccountStore`), and Domain Model (`Account`, `Customer`).
- **Domain Modeling & Polymorphism:** Abstract `Account` base class with `SavingsAccount`, `CurrentAccount`, and `FixedDepositAccount`.
- **Fault Tolerance:** Custom exception hierarchy (`BankException`, `InsufficientFundsException`, `InvalidAmountException`, `AccountNotFoundException`).
- **Thread Safety & Concurrency:** Synchronized account methods, lock ordering to prevent transfer deadlocks, and managed worker thread pools (`TransactionProcessor`).
- **Persistence:** Binary object serialization (`StatePersister`) ensuring state survives application restarts.
- **Audit & NIO Reporting:** Append-only transaction logging and end-of-day reconciliation summaries powered by Java NIO (`DirectoryStream`, `BufferedReader`).
- **Reflection & Metadata:** Custom runtime annotations (`@Id`, `@Positive`, `@MaxLength`, `@Audit`) with automated validators and reflective audit logging.
- **Unit Testing:** Comprehensive JUnit test suite (`@Test`) validating deposits, limit constraints, and concurrent operations.

---

## 2. Layered Architecture

```
+-----------------------------------------------------------------+
|               Presentation Layer (UI / Menu)                    |
|   service.MiniBank (Practical 1 Console Menu, Scanner IO)      |
+-----------------------------------------------------------------+
                               |
                               v
+-----------------------------------------------------------------+
|                   Business Service Layer                        |
|   service.BankService (Coordinates Operations, Audit, Batch)   |
+-----------------------------------------------------------------+
         |                           |                      |
         v                           v                      v
+------------------+     +-----------------------+   +-------------------+
| Data Access Layer|     | Concurrency Engine    |   | Utilities & Meta  |
| Repository<Account>|   | TransactionProcessor  |   | Validator         |
| AccountStore     |     | TransferManager (Safe)|   | AnnotationValidator|
| StatePersister   |     | TransactionTask       |   | StatementFormatter|
+------------------+     +-----------------------+   | AuditLogger       |
         |                                           +-------------------+
         v
+-----------------------------------------------------------------+
|                     Domain Model & Exceptions                   |
|   model.Account (SavingsAccount, CurrentAccount, FixedDeposit)  |
|   exception.BankException (InsufficientFunds, InvalidAmount...) |
+-----------------------------------------------------------------+
```

### Module Responsibilities:
1. **Model Layer (`model`):** Encapsulates core banking data, accounts, customers, interfaces (`Transactable`, `InterestBearing`, `Premium`), and annotations (`@Id`, `@Positive`, `@MaxLength`, `@Audit`).
2. **Service Layer (`service`):** Contains `BankService` which exposes clean public APIs (`openAccount`, `deposit`, `withdraw`, `transfer`, `statement`, `listAccounts`, `processBatch`).
3. **Persistence Layer (`service.Repository`, `service.StatePersister`):** Stores entities in memory with O(1) reflection-based primary key extraction and serializes account state to disk on exit.
4. **Utility Layer (`util`):** Provides regular expression validation (`Validator`), reflection validators (`AnnotationValidator`), statement formatting (`StatementFormatter`), and reflective method logging (`AuditLogger`).
5. **Exception Hierarchy (`exception`):** Rooted at checked `BankException`, enabling structured, recoverable error handling with shortfall details.

---

## 3. Practical Progression Map

| Practical | Core Skill / Requirement | Implementation in MiniBank |
|:---|:---|:---|
| **Practical 1** | Menu shell, Enums, Records | `MenuOption` enum, `BankInfo` record, interactive navigation |
| **Practical 2** | Encapsulation & basic methods | `Account`, `Customer`, private balance with `deposit()` and `withdraw()` |
| **Practical 3** | Object methods, equality & cloning | `toString()`, `equals()`, `hashCode()`, `Comparable<Account>`, nested `Address` |
| **Practical 4** | Regex validation & String parsing | `Validator` (mobile, email, PAN, IFSC), `CommandParser`, `StatementFormatter` |
| **Practical 5** | Inheritance & polymorphism | Abstract `Account`, `SavingsAccount`, `CurrentAccount`, `FixedDepositAccount` |
| **Practical 6** | Interfaces & Lambdas | `Transactable`, `InterestBearing`, `WithdrawRule`, `Premium` marker |
| **Practical 7** | Custom annotations & Reflection | `@Id`, `@Positive`, `@MaxLength`, `AnnotationValidator` |
| **Practical 8** | Custom exception hierarchy | `BankException`, `InsufficientFundsException`, `InvalidAmountException`, `AccountNotFoundException` |
| **Practical 9** | Multithreading & Synchronized | Race condition demo, `synchronized` deposit/withdraw, `AccountWorker` |
| **Practical 10** | Thread pool & Deadlock resolution | `TransactionProcessor` (4 worker threads), `TransferManager` lock ordering |
| **Practical 11** | Object serialization & Java NIO | `StatePersister` (accounts.ser), `TransactionLog`, `ReportGenerator` |
| **Practical 12** | Collections framework | `AccountStore` (HashMap, ArrayList), Comparator sorting, `ConcurrentHashMap` |
| **Practical 13** | Generic Repository & Bounded Types | `Repository<T>` with reflection `@Id`, `@SafeVarargs saveAll()`, `average(N[])` |
| **Practical 14** | **Milestone 3 Capstone Assembly** | **`BankService`, Menu wiring, Safe batch processing, JUnit tests, `@Audit`, Runnable JAR** |

---

## 4. Compilation and Execution Guide

### Prerequisites
- Java Development Kit (JDK 17 or higher, e.g., JDK 26) installed.

### Step 1: Compile All Sources
From the project root directory, compile all packages into the `out` directory:
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

### Step 2: Build the Runnable JAR
Package the compiled classes and manifest into `minibank.jar`:
```powershell
jar cfm minibank.jar manifest.mf -C out .
```

### Step 3: Run the Application
You can run MiniBank directly via the JAR or through class files:

#### A. Interactive Banking Menu (Standard User Mode)
```powershell
java -jar minibank.jar
```
*(Or via class files: `java -cp out service.MiniBank`)*

#### B. Run Automated JUnit Test Suite
```powershell
java -jar minibank.jar --test
```
*(Or directly via test runner: `java -cp out test.MiniBankTest`)*

#### C. Run Full Capstone Demonstration (Viva Showcase)
```powershell
java -jar minibank.jar --demo
```

---

## 5. Main Menu Operations Guide

When launching the interactive console menu, you are presented with 9 options:

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

1. **Open Account:** Prompts for account type (Savings, Current, or Fixed Deposit), holder name, and initial deposit amount. Creates and registers the account in the repository.
2. **Deposit Funds:** Prompts for account number and positive amount; increments balance atomically and logs transaction.
3. **Withdraw Funds:** Prompts for account number and amount; enforces account minimum balance or overdraft limit, raising `InsufficientFundsException` with exact shortfall if funds are insufficient.
4. **Transfer Funds:** Moves funds between two accounts using consistent lock ordering (locking the smaller account number first) to prevent deadlock.
5. **View Account Statement:** Formats and prints a multi-line bank statement showing account number, owner, status, current balance, and annual interest.
6. **List All Accounts:** Displays an overview table of all registered accounts.
7. **Safe Batch Processing:** Dispatches a batch of deposit/withdraw tasks across 4 worker threads in the managed `TransactionProcessor` thread pool.
8. **End-of-Day Summary Report:** Walks `logs/` using Java NIO `DirectoryStream`, aggregates all deposits, withdrawals, and net flow, and writes the summary to `reports/end_of_day_report.txt`.
9. **Exit & Save State:** Serializes all account data into `data/accounts.ser` using `StatePersister` and gracefully terminates the session.

---

## 6. Unit Testing Suite (`test.MiniBankTest`)

The test suite validates core business logic and concurrency requirements using `@Test` annotations and assertions:

1. **`testDepositIncreasesBalance`**: Asserts that depositing Rs.2,500 into an account with Rs.5,000 raises the balance to Rs.7,500.
2. **`testWithdrawalOverLimitThrowsException`**: Asserts that `InsufficientFundsException` is thrown when a withdrawal exceeds available balance / overdraft limits.
3. **`testConcurrentDepositsKeepBalanceCorrect`**: Spawns 10 concurrent threads, each performing 100 deposits of Rs.10 (1,000 total operations) on a single shared account. Asserts that final balance is exactly Rs.10,000 without lost updates.
4. **`testTransferBetweenAccounts`**: Asserts that transferring funds correctly debits the sender and credits the receiver.
5. **`testStatePersistenceSaveAndLoad`**: Asserts that saving accounts to a serialized file and reloading them preserves all properties and account balances intact.

To run the test suite:
```powershell
java -cp out test.MiniBankTest
```
**Output:**
```
=================================================
        MINIBANK JUNIT TEST SUITE RUNNER         
=================================================
  Running: testWithdrawalOverLimitThrowsException     ... [PASSED]
  Running: testDepositIncreasesBalance                ... [PASSED]
  Running: testConcurrentDepositsKeepBalanceCorrect   ... [PASSED]
  Running: testTransferBetweenAccounts                ... [PASSED]
  Running: testStatePersistenceSaveAndLoad            ... [PASSED]
-------------------------------------------------
Test Results: Total: 5 | Passed: 5 | Failed: 0
=================================================
SUCCESS: All MiniBank unit tests passed successfully!
```

---

## 7. Viva Q&A / Conceptual Questions

### Q1: What is a layered architecture (model, service, user interface), and why is it useful?
**Answer:**  
A layered architecture divides software into distinct horizontal tiers, each with a single responsibility:
- **Presentation Layer (UI/Menu):** Handles user interactions, prompts, and displays output.
- **Service Layer (`BankService`):** Orchestrates business logic, validations, concurrency, and transactions.
- **Data Access Layer (`Repository`, `StatePersister`):** Manages data retrieval and persistence without knowing how business logic works.
- **Model Layer (`Account`, `Customer`):** Defines the domain entities and core state rules.

**Why it is useful:**
1. **Separation of Concerns:** Business logic is decoupled from user input, allowing the UI to change (e.g., from console to GUI or web API) without modifying core banking logic.
2. **Maintainability & Testability:** Each layer can be tested independently (e.g., testing `BankService` via JUnit without requiring a console menu).
3. **Reusability:** The generic `Repository<T>` and validation logic can be reused across different domain entities.

---

### Q2: How do you assert in JUnit that a method throws an expected exception?
**Answer:**  
In modern JUnit (JUnit 5), you use `Assertions.assertThrows`:
```java
assertThrows(InsufficientFundsException.class, () -> {
    service.withdraw(accountNumber, excessiveAmount);
});
```
`assertThrows` accepts the expected exception class and an executable lambda. If the lambda throws the expected exception (or a subclass), the assertion passes and returns the exception instance for optional inspection (e.g., checking `e.getShortfall()`). If the lambda completes without throwing or throws an unexpected exception type, an `AssertionError` is raised.

In JUnit 4, the equivalent is either `@Test(expected = InsufficientFundsException.class)` or using `try-catch` with `Assertions.fail("Expected exception was not thrown")`.

---

### Q3: What does packaging the application as a runnable JAR provide?
**Answer:**  
A runnable Java Archive (JAR) packages all compiled `.class` files, metadata, and resources into a single compressed ZIP-compatible file. It provides:
1. **Portability & Distribution:** The entire application is bundled into a single file (`minibank.jar`) that can be transferred and executed on any system with a compatible JRE.
2. **Self-Contained Execution:** By specifying `Main-Class: service.MiniBank` in `manifest.mf`, the end user simply executes `java -jar minibank.jar` without needing to configure complex classpath arguments or know class directory layouts.
3. **Integrity & Security:** Protects code from accidental file corruption and supports digital code signing.

---

## 8. Git Commit & Tagging Reference

To conclude Milestone 3 and Practical 14:
```bash
git add .
git commit -m "Complete Practical 14 Capstone: Wire BankService, menu, batch processing, NIO reporting, tests and runnable JAR"
git tag lab-14
```
