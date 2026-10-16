package service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Lab 10 - Part B (Requirement 1): TransactionProcessor
 * 
 * Manages a fixed thread pool of 4 threads to process banking transactions
 * concurrently instead of creating new threads manually every time.
 */
public class TransactionProcessor {
    // 1. Holds an ExecutorService created with Executors.newFixedThreadPool(4)
    private final ExecutorService executor;

    public TransactionProcessor() {
        // Creates a pool with 4 worker threads ready to take tasks from the queue
        this.executor = Executors.newFixedThreadPool(4);
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
