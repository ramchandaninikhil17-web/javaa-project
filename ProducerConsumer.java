import java.util.LinkedList;
import java.util.Queue;

class SharedBuffer {
    private Queue<Integer> queue = new LinkedList<>();
    private int capacity;

    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(int item) throws InterruptedException {
        while (queue.size() == capacity) {
            System.out.println("Buffer is full (capacity " + capacity + "). Producer waiting...");
            wait();
        }
        queue.add(item);
        System.out.println("Produced item: " + item + " | Buffer size: " + queue.size());
        notifyAll();
    }

    public synchronized int consume() throws InterruptedException {
        while (queue.isEmpty()) {
            System.out.println("Buffer is empty. Consumer waiting...");
            wait();
        }
        int item = queue.poll();
        System.out.println("Consumed item: " + item + " | Buffer size: " + queue.size());
        notifyAll();
        return item;
    }
}

class Producer implements Runnable {
    private SharedBuffer buffer;
    private int totalItems;

    public Producer(SharedBuffer buffer, int totalItems) {
        this.buffer = buffer;
        this.totalItems = totalItems;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= totalItems; i++) {
                buffer.produce(i);
                Thread.sleep(200); // production delay
            }
        } catch (InterruptedException e) {
            System.out.println("Producer interrupted.");
        }
    }
}

class Consumer implements Runnable {
    private SharedBuffer buffer;
    private int totalItems;

    public Consumer(SharedBuffer buffer, int totalItems) {
        this.buffer = buffer;
        this.totalItems = totalItems;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= totalItems; i++) {
                buffer.consume();
                Thread.sleep(400); // consumption delay
            }
        } catch (InterruptedException e) {
            System.out.println("Consumer interrupted.");
        }
    }
}

public class ProducerConsumer {
    public static void main(String[] args) {
        int bufferCapacity = 3;
        int totalItems = 8;

        SharedBuffer buffer = new SharedBuffer(bufferCapacity);

        System.out.println("=== Producer - Consumer Practical ===");
        System.out.println("Buffer capacity: " + bufferCapacity + " | Total items: " + totalItems + "\n");

        Thread producerThread = new Thread(new Producer(buffer, totalItems), "Producer-Thread");
        Thread consumerThread = new Thread(new Consumer(buffer, totalItems), "Consumer-Thread");

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted: " + e.getMessage());
        }

        System.out.println("\nAll items produced and consumed in order with 0 lost items.");
    }
}
