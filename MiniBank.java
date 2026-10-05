import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Record object printed as application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Requirement 3: Customer with nested Address class
        Customer.Address addr1 = new Customer.Address("12 MG Road", "Bengaluru", "560001");
        Customer.Address addr2 = new Customer.Address("45 Park Street", "Kolkata", "700016");

        Customer customer1 = new Customer("Alice Smith", "alice@example.com", "9876543210", addr1);
        Customer customer2 = new Customer("Bob Jones", "bob@example.com", "9876543211", addr2);
        Customer customer3 = new Customer("Charlie Brown", "charlie@example.com", "9876543212");

        System.out.println("--- Registered Customers ---");
        System.out.println(customer1);
        System.out.println(customer2);
        System.out.println(customer3);

        // Requirement 4: Demonstration of clone() on Customer
        System.out.println("\n--- Customer clone() Demonstration ---");
        Customer clonedCustomer1 = customer1.clone();
        System.out.println("Original Customer: " + customer1);
        System.out.println("Cloned Customer:   " + clonedCustomer1);
        System.out.println("Is clone a separate object instance? " + (customer1 != clonedCustomer1));
        System.out.println("Are customer IDs identical? " + customer1.getCustomerId().equals(clonedCustomer1.getCustomerId()));

        // Create three Account objects in an Account[] array
        Account[] accounts = new Account[3];
        accounts[0] = new Account(customer1.getName(), 5000);
        accounts[1] = new Account(customer2.getName(), 2000);
        accounts[2] = new Account(customer3.getName()); // Uses this(ownerName, 0)

        // Requirement 5: Print accounts using toString()
        System.out.println("\n--- Accounts Printed using toString() ---");
        for (Account account : accounts) {
            System.out.println(account.toString());
        }

        // Transactions demonstration
        System.out.println("\n--- Performing Transactions ---");
        accounts[0].deposit(1500);
        System.out.println("Deposited Rs.1500 into " + accounts[0].getAccountNumber() + " -> " + accounts[0]);

        boolean bobSuccess = accounts[1].withdraw(1000);
        System.out.println("Withdrew Rs.1000 from " + accounts[1].getAccountNumber() + " (Success: " + bobSuccess + ") -> " + accounts[1]);

        boolean charlieSuccess = accounts[2].withdraw(500);
        System.out.println("Withdrew Rs.500 from " + accounts[2].getAccountNumber() + " (Success: " + charlieSuccess + " - Insufficient balance) -> " + accounts[2]);

        accounts[2].deposit(3000);
        System.out.println("Deposited Rs.3000 into " + accounts[2].getAccountNumber() + " -> " + accounts[2]);

        // Requirement 5: Compare two Account objects with equals()
        System.out.println("\n--- Comparing Accounts with equals() & hashCode() ---");
        System.out.println("accounts[0].equals(accounts[0]): " + accounts[0].equals(accounts[0]) + " (Same account)");
        System.out.println("accounts[0].equals(accounts[1]): " + accounts[0].equals(accounts[1]) + " (Different accounts)");
        System.out.println("accounts[0] hashCode: " + accounts[0].hashCode());
        System.out.println("accounts[1] hashCode: " + accounts[1].hashCode());

        // Requirement 5: Use instanceof to check an object's type
        System.out.println("\n--- Type Checking with instanceof ---");
        Object obj1 = accounts[0];
        Object obj2 = "Non-account string object";

        if (obj1 instanceof Account acc) {
            System.out.println("obj1 is an instance of Account: " + acc.getAccountNumber() + " (Owner: " + acc.getOwnerName() + ")");
        } else {
            System.out.println("obj1 is NOT an instance of Account");
        }

        if (obj2 instanceof Account) {
            System.out.println("obj2 is an instance of Account");
        } else {
            System.out.println("obj2 is NOT an instance of Account (Type: " + obj2.getClass().getSimpleName() + ")");
        }

        // Print final balances
        System.out.println("\n--- Final Account Balances ---");
        for (Account account : accounts) {
            System.out.println(account);
        }
    }

    // Interactive menu shell preserved from Practical 1
    public static void runMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- MiniBank Menu ---");
            System.out.println("1. Open Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();

            MenuOption selectedOption = switch (choice) {
                case 1 -> MenuOption.OPEN_ACCOUNT;
                case 2 -> MenuOption.DEPOSIT;
                case 3 -> MenuOption.WITHDRAW;
                case 4 -> MenuOption.TRANSFER;
                case 5 -> MenuOption.EXIT;
                default -> null;
            };

            if (selectedOption == null) {
                System.out.println("Invalid choice. Please choose a number from 1 to 5.");
                continue;
            }

            String placeholder = switch (selectedOption) {
                case OPEN_ACCOUNT -> "Open Account - to be implemented in a later lab.";
                case DEPOSIT -> "Deposit - to be implemented in a later lab.";
                case WITHDRAW -> "Withdraw - to be implemented in a later lab.";
                case TRANSFER -> "Transfer - to be implemented in a later lab.";
                case EXIT -> "Exiting MiniBank. Thank you, goodbye!";
            };

            System.out.println(placeholder);

            if (selectedOption == MenuOption.EXIT) {
                running = false;
            }
        }
    }
}
