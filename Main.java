import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// Represents a customer with a ticket number and transaction time
class Customer {
    private int ticketNumber;
    private int transactionTime;

    public Customer(int ticketNumber, int transactionTime) {
        this.ticketNumber = ticketNumber;
        this.transactionTime = transactionTime;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public int getTransactionTime() {
        return transactionTime;
    }
}

// Represents a server that can serve customers concurrently
class Server implements Runnable {
    private int serverNumber;
    private Customer currentCustomer;
    private boolean isAvailable;

    public Server(int serverNumber) {
        this.serverNumber = serverNumber;
        this.isAvailable = true;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void serveCustomer(Customer customer) {
        this.currentCustomer = customer;
        this.isAvailable = false;
    }

    @Override
    public void run() {
        // Display which customer the server is serving
        System.out.println("Server " + serverNumber + " takes customer " + currentCustomer.getTicketNumber());

        try {
            // Simulate the server working on the customer (sleep for the transaction time)
            Thread.sleep(currentCustomer.getTransactionTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Display that the server is done with the customer
        System.out.println("Server " + serverNumber + " is done with customer " + currentCustomer.getTicketNumber());

        // Mark the server as available
        this.isAvailable = true;
    }
}

public class Main {
    private static final int NUM_SERVERS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user to enter the name of the file with customer data
        System.out.print("Enter the name of the file with the customers line: ");
        String fileName = scanner.nextLine();

        // Read customers from the file
        List<Customer> customers = readCustomersFromFile(fileName);

        // Create a list of servers
        List<Server> servers = createServers();

        // Create a thread pool executor to manage threads
        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                NUM_SERVERS, NUM_SERVERS, 0L, TimeUnit.SECONDS, taskQueue);

        // Process customers and assign them to available servers
        int customerIndex = 0;
        while (customerIndex < customers.size() || !taskQueue.isEmpty()) {
            for (Server server : servers) {
                if (server.isAvailable() && customerIndex < customers.size()) {
                    // Assign the next available customer to the server and execute the server
                    Customer customer = customers.get(customerIndex);
                    server.serveCustomer(customer);
                    executor.execute(server);
                    customerIndex++;
                }
            }
        }

        // Shut down the executor when all tasks are done
        executor.shutdown();

        // Close the scanner
        scanner.close();
    }

    // Read customers from a file and return a list of Customer objects
    private static List<Customer> readCustomersFromFile(String fileName) {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                int ticketNumber = Integer.parseInt(parts[0]);
                int transactionTime = Integer.parseInt(parts[1]);
                customers.add(new Customer(ticketNumber, transactionTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Create a list of servers
    private static List<Server> createServers() {
        List<Server> servers = new ArrayList<>();
        for (int i = 1; i <= NUM_SERVERS; i++) {
            servers.add(new Server(i));
        }
        return servers;
    }
}
