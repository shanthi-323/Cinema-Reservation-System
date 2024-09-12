//Student ID: 20055810
//Name : Shanthi A/P Saravanan

import java.util.concurrent.locks.ReentrantLock;  // Importing ReentrantLock for concurrency control
import java.util.concurrent.ThreadLocalRandom;    // Importing ThreadLocalRandom for generating random numbers
import java.util.concurrent.atomic.AtomicInteger; // Importing AtomicInteger for thread-safe integer operations

class Cinema {
    private final int[][] seats;                  // 2D array to represent seats in the theatres
    private final ReentrantLock lock;             // Lock to ensure thread-safe access to the seats
    private final AtomicInteger seatsRemaining;   // AtomicInteger to keep track of remaining seats

    // Constructor to initialize the cinema with specified number of theatres and seats per theatre
    public Cinema(int numTheatres, int numSeatsPerTheatre) {
        seats = new int[numTheatres][numSeatsPerTheatre];
        lock = new ReentrantLock();
        seatsRemaining = new AtomicInteger(numTheatres * numSeatsPerTheatre);
    }

    // Method to reserve seats in a specified theatre
    public boolean reserve_Seats(int theatre, int[] seatsToReserve) {
        lock.lock();  // Acquiring the lock before accessing shared resources
        try {
            // Check if the seats are available
            for (int seat : seatsToReserve) {
                if (seats[theatre][seat] == 1) {
                    return false; // Seat already reserved
                }
            }
            // Reserve the seats
            for (int seat : seatsToReserve) {
                seats[theatre][seat] = 1;
            }
            seatsRemaining.addAndGet(-seatsToReserve.length);  // Decrement the count of remaining seats
            return true;
        } finally {
            lock.unlock();  // Releasing the lock after operations
        }
    }

    // Method to check if all seats are filled
    public boolean allSeatsFilled() {
        return seatsRemaining.get() == 0;
    }

    // Method to check if there are any unreserved seats
    public boolean has_UnreservedSeats() {
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                if (seats[i][j] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // Method to display the current state of the seats in each theatre
    public void display_Seats() {
        for (int i = 0; i < seats.length; i++) {
            System.out.print("Theatre " + (i + 1) + ": ");
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print(seats[i][j] + " ");
            }
            System.out.println();
        }
    }
}

class Customer extends Thread {
    private final Cinema cinema;  // Reference to the Cinema object
    private final int id;         // ID of the customer

    // Constructor to initialize the customer with a cinema reference and an ID
    public Customer(Cinema cinema, int id) {
        this.cinema = cinema;
        this.id = id;
    }

    @Override
    public void run() {
        while (cinema.has_UnreservedSeats()) {
            int theatre = ThreadLocalRandom.current().nextInt(0, 3);  // Randomly select a theatre
            int numSeats = ThreadLocalRandom.current().nextInt(1, 4); // Randomly select 1 to 3 seats
            int[] seatsToReserve = new int[numSeats];
            for (int i = 0; i < numSeats; i++) {
                seatsToReserve[i] = ThreadLocalRandom.current().nextInt(0, 20); // Randomly select seat numbers
            }

            boolean success = cinema.reserve_Seats(theatre, seatsToReserve);  // Attempt to reserve seats
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1001)); // Simulate delay before confirming reservation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Handle interruption
            }
            if (success) {
                System.out.println("Customer " + id + " successfully reserved seats in Theatre " + (theatre + 1));
            } else {
                System.out.println("Customer " + id + " failed to reserve seats in Theatre " + (theatre + 1));
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Cinema cinema = new Cinema(3, 20);  // Create a cinema with 3 theatres, each having 20 seats
        Customer[] customers = new Customer[110];  // Create an array of 110 customers
        for (int i = 0; i < customers.length; i++) {
            customers[i] = new Customer(cinema, i);  // Initialize each customer with the cinema reference and an ID
            customers[i].start();  // Start each customer thread
        }
        for (Customer customer : customers) {
            try {
                customer.join();  // Wait for each customer thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Handle interruption
            }
        }
        while (cinema.has_UnreservedSeats()) {
            for (int i = 0; i < customers.length; i++) {
                if (!customers[i].isAlive()) {
                    customers[i] = new Customer(cinema, i);  // Restart customer threads if necessary
                    customers[i].start();
                }
            }
            for (Customer customer : customers) {
                try {
                    customer.join();  // Wait for each customer thread to finish
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Handle interruption
                }
            }
        }
        cinema.display_Seats();  // Display the final state of the seats
    }
}
