package service;

import exception.*;
import model.*;
import model.annotation.AnnotationValidator;
import test.MiniBankTest;
import util.StatementFormatter;
import util.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static util.StatementFormatter.buildStatement;

/**
 * Milestone 3 / Practical 14: MiniBank Final Capstone Application
 * 
 * Assembles all modular components into a deliverable console banking application:
 * - BankService wires Generic Repository<Account>, Validator, and custom exceptions.
 * - Interactive Practical 1 Menu connected to live banking operations.
 * - Concurrency control with TransactionProcessor over thread-safe collections.
 * - Persistence via Object Serialization (StatePersister) on startup & exit.
 * - Transaction logging & Java NIO reporting (ReportGenerator).
 * - Full JUnit test suite with @Test and reflective audit logging via @Audit.
 */
public class MiniBank {

    private static final Path DATA_FILE = Paths.get("data", "accounts.ser");
    private static final Path LOGS_FOLDER = Paths.get("logs");
    private static final Path REPORT_FILE = Paths.get("reports", "end_of_day_report.txt");

    public static void main(String[] args) {
        // Application header (Practical 1: BankInfo record)
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=================================================================");
        System.out.println("        " + bankInfo.name() + " - " + bankInfo.branch() + " (Final Capstone)");
        System.out.println("=================================================================\n");

        BankService bankService = new BankService();

        // Requirement 2: Load saved accounts on start-up (Practical 11)
        System.out.println("[Startup] Loading persisted accounts from disk...");
        int loadedCount = bankService.loadAccounts(DATA_FILE);

        // If no saved file exists yet, seed initial default accounts
        if (loadedCount == 0) {
            System.out.println("[Startup] No saved state found. Initializing seed accounts...");
            seedInitialAccounts(bankService);
            bankService.saveAccounts(DATA_FILE);
        } else {
            System.out.println("[Startup] System restored with " + loadedCount + " active accounts.\n");
        }

        // CLI Flag handling: --test runs JUnit tests; --demo runs complete capstone showcase
        if (args.length > 0) {
            if ("--test".equalsIgnoreCase(args[0])) {
                System.out.println("\nExecuting MiniBank automated JUnit test suite...\n");
                MiniBankTest.main(new String[0]);
                return;
            } else if ("--demo".equalsIgnoreCase(args[0])) {
                System.out.println("\nExecuting MiniBank full capstone milestone verification...\n");
                runFullDemonstration(bankService);
                bankService.saveAccounts(DATA_FILE);
                return;
            }
        }

        // Standard Interactive Execution: Connect Practical 1 Menu to live BankService
        try (Scanner scanner = new Scanner(System.in)) {
            runMenu(scanner, bankService);
        } catch (Exception e) {
            System.out.println("MiniBank session concluded: " + e.getMessage());
        } finally {
            // Requirement 2: Always save accounts on exit (Practical 11)
            System.out.println("\n[Shutdown] Saving account state to disk before exiting...");
            bankService.saveAccounts(DATA_FILE);
            System.out.println("[Shutdown] Data successfully persisted. Goodbye!");
        }
    }

    /**
     * Seeds initial sample accounts into the bank for first-time runs.
     */
    private static void seedInitialAccounts(BankService service) {
        try {
            Account a1 = new SavingsAccount("Alice Smith", 5000, 1000);
            Account a2 = new CurrentAccount("Bob Jones", 2000, 5000);
            Account a3 = new FixedDepositAccount("Charlie Brown", 50000);

            service.openAccount(a1);
            service.openAccount(a2);
            service.openAccount(a3);
            System.out.println("  -> Seeded 3 default accounts (Alice, Bob, Charlie).");
        } catch (InvalidAmountException e) {
            System.err.println("Seed account error: " + e.getMessage());
        }
    }

