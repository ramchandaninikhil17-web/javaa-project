import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Record object printed as application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Create three Customer objects
        Customer customer1 = new Customer("Alice Smith", "alice@example.com", "9876543210");
        Customer customer2 = new Customer("Bob Jones", "bob@example.com", "9876543211");
        Customer customer3 = new Customer("Charlie Brown", "charlie@example.com", "9876543212");

        System.out.println("--- Registered Customers ---");
        System.out.println(customer1.getCustomerId() + ": " + customer1.getName() + " | " + customer1.getEmail() + " | " + customer1.getMobile());
        System.out.println(customer2.getCustomerId() + ": " + customer2.getName() + " | " + customer2.getEmail() + " | " + customer2.getMobile());
        System.out.println(customer3.getCustomerId() + ": " + customer3.getName() + " | " + customer3.getEmail() + " | " + customer3.getMobile());

        // Requirement 5: Create three Account objects inside an Account[] array
        Account[] accounts = new Account[3];
        accounts[0] = new Account(customer1.getName(), 5000);
        accounts[1] = new Account(customer2.getName(), 2000);
        accounts[2] = new Account(customer3.getName()); // Uses constructor chaining this(ownerName, 0)

        System.out.println("\n--- Initial Account Details ---");
        for (Account account : accounts) {
            System.out.println("Account Number: " + account.getAccountNumber() +
                    " | Owner: " + account.getOwnerName() +
                    " | Balance: Rs." + account.getBalance() +
                    " | Active: " + account.isActive());
        }

        // Perform deposits and withdrawals
        System.out.println("\n--- Performing Transactions ---");

        System.out.println("1. Depositing Rs.1500 to " + accounts[0].getAccountNumber() + " (" + accounts[0].getOwnerName() + ")...");
        accounts[0].deposit(1500);

        System.out.println("2. Withdrawing Rs.1000 from " + accounts[1].getAccountNumber() + " (" + accounts[1].getOwnerName() + ")...");
        boolean bobSuccess = accounts[1].withdraw(1000);
        System.out.println("   Withdrawal status: " + (bobSuccess ? "Successful" : "Failed"));

        System.out.println("3. Withdrawing Rs.500 from " + accounts[2].getAccountNumber() + " (" + accounts[2].getOwnerName() + ", current balance: 0)...");
        boolean charlieSuccess = accounts[2].withdraw(500);
        System.out.println("   Withdrawal status: " + (charlieSuccess ? "Successful" : "Failed (Insufficient balance)"));

        System.out.println("4. Depositing Rs.3000 to " + accounts[2].getAccountNumber() + " (" + accounts[2].getOwnerName() + ")...");
        accounts[2].deposit(3000);

        // Print each final balance
        System.out.println("\n--- Final Account Balances ---");
        for (Account account : accounts) {
            System.out.println("Account: " + account.getAccountNumber() +
                    " | Owner: " + account.getOwnerName() +
                    " | Final Balance: Rs." + account.getBalance());
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
