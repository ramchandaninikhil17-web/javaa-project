import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class BankTask implements Runnable {
    private int taskId;

    public BankTask(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("Task " + taskId + " started by [" + threadName + "]");
        try {
            Thread.sleep(300); // brief sleep
        } catch (InterruptedException e) {
            System.out.println("Task " + taskId + " was interrupted.");
        }
        System.out.println("Task " + taskId + " finished by [" + threadName + "]");
    }
}

public class ThreadPoolRunner {
    public static void main(String[] args) {
        // Fixed thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("=== Thread Pool Runner ===");
        System.out.println("Pool size: 3 | Total tasks: 10\n");

        // Submit 10 tasks to show thread reuse
        for (int i = 1; i <= 10; i++) {
            executor.execute(new BankTask(i));
        }

        // Shut down executor and wait for termination
        executor.shutdown();

        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("\nAll tasks completed successfully. Thread pool shut down.");
            } else {
                System.out.println("\nTimeout elapsed before completion.");
            }
        } catch (InterruptedException e) {
            System.out.println("Execution interrupted: " + e.getMessage());
        }
    }
}