    /**
     * Requirement 2: Connect the Practical 1 menu so each option calls the matching
     * BankService method; load saved accounts on start-up and save them on exit.
     */
    public static void runMenu(Scanner scanner, BankService service) {
        boolean running = true;

        while (running) {
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("                       MINIBANK MAIN MENU                        ");
            System.out.println("-----------------------------------------------------------------");
            System.out.println("  1. Open Account");
            System.out.println("  2. Deposit Funds");
            System.out.println("  3. Withdraw Funds");
            System.out.println("  4. Transfer Funds (Deadlock-Free)");
            System.out.println("  5. View Account Statement");
            System.out.println("  6. List All Accounts");
            System.out.println("  7. Safe Batch Processing (TransactionProcessor Thread Pool)");
            System.out.println("  8. End-of-Day Summary Report (Java NIO)");
            System.out.println("  9. Exit & Save State");
            System.out.println("-----------------------------------------------------------------");
            System.out.print("Enter your choice (1-9): ");

            if (!scanner.hasNextInt()) {
                if (!scanner.hasNext()) {
                    // Reached EOF (non-interactive mode)
                    break;
                }
                System.out.println("Invalid input. Please enter a number between 1 and 9.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume trailing newline

            MenuOption selectedOption = switch (choice) {
                case 1 -> MenuOption.OPEN_ACCOUNT;
                case 2 -> MenuOption.DEPOSIT;
                case 3 -> MenuOption.WITHDRAW;
                case 4 -> MenuOption.TRANSFER;
                case 5 -> MenuOption.STATEMENT;
                case 6 -> MenuOption.LIST_ACCOUNTS;
                case 7 -> MenuOption.BATCH_PROCESS;
                case 8 -> MenuOption.REPORT;
                case 9 -> MenuOption.EXIT;
                default -> null;
            };

            if (selectedOption == null) {
                System.out.println("Invalid choice. Please choose a valid number from 1 to 9.");
                continue;
            }

            switch (selectedOption) {
                case OPEN_ACCOUNT -> handleOpenAccount(scanner, service);
                case DEPOSIT -> handleDeposit(scanner, service);
                case WITHDRAW -> handleWithdraw(scanner, service);
                case TRANSFER -> handleTransfer(scanner, service);
                case STATEMENT -> handleStatement(scanner, service);
                case LIST_ACCOUNTS -> handleListAccounts(service);
                case BATCH_PROCESS -> handleBatchProcess(service);
                case REPORT -> handleReport(service);
                case EXIT -> {
                    System.out.println("\nExiting MiniBank. Thank you, goodbye!");
                    running = false;
                }
            }
        }
    }

    private static void handleOpenAccount(Scanner scanner, BankService service) {
        System.out.println("\n--- [Option 1] Open New Account ---");
        System.out.println("Select Account Type:");
        System.out.println("  1. Savings Account (Interest: 4.0%, Min Balance)");
        System.out.println("  2. Current Account (Interest: 0.0%, Overdraft Limit)");
        System.out.println("  3. Fixed Deposit Account (Interest: 7.0%, Locked)");
        System.out.print("Choose type (1-3): ");

        int typeChoice = 1;
        if (scanner.hasNextInt()) {
            typeChoice = scanner.nextInt();
        }
        scanner.nextLine();

        String type = switch (typeChoice) {
            case 2 -> "CURRENT";
            case 3 -> "FD";
            default -> "SAVINGS";
        };

        System.out.print("Enter Account Holder Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Error: Account holder name cannot be blank.");
            return;
        }

        System.out.print("Enter Initial Deposit Amount (Rs.): ");
        if (!scanner.hasNextLong()) {
            System.out.println("Error: Invalid numeric amount.");
            scanner.nextLine();
            return;
        }
        long initialBalance = scanner.nextLong();
        scanner.nextLine();

        long extraParam = 0;
        if ("SAVINGS".equals(type)) {
            System.out.print("Enter Minimum Required Balance [Default: 1000]: ");
            if (scanner.hasNextLong()) {
                extraParam = scanner.nextLong();
            }
            scanner.nextLine();
        } else if ("CURRENT".equals(type)) {
            System.out.print("Enter Overdraft Limit [Default: 5000]: ");
            if (scanner.hasNextLong()) {
                extraParam = scanner.nextLong();
            }
            scanner.nextLine();
        }

        try {
            Account newAcc = service.openAccount(type, name, initialBalance, extraParam);
            System.out.println("\n>>> Account Successfully Opened!");
            System.out.println("    Account Number : " + newAcc.getAccountNumber());
            System.out.println("    Holder Name    : " + newAcc.getOwnerName());
            System.out.println("    Account Type   : " + newAcc.getClass().getSimpleName());
            System.out.println("    Opening Balance: Rs." + newAcc.getBalance());
        } catch (InvalidAmountException e) {
            System.out.println("Failed to open account: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error while opening account: " + e.getMessage());
        }
    }

    private static void handleDeposit(Scanner scanner, BankService service) {
        System.out.println("\n--- [Option 2] Deposit Funds ---");
        System.out.print("Enter Account Number (e.g., AC0001): ");
        String accNum = scanner.nextLine().trim();

        System.out.print("Enter Deposit Amount (Rs.): ");
        if (!scanner.hasNextLong()) {
            System.out.println("Error: Amount must be a valid number.");
            scanner.nextLine();
            return;
        }
        long amount = scanner.nextLong();
        scanner.nextLine();

        try {
            service.deposit(accNum, amount);
            Account acc = service.findAccount(accNum);
            System.out.println("\n>>> Deposit Successful!");
            System.out.println("    Account Number: " + acc.getAccountNumber());
            System.out.println("    Amount Added  : Rs." + amount);
            System.out.println("    New Balance   : Rs." + acc.getBalance());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid Amount: " + e.getMessage());
        }
    }

    private static void handleWithdraw(Scanner scanner, BankService service) {
        System.out.println("\n--- [Option 3] Withdraw Funds ---");
        System.out.print("Enter Account Number (e.g., AC0001): ");
        String accNum = scanner.nextLine().trim();

        System.out.print("Enter Withdrawal Amount (Rs.): ");
        if (!scanner.hasNextLong()) {
            System.out.println("Error: Amount must be a valid number.");
            scanner.nextLine();
            return;
        }
        long amount = scanner.nextLong();
        scanner.nextLine();

        try {
            service.withdraw(accNum, amount);
            Account acc = service.findAccount(accNum);
            System.out.println("\n>>> Withdrawal Successful!");
            System.out.println("    Account Number: " + acc.getAccountNumber());
            System.out.println("    Amount Debited: Rs." + amount);
            System.out.println("    New Balance   : Rs." + acc.getBalance());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Insufficient Funds: " + e.getMessage());
            if (e.getShortfall() > 0) {
                System.out.println("Shortfall Amount : Rs." + e.getShortfall() + " needed to complete this withdrawal.");
            }
        } catch (InvalidAmountException e) {
            System.out.println("Invalid Amount: " + e.getMessage());
        }
    }

    private static void handleTransfer(Scanner scanner, BankService service) {
        System.out.println("\n--- [Option 4] Transfer Funds (Deadlock-Free) ---");
        System.out.print("Enter Source Account Number (From): ");
        String fromAcc = scanner.nextLine().trim();

        System.out.print("Enter Destination Account Number (To): ");
        String toAcc = scanner.nextLine().trim();

        System.out.print("Enter Transfer Amount (Rs.): ");
        if (!scanner.hasNextLong()) {
            System.out.println("Error: Amount must be a valid number.");
            scanner.nextLine();
            return;
        }
        long amount = scanner.nextLong();
        scanner.nextLine();

        try {
            service.transfer(fromAcc, toAcc, amount);
            Account src = service.findAccount(fromAcc);
            Account dst = service.findAccount(toAcc);
            System.out.println("\n>>> Transfer Successful!");
            System.out.println("    Transferred Rs." + amount + " from " + fromAcc + " to " + toAcc);
            System.out.println("    Source Balance     : Rs." + src.getBalance());
            System.out.println("    Destination Balance: Rs." + dst.getBalance());
        } catch (BankException e) {
            System.out.println("Transfer Failed: " + e.getMessage());
        }
    }

    private static void handleStatement(Scanner scanner, BankService service) {
        System.out.println("\n--- [Option 5] View Account Statement ---");
        System.out.print("Enter Account Number (e.g., AC0001): ");
        String accNum = scanner.nextLine().trim();

        try {
            String stmt = service.statement(accNum);
            System.out.println("\n" + stmt);
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void handleListAccounts(BankService service) {
        System.out.println("\n--- [Option 6] List All Accounts ---");
        List<Account> accounts = service.listAccounts();

        if (accounts.isEmpty()) {
            System.out.println("  No accounts found in the system.");
            return;
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.printf("  %-10s | %-18s | %-15s | %s%n", "Account No", "Owner Name", "Balance", "Type");
        System.out.println("-----------------------------------------------------------------");
        for (Account acc : accounts) {
            System.out.printf("  %-10s | %-18s | Rs.%-12d | %s%n",
                    acc.getAccountNumber(), acc.getOwnerName(), acc.getBalance(), acc.getClass().getSimpleName());
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Total Accounts: " + accounts.size());
    }

    private static void handleBatchProcess(BankService service) {
        System.out.println("\n--- [Option 7] Safe Batch Processing (TransactionProcessor Thread Pool) ---");
        List<Account> accounts = service.listAccounts();
        if (accounts.size() < 2) {
            System.out.println("Need at least 2 accounts to run batch transactions.");
            return;
        }

        Account a1 = accounts.get(0);
        Account a2 = accounts.get(1);

        System.out.println("Preparing batch of 6 concurrent transaction tasks across 4 worker threads...");
        System.out.println("  Target 1: " + a1.getAccountNumber() + " (Current Balance: Rs." + a1.getBalance() + ")");
        System.out.println("  Target 2: " + a2.getAccountNumber() + " (Current Balance: Rs." + a2.getBalance() + ")");

        List<TransactionTask> batch = new ArrayList<>();
        batch.add(new TransactionTask(1, a1, TransactionTask.Type.DEPOSIT, 1500));
        batch.add(new TransactionTask(2, a2, TransactionTask.Type.DEPOSIT, 2000));
        batch.add(new TransactionTask(3, a1, TransactionTask.Type.WITHDRAW, 500));
        batch.add(new TransactionTask(4, a2, TransactionTask.Type.WITHDRAW, 1000));
        batch.add(new TransactionTask(5, a1, TransactionTask.Type.DEPOSIT, 3000));
        batch.add(new TransactionTask(6, a2, TransactionTask.Type.DEPOSIT, 2500));

        // Process batch using TransactionProcessor
        service.processBatch(batch);

        System.out.println("\n>>> Batch processing finished!");
        System.out.println("    " + a1.getAccountNumber() + " Updated Balance: Rs." + a1.getBalance());
        System.out.println("    " + a2.getAccountNumber() + " Updated Balance: Rs." + a2.getBalance());
    }

    private static void handleReport(BankService service) {
        System.out.println("\n--- [Option 8] End-of-Day Summary Report (Java NIO) ---");
        try {
            String report = service.generateEndOfDayReport();
            System.out.println("\n" + report);
        } catch (IOException e) {
            System.out.println("Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Executes the comprehensive capstone milestone demonstration covering
     * Practicals 1 to 14.
     */
    private static void runFullDemonstration(BankService bankService) {
        System.out.println("=================================================================");
        System.out.println("       MINIBANK COMPREHENSIVE CAPSTONE LAB DEMONSTRATION         ");
        System.out.println("=================================================================\n");

        // 1. List accounts
        System.out.println("--- 1. Account Listing via BankService & Generic Repository ---");
        handleListAccounts(bankService);

        // 2. Perform live operations
        System.out.println("\n--- 2. Live Banking Transactions (Deposit & Withdraw) ---");
        List<Account> accounts = bankService.listAccounts();
        if (!accounts.isEmpty()) {
            Account primary = accounts.get(0);
            try {
                System.out.println("Executing Deposit of Rs.2,500 into " + primary.getAccountNumber() + "...");
                bankService.deposit(primary.getAccountNumber(), 2500);

                System.out.println("Executing Withdrawal of Rs.1,000 from " + primary.getAccountNumber() + "...");
                bankService.withdraw(primary.getAccountNumber(), 1000);
            } catch (BankException e) {
                System.err.println("Transaction failed: " + e.getMessage());
            }

            // 3. Statement generation
            System.out.println("\n--- 3. Formatted Statement Generation ---");
            try {
                System.out.println(bankService.statement(primary.getAccountNumber()));
            } catch (AccountNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        // 4. Deadlock-free Transfer
        if (accounts.size() >= 2) {
            System.out.println("\n--- 4. Deadlock-Free Inter-Account Transfer ---");
            Account acc1 = accounts.get(0);
            Account acc2 = accounts.get(1);
            try {
                System.out.println("Transferring Rs.1,500 from " + acc1.getAccountNumber() + " to " + acc2.getAccountNumber() + "...");
                bankService.transfer(acc1.getAccountNumber(), acc2.getAccountNumber(), 1500);
            } catch (BankException e) {
                System.err.println("Transfer error: " + e.getMessage());
            }
        }

        // 5. Safe Batch Processing
        System.out.println("\n--- 5. Concurrent Safe Batch Processing ---");
        handleBatchProcess(bankService);

        // 6. End of day report
        System.out.println("\n--- 6. Java NIO End-of-Day Report ---");
        handleReport(bankService);

        // 7. Unit Tests
        System.out.println("\n--- 7. Automated JUnit Test Suite Execution ---");
        MiniBankTest.main(new String[0]);
    }
}
