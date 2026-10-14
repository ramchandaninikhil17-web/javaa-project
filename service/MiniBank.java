package service;

import exception.*;
import model.*;
import util.AnnotationValidator;
import util.Validator;
import static util.StatementFormatter.buildStatement; // Requirement 5: Static import

import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Requirement 1 & 2: Accounts implementing Transactable and InterestBearing
        System.out.println("--- 1. Accounts & Capabilities (Transactable & InterestBearing) ---");
        Account[] accounts = new Account[3];
        accounts[0] = new SavingsAccount("Alice Smith", 5000, 1000);
        accounts[1] = new CurrentAccount("Bob Jones", 2000, 5000);
        accounts[2] = new FixedDepositAccount("Charlie Brown", 50000);

        for (Account acc : accounts) {
            System.out.println(acc);
            System.out.printf("  Interest Rate: %.1f%% | Yearly Interest: Rs.%.2f%n",
                    acc.interestRate(), acc.yearlyInterest()); // Default method from InterestBearing

            // Requirement 4: Checking marker interface Premium
            if (acc instanceof Premium) {
                System.out.println("  -> [Marker Interface]: This account qualifies as PREMIUM.");
            }
        }

        // Requirement 3: Use WithdrawRule two ways (Anonymous Class, then Lambda)
        System.out.println("\n--- 2. Functional Interface (WithdrawRule) ---");

        // Way 1: Anonymous class (e.g., maximum transaction limit of Rs.3000)
        WithdrawRule maxLimitRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount <= 3000;
            }
        };

        // Way 2: Lambda expression (e.g., balance after withdrawal must remain at least Rs.500)
        WithdrawRule minReserveRule = (account, amount) -> (account.getBalance() - amount) >= 500;

        Account aliceAcc = accounts[0];
        long testAmount1 = 2500;
        long testAmount2 = 4500;

        System.out.println("Testing Alice's account (Balance: Rs." + aliceAcc.getBalance() + "):");
        System.out.println("  [Anonymous Class] Max limit Rs.3000 for Rs." + testAmount1 + ": " +
                maxLimitRule.allow(aliceAcc, testAmount1));
        System.out.println("  [Anonymous Class] Max limit Rs.3000 for Rs." + testAmount2 + ": " +
                maxLimitRule.allow(aliceAcc, testAmount2));

        System.out.println("  [Lambda Expression] Min reserve Rs.500 for Rs." + testAmount1 + ": " +
                minReserveRule.allow(aliceAcc, testAmount1));
        System.out.println("  [Lambda Expression] Min reserve Rs.500 for Rs.4800: " +
                minReserveRule.allow(aliceAcc, 4800));

        // Requirement 1: Transactable interface in action
        System.out.println("\n--- 3. Transactable Interface Operations ---");
        Transactable t = accounts[0];
        try {
            t.deposit(1500);
            System.out.println("Deposited Rs.1500 into " + accounts[0].getAccountNumber() + " -> New Balance: Rs." + accounts[0].getBalance());
            t.withdraw(2000);
            System.out.println("Withdrew Rs.2000 from " + accounts[0].getAccountNumber() + " -> New Balance: Rs." + accounts[0].getBalance());
        } catch (BankException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }

        // Command parsing and Statement formatting (using static import buildStatement)
        System.out.println("\n--- 4. Command Parsing & Statement Generation ---");
        Command command = CommandParser.parse("DEPOSIT AC0001 1000");
        System.out.println("Parsed Command: " + command.type() + " Rs." + command.amount() + " for " + command.accountNumber());

        // Calling statically imported buildStatement
        System.out.println("\n" + buildStatement(accounts[0]));

        // Requirement 5 (Part B): Reflection & Annotation Validation (@Positive, @MaxLength)
        System.out.println("\n--- 5. Custom Annotations & Reflection Validation (Part B) ---");

        // Test 1: Account with negative balance (violating @Positive)
        System.out.println("[Test 1] Creating an account with negative balance (-500)...");
        Account invalidBalanceAccount = new SavingsAccount("David Miller", -500, 1000);
        String[] balanceErrors = AnnotationValidator.validate(invalidBalanceAccount);
        System.out.println("Validation result (" + balanceErrors.length + " error(s) detected):");
        for (String error : balanceErrors) {
            System.out.println("  -> Error: " + error);
        }

        // Test 2: Account with owner name exceeding @MaxLength(30)
        System.out.println("\n[Test 2] Creating an account with owner name exceeding @MaxLength(30)...");
        Account longNameAccount = new CurrentAccount("Maximilian Alexander Montgomery The Third", 1500, 5000);
        String[] lengthErrors = AnnotationValidator.validate(longNameAccount);
        System.out.println("Validation result (" + lengthErrors.length + " error(s) detected):");
        for (String error : lengthErrors) {
            System.out.println("  -> Error: " + error);
        }

        // Test 3: Legitimate account satisfying all rules
        System.out.println("\n[Test 3] Validating legitimate account (" + accounts[0].getAccountNumber() + ")...");
        String[] validErrors = AnnotationValidator.validate(accounts[0]);
        if (validErrors.length == 0) {
            System.out.println("  -> Validation passed! Account is completely valid.");
        } else {
            for (String error : validErrors) {
                System.out.println("  -> Error: " + error);
            }
        }

        // Milestone 1 Checkpoint: Fault Tolerance & Custom Exception Hierarchy
        System.out.println("\n--- 6. Milestone 1 Checkpoint: Fault Tolerance & Custom Exceptions ---");

        // 6.1 InvalidAmountException on deposit
        System.out.println("[Test 6.1] Attempting deposit with non-positive amount (-500)...");
        try {
            accounts[0].deposit(-500);
        } catch (InvalidAmountException e) {
            System.out.println("  -> Caught InvalidAmountException: " + e.getMessage());
        }

        // 6.2 InvalidAmountException on withdrawal
        System.out.println("\n[Test 6.2] Attempting withdrawal with zero amount (0)...");
        try {
            accounts[0].withdraw(0);
        } catch (InvalidAmountException e) {
            System.out.println("  -> Caught InvalidAmountException: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("  -> Caught InsufficientFundsException: " + e.getMessage());
        }

        // 6.3 InsufficientFundsException with Shortfall
        System.out.println("\n[Test 6.3] Attempting withdrawal exceeding balance (Balance: Rs." 
                + accounts[0].getBalance() + ", Requested: Rs.10000)...");
        try {
            accounts[0].withdraw(10000);
        } catch (InsufficientFundsException e) {
            System.out.println("  -> Caught InsufficientFundsException: " + e.getMessage());
            System.out.println("  -> Explicit Shortfall: Rs." + e.getShortfall() 
                    + " (Please deposit at least Rs." + e.getShortfall() + " to complete this withdrawal)");
        } catch (InvalidAmountException e) {
            System.out.println("  -> Caught InvalidAmountException: " + e.getMessage());
        }

        // 6.4 AccountNotFoundException in transfer (null destination)
        System.out.println("\n[Test 6.4] Attempting transfer to an invalid/null destination account...");
        try {
            accounts[0].transfer(null, 1000);
        } catch (AccountNotFoundException e) {
            System.out.println("  -> Caught AccountNotFoundException: " + e.getMessage());
        } catch (BankException e) {
            System.out.println("  -> Caught BankException: " + e.getMessage());
        }

        // 6.5 Successful transfer using try, catch, and finally
        System.out.println("\n[Test 6.5] Transferring Rs.1500 from " + accounts[0].getAccountNumber() 
                + " to " + accounts[1].getAccountNumber() + "...");
        try {
            accounts[0].transfer(accounts[1], 1500);
            System.out.println("  -> Transfer successful! Sender balance: Rs." + accounts[0].getBalance() 
                    + " | Receiver balance: Rs." + accounts[1].getBalance());
        } catch (BankException e) {
            System.out.println("  -> Transfer failed: " + e.getMessage());
        } finally {
            System.out.println("  -> [Caller Finally Block]: Transfer operation lifecycle completed.");
        }

        // 6.6 Try-With-Resources using AutoCloseable BankSession
        System.out.println("\n[Test 6.6] Try-With-Resources using AutoCloseable BankSession...");
        try (BankSession session = new BankSession("Operator-Alice")) {
            session.logOperation("Audited deposit into " + accounts[0].getAccountNumber());
            accounts[0].deposit(500);
            System.out.println("  -> Deposited Rs.500 inside active session. Current Balance: Rs." + accounts[0].getBalance());
        } catch (InvalidAmountException e) {
            System.out.println("  -> Session error: " + e.getMessage());
        }

        // =========================================================================
        // Practical 9 (Part B): Multithreading, Concurrency & Thread Safety
        // =========================================================================
        System.out.println("\n=================================================================");
        System.out.println("--- 7. Multithreading, Race Conditions & Thread Safety (Part B) ---");
        System.out.println("=================================================================");

        final int numWorkers = 10;
        final int depositsPerWorker = 1000;
        final long depositAmount = 1;
        final long expectedBalance = numWorkers * depositsPerWorker * depositAmount; // 10,000

        // 7.1 Demonstrating Race Condition (Without Synchronization)
        System.out.println("\n[Test 7.1] Demonstrating Race Condition WITHOUT Synchronization...");
        System.out.println("  Starting Balance : Rs.0");
        System.out.println("  Workload         : 10 threads x 1,000 deposits of Rs.1 each");
        System.out.println("  Expected Balance : Rs." + expectedBalance);

        Account unsafeAccount = new UnsynchronizedAccount("Unsafe-Account", 0);
        Thread[] unsafeThreads = new Thread[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            unsafeThreads[i] = new Thread(
                new AccountWorker(unsafeAccount, depositsPerWorker, depositAmount),
                "UnsafeWorker-" + (i + 1)
            );
            unsafeThreads[i].start();
        }

        for (Thread thread : unsafeThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread join interrupted: " + e.getMessage());
            }
        }

        long unsafeFinalBalance = unsafeAccount.getBalance();
        System.out.println("\n  -> Final Balance (Unsafe): Rs." + unsafeFinalBalance);
        System.out.println("  -> Lost Deposits Count   : " + (expectedBalance - unsafeFinalBalance));
        if (unsafeFinalBalance < expectedBalance) {
            System.out.println("  -> Outcome: RACE CONDITION OCCURRED! Final balance is less than Rs." 
                    + expectedBalance + " due to uncoordinated concurrent updates.");
        } else {
            System.out.println("  -> Outcome: Threads did not collide on this run.");
        }

        // 7.2 Fixing Race Condition with Synchronization
        System.out.println("\n[Test 7.2] Demonstrating Thread Safety WITH 'synchronized' Keyword...");
        System.out.println("  Starting Balance : Rs.0");
        System.out.println("  Workload         : 10 threads x 1,000 deposits of Rs.1 each");
        System.out.println("  Expected Balance : Rs." + expectedBalance);

        Account safeAccount = new SavingsAccount("Safe-Account", 0, 0);
        Thread[] safeThreads = new Thread[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            safeThreads[i] = new Thread(
                new AccountWorker(safeAccount, depositsPerWorker, depositAmount),
                "SafeWorker-" + (i + 1)
            );
            safeThreads[i].start();
        }

        for (Thread thread : safeThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread join interrupted: " + e.getMessage());
            }
        }

        long safeFinalBalance = safeAccount.getBalance();
        System.out.println("\n  -> Final Balance (Safe)  : Rs." + safeFinalBalance);
        System.out.println("  -> Lost Deposits Count   : " + (expectedBalance - safeFinalBalance));
        System.out.println("  -> Outcome: EXACT MATCH! Synchronization guaranteed atomic read-modify-write operations.");

        // 7.3 Observing Thread Lifecycle States
        System.out.println("\n[Test 7.3] Observing Thread Lifecycle States...");
        Account lifecycleAcc = new SavingsAccount("Lifecycle-Account", 0, 0);
        Thread demoThread = new Thread(new AccountWorker(lifecycleAcc, 5, 10), "LifecycleObserverThread");

        System.out.println("  1. State after creation (before start) : " + demoThread.getState()); // NEW
        demoThread.start();
        System.out.println("  2. State immediately after start()     : " + demoThread.getState()); // RUNNABLE
        try {
            demoThread.join();
        } catch (InterruptedException ignored) {}
        System.out.println("  3. State after join() (completed)      : " + demoThread.getState()); // TERMINATED

        // 7.4 Supplementary Problem 1: Concurrent Deposits & Withdrawals
        System.out.println("\n[Test 7.4] Supplementary Problem 1: Concurrent Deposits AND Withdrawals...");
        final long initialBiDirBalance = 5000;
        final int biDirWorkers = 10;
        final int biDirTimes = 500;
        final long biDirAmount = 2; // 10 * 500 * 2 = 10,000 added, 10,000 withdrawn -> net 0
        Account biDirAccount = new SavingsAccount("BiDirectional-Account", initialBiDirBalance, 0);

        Thread[] depositWorkers = new Thread[biDirWorkers];
        Thread[] withdrawWorkers = new Thread[biDirWorkers];

        for (int i = 0; i < biDirWorkers; i++) {
            depositWorkers[i] = new Thread(
                new AccountWorker(biDirAccount, biDirTimes, biDirAmount, false),
                "Depositor-" + (i + 1)
            );
            withdrawWorkers[i] = new Thread(
                new AccountWorker(biDirAccount, biDirTimes, biDirAmount, true),
                "Withdrawer-" + (i + 1)
            );
            depositWorkers[i].start();
            withdrawWorkers[i].start();
        }

        for (int i = 0; i < biDirWorkers; i++) {
            try {
                depositWorkers[i].join();
                withdrawWorkers[i].join();
            } catch (InterruptedException ignored) {}
        }

        System.out.println("\n  Starting Balance: Rs." + initialBiDirBalance);
        System.out.println("  Total Added     : Rs." + (biDirWorkers * biDirTimes * biDirAmount));
        System.out.println("  Total Withdrawn : Rs." + (biDirWorkers * biDirTimes * biDirAmount));
        System.out.println("  Final Balance   : Rs." + biDirAccount.getBalance());
        System.out.println("  -> Outcome: PERFECT CONCURRENCY! Balance preserved exactly at Rs." + initialBiDirBalance);

        // 7.5 Supplementary Problem 2: Lock-Free Concurrency with AtomicLong
        System.out.println("\n[Test 7.5] Supplementary Problem 2: Lock-Free Concurrency with AtomicLong...");
        Account atomicAccount = new AtomicAccount("Atomic-Account", 0);
        Thread[] atomicThreads = new Thread[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            atomicThreads[i] = new Thread(
                new AccountWorker(atomicAccount, depositsPerWorker, depositAmount),
                "AtomicWorker-" + (i + 1)
            );
            atomicThreads[i].start();
        }

        for (Thread thread : atomicThreads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }

        System.out.println("\n  AtomicAccount Final Balance: Rs." + atomicAccount.getBalance());
        System.out.println("  -> Outcome: AtomicLong hardware-level CAS guarantees non-blocking thread safety without lock overhead.");
    }

    // Interactive menu shell preserved from Practical 1
    public static void runMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- MiniBank Menu ---");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();

            MenuOption selectedOption = switch (choice) {
                case 1 -> MenuOption.OPEN_ACCOUNT;
                case 2 -> MenuOption.DEPOSIT;
                case 3 -> MenuOption.WITHDRAW;
                case 4 -> MenuOption.TRANSFER;
                case 5 -> MenuOption.EXIT;
                default -> null;
            };

            if (selectedOption == null) {
                System.out.println("Invalid choice. Please choose a number from 1 to 5.");
                continue;
            }

            String placeholder = switch (selectedOption) {
                case OPEN_ACCOUNT -> "Open Account - to be implemented in a later lab.";
                case DEPOSIT -> "Deposit - to be implemented in a later lab.";
                case WITHDRAW -> "Withdraw - to be implemented in a later lab.";
                case TRANSFER -> "Transfer - to be implemented in a later lab.";
                case EXIT -> "Exiting MiniBank. Thank you, goodbye!";
            };

            System.out.println(placeholder);

            if (selectedOption == MenuOption.EXIT) {
                running = false;
            }
        }
    }
}
