package lab09;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Practical 9 - Part A (Problem 3 - Advanced Learners): Parallel Array Summation
 * 
 * Splits a large array across multiple threads to compute the total sum:
 * 1. Approach 0 (Unsynchronized): Direct concurrent addition leads to race conditions.
 * 2. Approach 1 (Synchronized / Atomic): Lock/CAS on every addition guarantees correctness
 *    but suffers heavy lock contention overhead.
 * 3. Approach 2 (Thread-Local Accumulators): Each thread calculates a local sum independently
 *    and aggregates at the end — optimal performance and zero lock contention.
 */
public class ArraySumComparison {

    public static void main(String[] args) throws InterruptedException {
        final int arraySize = 5_000_000;
        final int numThreads = 4;
        final int chunkSize = arraySize / numThreads;

        // Initialize array with 1s so the expected sum is exactly arraySize
        long[] numbers = new long[arraySize];
        Arrays.fill(numbers, 1L);
        final long expectedSum = (long) arraySize;

        System.out.println("============================================================");
        System.out.println(" Practical 9 - Part A (3): Parallel Array Sum Comparison   ");
        System.out.println("============================================================");
        System.out.println("Array Size   : " + arraySize);
        System.out.println("Worker Threads: " + numThreads);
        System.out.println("Expected Sum : " + expectedSum + "\n");

        // -------------------------------------------------------------
        // Approach 0: Unsynchronized Shared Total (Wrong Result)
        // -------------------------------------------------------------
        class UnsafeSumHolder {
            long total = 0;
            void add(long val) { total += val; }
        }
        UnsafeSumHolder unsafeHolder = new UnsafeSumHolder();
        Thread[] threads0 = new Thread[numThreads];

        long startTime0 = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = (i == numThreads - 1) ? arraySize : (i + 1) * chunkSize;
            threads0[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    unsafeHolder.add(numbers[j]);
                }
            });
            threads0[i].start();
        }
        for (Thread t : threads0) t.join();
        long elapsed0 = System.currentTimeMillis() - startTime0;

        System.out.println("--- Approach 0: Unsynchronized Shared Accumulator ---");
        System.out.println("Computed Sum : " + unsafeHolder.total);
        System.out.println("Time Elapsed : " + elapsed0 + " ms");
        System.out.println("Outcome      : " + (unsafeHolder.total == expectedSum ? "Accidentally correct" : "WRONG (Lost Updates)") + "\n");

        // -------------------------------------------------------------
        // Approach 1 (Fix Way 1): AtomicLong Shared Accumulator
        // -------------------------------------------------------------
        AtomicLong atomicTotal = new AtomicLong(0);
        Thread[] threads1 = new Thread[numThreads];

        long startTime1 = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = (i == numThreads - 1) ? arraySize : (i + 1) * chunkSize;
            threads1[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    atomicTotal.addAndGet(numbers[j]);
                }
            });
            threads1[i].start();
        }
        for (Thread t : threads1) t.join();
        long elapsed1 = System.currentTimeMillis() - startTime1;

        System.out.println("--- Approach 1 (Fix Way 1): AtomicLong Per-Element CAS ---");
        System.out.println("Computed Sum : " + atomicTotal.get());
        System.out.println("Time Elapsed : " + elapsed1 + " ms");
        System.out.println("Outcome      : " + (atomicTotal.get() == expectedSum ? "CORRECT (High Contention)" : "WRONG") + "\n");

        // -------------------------------------------------------------
        // Approach 2 (Fix Way 2): Local Accumulation (Divide & Conquer)
        // -------------------------------------------------------------
        long[] partialSums = new long[numThreads];
        Thread[] threads2 = new Thread[numThreads];

        long startTime2 = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            final int start = i * chunkSize;
            final int end = (i == numThreads - 1) ? arraySize : (i + 1) * chunkSize;
            threads2[i] = new Thread(() -> {
                long localSum = 0;
                for (int j = start; j < end; j++) {
                    localSum += numbers[j];
                }
                partialSums[threadIndex] = localSum;
            });
            threads2[i].start();
        }
        for (Thread t : threads2) t.join();

        long combinedTotal = 0;
        for (long partial : partialSums) {
            combinedTotal += partial;
        }
        long elapsed2 = System.currentTimeMillis() - startTime2;

        System.out.println("--- Approach 2 (Fix Way 2): Thread-Local Partial Sums ---");
        System.out.println("Computed Sum : " + combinedTotal);
        System.out.println("Time Elapsed : " + elapsed2 + " ms");
        System.out.println("Outcome      : " + (combinedTotal == expectedSum ? "CORRECT & FAST (Zero Contention)" : "WRONG") + "\n");

        // Summary Performance Decision
        System.out.println("=== Performance Interpretation ===");
        System.out.println("Approach 2 is " + (elapsed1 > 0 ? (elapsed1 / Math.max(1, elapsed2)) : "multiple")
                + "x faster than Approach 1 because it eliminates inter-core cache-line ping-pong (false sharing) and locking overhead.");
    }
}
