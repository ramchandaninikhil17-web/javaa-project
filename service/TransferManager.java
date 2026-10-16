package service;

import exception.BankException;
import model.Account;

/**
 * Lab 10 - Part B (Requirement 4): Deadlock Demonstration & Resolution
 * 
 * Demonstrates:
 * 1. How transferring money A -> B and B -> A with opposite lock order causes DEADLOCK.
 * 2. How always acquiring the lock on the smaller accountNumber first prevents DEADLOCK completely.
 */
public class TransferManager {

    /**
     * BAD TRANSFER (Deadlock-prone):
     * Locks the 'from' account first, then locks the 'to' account.
     * If Thread 1 is moving A -> B (locks A, waits for B)
     * and Thread 2 is moving B -> A (locks B, waits for A),
     * a circular wait is created -> DEADLOCK!
     */
    public static void transferUnsafe(Account from, Account to, long amount) throws BankException {
        String threadName = Thread.currentThread().getName();

        System.out.println("  [" + threadName + "] Trying to lock FROM account: " + from.getAccountNumber());
        synchronized (from) {
            System.out.println("  [" + threadName + "] >>> LOCKED " + from.getAccountNumber() 
                    + ". Now sleeping 150ms before asking for " + to.getAccountNumber() + "...");
            try {
                // Sleep gives the other thread enough time to acquire its first lock
                Thread.sleep(150);
            } catch (InterruptedException ignored) {}

            System.out.println("  [" + threadName + "] Now waiting to lock TO account: " + to.getAccountNumber() + "...");
            synchronized (to) {
                // In deadlock scenario, threads will NEVER reach here!
                System.out.println("  [" + threadName + "] >>> LOCKED " + to.getAccountNumber() + ". Performing transfer!");
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    /**
     * GOOD TRANSFER (Deadlock-free):
     * Fixes deadlock by consistent lock ordering.
     * We compare account numbers and ALWAYS lock the account with the smaller
     * accountNumber first, regardless of which account is sender or receiver.
     */
    public static void transferSafe(Account from, Account to, long amount) throws BankException {
        String threadName = Thread.currentThread().getName();

        // 1. Identify which account has the smaller accountNumber
        Account firstLock;
        Account secondLock;

        if (from.getAccountNumber().compareTo(to.getAccountNumber()) < 0) {
            firstLock = from;
            secondLock = to;
        } else {
            firstLock = to;
            secondLock = from;
        }

        System.out.println("  [" + threadName + "] Lock Hierarchy -> 1st: " 
                + firstLock.getAccountNumber() + " | 2nd: " + secondLock.getAccountNumber());

        // 2. Always lock smaller accountNumber first!
        synchronized (firstLock) {
            System.out.println("  [" + threadName + "] >>> Step 1: Locked smaller account (" 
                    + firstLock.getAccountNumber() + ")");
            try {
                Thread.sleep(80); // Small pause to show other thread waits safely
            } catch (InterruptedException ignored) {}

            // 3. Then lock the larger accountNumber second
            synchronized (secondLock) {
                System.out.println("  [" + threadName + "] >>> Step 2: Locked larger account (" 
                        + secondLock.getAccountNumber() + "). Performing transfer...");
                from.withdraw(amount);
                to.deposit(amount);
                System.out.println("  [" + threadName + "] SUCCESS: Transferred Rs." + amount 
                        + " from " + from.getAccountNumber() + " to " + to.getAccountNumber());
            }
        }
    }
}
