package service;

import model.*;
import util.Validator;
import static util.StatementFormatter.buildStatement; // Requirement 5: Static import

import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Requirement 1 & 2: Accounts implementing Transactable and InterestBearing
        System.out.println("--- 1. Accounts & Capabilities (Transactable & InterestBearing) ---");
        Account[] accounts = new Account[3];
        accounts[0] = new SavingsAccount("Alice Smith", 5000, 1000);
        accounts[1] = new CurrentAccount("Bob Jones", 2000, 5000);
        accounts[2] = new FixedDepositAccount("Charlie Brown", 50000);

        for (Account acc : accounts) {
            System.out.println(acc);
            System.out.printf("  Interest Rate: %.1f%% | Yearly Interest: Rs.%.2f%n",
                    acc.interestRate(), acc.yearlyInterest()); // Default method from InterestBearing

            // Requirement 4: Checking marker interface Premium
            if (acc instanceof Premium) {
                System.out.println("  -> [Marker Interface]: This account qualifies as PREMIUM.");
            }
        }

        // Requirement 3: Use WithdrawRule two ways (Anonymous Class, then Lambda)
        System.out.println("\n--- 2. Functional Interface (WithdrawRule) ---");

        // Way 1: Anonymous class (e.g., maximum transaction limit of Rs.3000)
        WithdrawRule maxLimitRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount <= 3000;
            }
        };

        // Way 2: Lambda expression (e.g., balance after withdrawal must remain at least Rs.500)
        WithdrawRule minReserveRule = (account, amount) -> (account.getBalance() - amount) >= 500;

        Account aliceAcc = accounts[0];
        long testAmount1 = 2500;
        long testAmount2 = 4500;

        System.out.println("Testing Alice's account (Balance: Rs." + aliceAcc.getBalance() + "):");
        System.out.println("  [Anonymous Class] Max limit Rs.3000 for Rs." + testAmount1 + ": " +
                maxLimitRule.allow(aliceAcc, testAmount1));
        System.out.println("  [Anonymous Class] Max limit Rs.3000 for Rs." + testAmount2 + ": " +
                maxLimitRule.allow(aliceAcc, testAmount2));

        System.out.println("  [Lambda Expression] Min reserve Rs.500 for Rs." + testAmount1 + ": " +
                minReserveRule.allow(aliceAcc, testAmount1));
        System.out.println("  [Lambda Expression] Min reserve Rs.500 for Rs.4800: " +
                minReserveRule.allow(aliceAcc, 4800));

        // Requirement 1: Transactable interface in action
        System.out.println("\n--- 3. Transactable Interface Operations ---");
        Transactable t = accounts[0];
        t.deposit(1500);
        System.out.println("Deposited Rs.1500 into " + accounts[0].getAccountNumber() + " -> New Balance: Rs." + accounts[0].getBalance());
        boolean success = t.withdraw(2000);
        System.out.println("Withdrew Rs.2000 from " + accounts[0].getAccountNumber() + " -> Status: " + success + ", Balance: Rs." + accounts[0].getBalance());

        // Command parsing and Statement formatting (using static import buildStatement)
        System.out.println("\n--- 4. Command Parsing & Statement Generation ---");
        Command command = CommandParser.parse("DEPOSIT AC0001 1000");
        System.out.println("Parsed Command: " + command.type() + " Rs." + command.amount() + " for " + command.accountNumber());

        // Calling statically imported buildStatement
        System.out.println("\n" + buildStatement(accounts[0]));
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
