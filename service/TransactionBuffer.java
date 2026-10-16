package service;

/**
 * Lab 10 - Part B (Requirement 3): TransactionBuffer
 * 
 * A simple Producer-Consumer shared circular array buffer.
 * - One thread (Producer) puts transaction tasks into the array buffer.
 * - Another thread (Consumer) takes transaction tasks and executes them.
 * - Uses wait() and notify() inside synchronized blocks to coordinate them.
 */
public class TransactionBuffer {
    // Shared fixed-size array buffer
    private final TransactionTask[] buffer;
    private int count = 0; // Number of items currently in the buffer
    private int in = 0;    // Index where producer inserts next item
    private int out = 0;   // Index where consumer removes next item

    public TransactionBuffer(int capacity) {
        this.buffer = new TransactionTask[capacity];
    }

    // Producer method: adds a transaction to the array buffer
    public void addTransaction(TransactionTask task) throws InterruptedException {
        // Synchronized block on this buffer object
        synchronized (this) {
            // While the array buffer is completely full, the producer must wait
            while (count == buffer.length) {
                System.out.println("  [Buffer FULL] Array is full (" + count + "/" + buffer.length 
                        + "). Producer is calling wait()...");
                wait(); // Releases lock and waits until consumer consumes an item
            }

            // Put task in array buffer at index 'in'
            buffer[in] = task;
            in = (in + 1) % buffer.length;
            count++;

            System.out.println("  [Producer] -> Added " + task + " | Items in buffer: " + count + "/" + buffer.length);

            // Notify consumer that a new transaction is ready in the buffer
            notify();
        }
    }

    // Consumer method: takes and returns a transaction from the array buffer
    public TransactionTask getTransaction() throws InterruptedException {
        // Synchronized block on this buffer object
        synchronized (this) {
            // While the array buffer is empty, consumer must wait
            while (count == 0) {
                System.out.println("  [Buffer EMPTY] No transactions in buffer. Consumer is calling wait()...");
                wait(); // Releases lock and waits until producer adds an item
            }

            // Take task from array buffer at index 'out'
            TransactionTask task = buffer[out];
            buffer[out] = null; // Clear reference
            out = (out + 1) % buffer.length;
            count--;

            System.out.println("  [Consumer] <- Removed " + task + " | Remaining in buffer: " + count + "/" + buffer.length);

            // Notify producer that space is now available in the array
            notify();

            return task;
        }
    }

    public synchronized int getCount() {
        return count;
    }

    public int getCapacity() {
        return buffer.length;
    }
}
