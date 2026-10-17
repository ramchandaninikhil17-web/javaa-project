package service;

import model.Account;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Lab 10 - Part B & Milestone 2 Checkpoint: TransactionProcessor
 * 
 * Manages a fixed thread pool of 4 threads to process banking transactions concurrently.
 * Requirement 5: Uses a ConcurrentHashMap<String, Account> for shared accounts so access is thread-safe.
 */
public class TransactionProcessor {
    // 1. Holds an ExecutorService created with Executors.newFixedThreadPool(4)
    private final ExecutorService executor;

    // Requirement 5: Thread-safe ConcurrentHashMap for shared accounts
    private final ConcurrentHashMap<String, Account> sharedAccounts = new ConcurrentHashMap<>();

    public TransactionProcessor() {
        // Creates a pool with 4 worker threads ready to take tasks from the queue
        this.executor = Executors.newFixedThreadPool(4);
    }

    /**
     * Requirement 5: Registers an account into the thread-safe ConcurrentHashMap.
     */
    public void addAccount(Account account) {
        if (account != null) {
            sharedAccounts.put(account.getAccountNumber(), account);
        }
    }

    /**
     * Requirement 5: Retrieves an account by account number from the thread-safe ConcurrentHashMap.
     */
    public Account getAccount(String accountNumber) {
        return sharedAccounts.get(accountNumber);
    }

    /**
     * Requirement 5: Returns the thread-safe map of shared accounts.
     */
    public ConcurrentHashMap<String, Account> getSharedAccounts() {
        return sharedAccounts;
    }

    // 2. Submit method that passes a Runnable task to the executor
    public void submit(Runnable task) {
        executor.execute(task);
    }

    // 3. Stop method that shuts down the thread pool and waits for tasks to finish
    public void stop() {
        System.out.println("  [TransactionProcessor] Stopping processor... Initiating shutdown.");
        executor.shutdown(); // Stops accepting new tasks
        try {
            // Await termination: wait up to 10 seconds for currently running tasks to finish
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("  [TransactionProcessor] Tasks took too long, forcing shutdownNow()...");
                executor.shutdownNow();
            } else {
                System.out.println("  [TransactionProcessor] All tasks finished cleanly. Pool stopped.");
            }
        } catch (InterruptedException e) {
            System.out.println("  [TransactionProcessor] Interrupted while waiting for tasks to finish.");
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }
}
