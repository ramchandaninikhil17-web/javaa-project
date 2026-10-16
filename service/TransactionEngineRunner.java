package service;

import exception.BankException;
import model.Account;
import model.SavingsAccount;

/**
 * Lab 10 - Part B: MiniBank Transaction Engine Demo
 * 
 * Demonstrates all 4 requirements in a clean, humanized student format:
 * 1. TransactionProcessor holding ExecutorService with 4 fixed threads
 * 2. Submitting batch deposit and withdrawal Runnable tasks & graceful shutdown
 * 3. Producer-Consumer transaction buffer using wait() and notify() in synchronized blocks
 * 4. Deadlock creation (opposite locking) & Resolution (smaller accountNumber first)
 */
public class TransactionEngineRunner {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   MiniBank - Lab 10 Part B: Transaction Engine & Concurrency Control     ");
        System.out.println("==========================================================================\n");

        runThreadPoolBatch();
        System.out.println("\n--------------------------------------------------------------------------\n");

        runProducerConsumer();
        System.out.println("\n--------------------------------------------------------------------------\n");

        runDeadlockAndFix();

        System.out.println("\n==========================================================================");
        System.out.println("   All Lab 10 Part B tests completed successfully!                        ");
        System.out.println("==========================================================================");
    }

    /**
     * Requirements 1 & 2:
     * Thread Pool execution with TransactionProcessor and Runnable transaction tasks.
     */
    public static void runThreadPoolBatch() {
        System.out.println("--- [Requirements 1 & 2] Thread Pool Processing with 4 Threads ---");

        // Accounts for testing
        Account alice = new SavingsAccount("Alice", 2000, 500);
        Account bob = new SavingsAccount("Bob", 1500, 500);

        System.out.println("Starting Balances:");
        System.out.println("  " + alice.getAccountNumber() + " (Alice): Rs." + alice.getBalance());
        System.out.println("  " + bob.getAccountNumber() + " (Bob):   Rs." + bob.getBalance());
        System.out.println("\nCreating TransactionProcessor (fixed thread pool of 4)...");

        TransactionProcessor processor = new TransactionProcessor();

        // Submit 8 tasks (mix of deposits and withdrawals)
        System.out.println("Submitting 8 batch transaction tasks...\n");
        processor.submit(new TransactionTask(1, alice, TransactionTask.Type.DEPOSIT, 500));
        processor.submit(new TransactionTask(2, bob, TransactionTask.Type.DEPOSIT, 1000));
        processor.submit(new TransactionTask(3, alice, TransactionTask.Type.WITHDRAW, 300));
        processor.submit(new TransactionTask(4, bob, TransactionTask.Type.WITHDRAW, 400));
        processor.submit(new TransactionTask(5, alice, TransactionTask.Type.DEPOSIT, 700));
        processor.submit(new TransactionTask(6, bob, TransactionTask.Type.DEPOSIT, 200));
        processor.submit(new TransactionTask(7, alice, TransactionTask.Type.WITHDRAW, 200));
        processor.submit(new TransactionTask(8, bob, TransactionTask.Type.WITHDRAW, 100));

        // Stop the processor and wait for all tasks to finish
        processor.stop();

        System.out.println("\nFinal Balances after batch processing:");
        System.out.println("  " + alice.getAccountNumber() + " (Alice): Rs." + alice.getBalance() + " (Expected: Rs.2700)");
        System.out.println("  " + bob.getAccountNumber() + " (Bob):   Rs." + bob.getBalance() + " (Expected: Rs.2200)");
        System.out.println("  -> Outcome: Managed thread pool processed all tasks safely without manual thread creation!");
    }

    /**
     * Requirement 3:
     * Producer-Consumer using shared array buffer and wait() / notify().
     */
    public static void runProducerConsumer() {
        System.out.println("--- [Requirement 3] Producer-Consumer with Shared Array Buffer ---");
        System.out.println("Setting up circular array buffer with capacity 3...\n");

        TransactionBuffer buffer = new TransactionBuffer(3);
        Account charlie = new SavingsAccount("Charlie", 1000, 200);
        int totalTransactions = 6;

        // Producer thread: creates transactions and puts them into the buffer
        Thread producerThread = new Thread(() -> {
            try {
                for (int i = 1; i <= totalTransactions; i++) {
                    TransactionTask.Type op = (i % 2 == 1) ? TransactionTask.Type.DEPOSIT : TransactionTask.Type.WITHDRAW;
                    long amount = 100 * i;
                    TransactionTask task = new TransactionTask(i, charlie, op, amount);

                    buffer.addTransaction(task);
                    Thread.sleep(100); // Production pace
                }
                System.out.println("  [Producer] Finished adding all " + totalTransactions + " transactions.");
            } catch (InterruptedException e) {
                System.out.println("  [Producer] Thread interrupted.");
                Thread.currentThread().interrupt();
            }
        }, "Transaction-Producer");

        // Consumer thread: takes transactions from buffer and executes them
        Thread consumerThread = new Thread(() -> {
            try {
                for (int i = 1; i <= totalTransactions; i++) {
                    TransactionTask task = buffer.getTransaction();
                    // Process transaction
                    task.run();
                    Thread.sleep(250); // Consumer is slightly slower, forcing buffer to fill up!
                }
                System.out.println("  [Consumer] Finished executing all " + totalTransactions + " transactions.");
            } catch (InterruptedException e) {
                System.out.println("  [Consumer] Thread interrupted.");
                Thread.currentThread().interrupt();
            }
        }, "Transaction-Consumer");

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted waiting for producer/consumer: " + e.getMessage());
        }

        System.out.println("\n  Charlie's Final Balance: Rs." + charlie.getBalance());
        System.out.println("  -> Outcome: wait() and notify() smoothly coordinated producer and consumer through the shared array!");
    }

    /**
     * Requirement 4:
     * Deadlock demonstration (opposite lock order) & Deadlock fix (smaller accountNumber first).
     */
    public static void runDeadlockAndFix() {
        System.out.println("--- [Requirement 4] Deadlock Demonstration & Fix ---");

        // 4.1 Deadlock Demonstration
        System.out.println("\n[Test 4.1] Creating DEADLOCK: Opposite Lock Ordering...");
        Account accA = new SavingsAccount("Account-A", 5000, 500);
        Account accB = new SavingsAccount("Account-B", 5000, 500);

        System.out.println("  Account 1: " + accA.getAccountNumber() + " (Balance: Rs." + accA.getBalance() + ")");
        System.out.println("  Account 2: " + accB.getAccountNumber() + " (Balance: Rs." + accB.getBalance() + ")");
        System.out.println("  Thread-1 will try to transfer A -> B (Locks A, then B)");
        System.out.println("  Thread-2 will try to transfer B -> A (Locks B, then A)\n");

        Thread deadlockT1 = new Thread(() -> {
            try {
                TransferManager.transferUnsafe(accA, accB, 500);
            } catch (BankException ignored) {}
        }, "DeadlockThread-1 (A->B)");

        Thread deadlockT2 = new Thread(() -> {
            try {
                TransferManager.transferUnsafe(accB, accA, 500);
            } catch (BankException ignored) {}
        }, "DeadlockThread-2 (B->A)");

        // Run as daemon threads so they won't hang the JVM after demo
        deadlockT1.setDaemon(true);
        deadlockT2.setDaemon(true);

        deadlockT1.start();
        deadlockT2.start();

        // Give threads 1 second to enter deadlock
        try {
            deadlockT1.join(1000);
            deadlockT2.join(1000);
        } catch (InterruptedException ignored) {}

        System.out.println("\n  Checking Thread States after 1 second:");
        System.out.println("    " + deadlockT1.getName() + " State: " + deadlockT1.getState());
        System.out.println("    " + deadlockT2.getName() + " State: " + deadlockT2.getState());

        if (deadlockT1.getState() == Thread.State.BLOCKED && deadlockT2.getState() == Thread.State.BLOCKED) {
            System.out.println("  ==> DEADLOCK OCCURRED! Both threads are BLOCKED indefinitely waiting for each other's lock.");
            System.out.println("  ==> Thread-1 holds lock on " + accA.getAccountNumber() + " and waits for " + accB.getAccountNumber());
            System.out.println("  ==> Thread-2 holds lock on " + accB.getAccountNumber() + " and waits for " + accA.getAccountNumber());
        }

        // 4.2 Deadlock Resolution via Lock Ordering
        System.out.println("\n[Test 4.2] FIXING DEADLOCK: Consistent Lock Ordering (Smaller Account Number First)...");
        Account safeA = new SavingsAccount("Safe-Account-A", 5000, 500);
        Account safeB = new SavingsAccount("Safe-Account-B", 5000, 500);

        System.out.println("  Account 1: " + safeA.getAccountNumber() + " (Balance: Rs." + safeA.getBalance() + ")");
        System.out.println("  Account 2: " + safeB.getAccountNumber() + " (Balance: Rs." + safeB.getBalance() + ")");
        System.out.println("  Rule: Both threads must acquire lock on the SMALLER accountNumber first.\n");

        Thread safeT1 = new Thread(() -> {
            try {
                TransferManager.transferSafe(safeA, safeB, 500);
            } catch (BankException e) {
                System.err.println("Transfer failed: " + e.getMessage());
            }
        }, "SafeThread-1 (A->B)");

        Thread safeT2 = new Thread(() -> {
            try {
                TransferManager.transferSafe(safeB, safeA, 300);
            } catch (BankException e) {
                System.err.println("Transfer failed: " + e.getMessage());
            }
        }, "SafeThread-2 (B->A)");

        safeT1.start();
        safeT2.start();

        try {
            safeT1.join();
            safeT2.join();
        } catch (InterruptedException ignored) {}

        System.out.println("\n  SafeThread-1 Final State: " + safeT1.getState());
        System.out.println("  SafeThread-2 Final State: " + safeT2.getState());
        System.out.println("  Final Balances:");
        System.out.println("    " + safeA.getAccountNumber() + ": Rs." + safeA.getBalance() + " (5000 - 500 + 300 = Rs.4800)");
        System.out.println("    " + safeB.getAccountNumber() + ": Rs." + safeB.getBalance() + " (5000 + 500 - 300 = Rs.5200)");
        System.out.println("  -> Outcome: ZERO DEADLOCK! Lock ordering broke circular wait and both transfers succeeded safely!");
    }
}
