package lab09;

/**
 * Practical 9 - Part A (Problem 1): Counter Race Condition
 * 
 * Demonstrates how concurrent updates to a shared counter without synchronization
 * lead to lost updates (race condition), and how intrinsic locking via the
 * 'synchronized' keyword guarantees atomicity and mutual exclusion.
 */
public class CounterRace {

    // Unsynchronized shared counter
    static class UnsynchronizedCounter {
        private int count = 0;

        public void increment() {
            // Non-atomic read-modify-write operation
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    // Synchronized shared counter
    static class SynchronizedCounter {
        private int count = 0;

        public synchronized void increment() {
            // Synchronized ensures only one thread executes this critical section at a time
            count++;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int numThreads = 10;
        final int incrementsPerThread = 10_000;
        final int expectedTotal = numThreads * incrementsPerThread;

        System.out.println("==================================================");
        System.out.println(" Practical 9 - Part A (1): Shared Counter Race    ");
        System.out.println("==================================================");
        System.out.println("Configuration: " + numThreads + " threads, " + incrementsPerThread + " increments each.");
        System.out.println("Expected Final Total: " + expectedTotal + "\n");

        // --- Test 1: Unsynchronized Counter ---
        System.out.println("--- Test 1: Running WITHOUT Synchronization ---");
        UnsynchronizedCounter unsafeCounter = new UnsynchronizedCounter();
        Thread[] unsafeThreads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            unsafeThreads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafeCounter.increment();
                }
            }, "UnsafeWorker-" + (i + 1));
            unsafeThreads[i].start();
        }

        for (Thread t : unsafeThreads) {
            t.join();
        }

        int unsafeResult = unsafeCounter.getCount();
        int unsafeLoss = expectedTotal - unsafeResult;
        System.out.println("Final Counter Value: " + unsafeResult);
        System.out.println("Lost Updates Count : " + unsafeLoss);
        if (unsafeResult < expectedTotal) {
            System.out.println("Result: RACE CONDITION CONFIRMED! Counter is lower than " + expectedTotal + ".");
        } else {
            System.out.println("Result: Threads unexpectedly did not interleave.");
        }

        // --- Test 2: Synchronized Counter ---
        System.out.println("\n--- Test 2: Running WITH 'synchronized' Keyword ---");
        SynchronizedCounter safeCounter = new SynchronizedCounter();
        Thread[] safeThreads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            safeThreads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    safeCounter.increment();
                }
            }, "SafeWorker-" + (i + 1));
            safeThreads[i].start();
        }

        for (Thread t : safeThreads) {
            t.join();
        }

        int safeResult = safeCounter.getCount();
        System.out.println("Final Counter Value: " + safeResult);
        System.out.println("Lost Updates Count : " + (expectedTotal - safeResult));
        System.out.println("Result: EXACT MATCH! Synchronization ensured perfect thread safety.");
    }
}
