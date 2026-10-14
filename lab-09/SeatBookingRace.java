package lab09;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Practical 9 - Part A (Problem 2): Seat Booking Race
 * 
 * Demonstrates a 'check-then-act' concurrency hazard where 10 passenger threads
 * contend to book 5 available seats. Without synchronization, threads read a stale
 * seatsLeft value and cause an oversell (> 5 booked). With synchronized book(),
 * exactly 5 seats are booked.
 */
public class SeatBookingRace {

    // Unsynchronized booking system
    static class UnsynchronizedBookingSystem {
        private int seatsLeft = 5;
        private final List<String> successfulBookings = Collections.synchronizedList(new ArrayList<>());

        public boolean bookSeat(String passenger) {
            // Check-then-act race condition
            if (seatsLeft > 0) {
                try {
                    // Artificial delay to widen the race window between check and update
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}

                seatsLeft--;
                successfulBookings.add(passenger);
                return true;
            }
            return false;
        }

        public int getSeatsLeft() {
            return seatsLeft;
        }

        public List<String> getSuccessfulBookings() {
            return successfulBookings;
        }
    }

    // Synchronized booking system
    static class SynchronizedBookingSystem {
        private int seatsLeft = 5;
        private final List<String> successfulBookings = new ArrayList<>();

        public synchronized boolean bookSeat(String passenger) {
            // Atomic critical section: check and action are bound together
            if (seatsLeft > 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}

                seatsLeft--;
                successfulBookings.add(passenger);
                return true;
            }
            return false;
        }

        public synchronized int getSeatsLeft() {
            return seatsLeft;
        }

        public synchronized List<String> getSuccessfulBookings() {
            return new ArrayList<>(successfulBookings);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int totalPassengers = 10;
        final int initialSeats = 5;

        System.out.println("==================================================");
        System.out.println(" Practical 9 - Part A (2): Seat Booking Race      ");
        System.out.println("==================================================");
        System.out.println("Total Available Seats : " + initialSeats);
        System.out.println("Contending Passengers : " + totalPassengers + "\n");

        // --- Test 1: Unsynchronized Booking ---
        System.out.println("--- Test 1: Booking WITHOUT Synchronization ---");
        UnsynchronizedBookingSystem unsafeSystem = new UnsynchronizedBookingSystem();
        Thread[] unsafeThreads = new Thread[totalPassengers];

        for (int i = 0; i < totalPassengers; i++) {
            final String passengerName = "Passenger-" + (i + 1);
            unsafeThreads[i] = new Thread(() -> {
                boolean success = unsafeSystem.bookSeat(passengerName);
                if (success) {
                    System.out.println("  [UNSAFE] " + passengerName + " SUCCESSFULLY booked a seat!");
                } else {
                    System.out.println("  [UNSAFE] " + passengerName + " FAILED (Sold out).");
                }
            }, passengerName);
            unsafeThreads[i].start();
        }

        for (Thread t : unsafeThreads) {
            t.join();
        }

        int unsafeBookedCount = unsafeSystem.getSuccessfulBookings().size();
        System.out.println("\nUnsafe Total Booked Seats : " + unsafeBookedCount + " / " + initialSeats);
        System.out.println("Remaining Seats in System : " + unsafeSystem.getSeatsLeft());
        if (unsafeBookedCount > initialSeats) {
            System.out.println("Result: CRITICAL OVERSELL DETECTED! " + (unsafeBookedCount - initialSeats)
                    + " extra seat(s) sold due to uncoordinated access.");
        } else {
            System.out.println("Result: Race condition did not trigger this run.");
        }

        // --- Test 2: Synchronized Booking ---
        System.out.println("\n--- Test 2: Booking WITH Synchronized book() ---");
        SynchronizedBookingSystem safeSystem = new SynchronizedBookingSystem();
        Thread[] safeThreads = new Thread[totalPassengers];

        for (int i = 0; i < totalPassengers; i++) {
            final String passengerName = "Passenger-" + (i + 1);
            safeThreads[i] = new Thread(() -> {
                boolean success = safeSystem.bookSeat(passengerName);
                if (success) {
                    System.out.println("  [SAFE]   " + passengerName + " SUCCESSFULLY booked a seat!");
                } else {
                    System.out.println("  [SAFE]   " + passengerName + " FAILED (Sold out).");
                }
            }, passengerName);
            safeThreads[i].start();
        }

        for (Thread t : safeThreads) {
            t.join();
        }

        int safeBookedCount = safeSystem.getSuccessfulBookings().size();
        System.out.println("\nSafe Total Booked Seats : " + safeBookedCount + " / " + initialSeats);
        System.out.println("Remaining Seats in System: " + safeSystem.getSeatsLeft());
        System.out.println("Result: EXACTLY 5 seats booked. Thread safety prevented overselling.");
    }
}
