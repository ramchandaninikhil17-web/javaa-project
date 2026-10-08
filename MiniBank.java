import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Requirement 5: Test each validator with one correct and one wrong input
        System.out.println("--- 1. Validation Tests ---");

        // Mobile validation
        String validMobile = "9876543210";
        String invalidMobile = "12345";
        System.out.println("Mobile '" + validMobile + "' is valid: " + Validator.isValidMobile(validMobile));
        System.out.println("Mobile '" + invalidMobile + "' is valid: " + Validator.isValidMobile(invalidMobile));

        // Email validation
        String validEmail = "alice@example.com";
        String invalidEmail = "alice_at_example.com";
        System.out.println("Email '" + validEmail + "' is valid: " + Validator.isValidEmail(validEmail));
        System.out.println("Email '" + invalidEmail + "' is valid: " + Validator.isValidEmail(invalidEmail));

        // PAN validation
        String validPan = "ABCDE1234F";
        String invalidPan = "12345ABCDE";
        System.out.println("PAN '" + validPan + "' is valid: " + Validator.isValidPan(validPan));
        System.out.println("PAN '" + invalidPan + "' is valid: " + Validator.isValidPan(invalidPan));

        // IFSC validation
        String validIfsc = "SBIN0001234";
        String invalidIfsc = "SBI0123";
        System.out.println("IFSC '" + validIfsc + "' is valid: " + Validator.isValidIfsc(validIfsc));
        System.out.println("IFSC '" + invalidIfsc + "' is valid: " + Validator.isValidIfsc(invalidIfsc));

        // Requirement 5: Parse a sample command and print its three parts
        System.out.println("\n--- 2. Command Parsing Test ---");
        String sampleLine = "DEPOSIT AC0001 500";
        System.out.println("Parsing input line: \"" + sampleLine + "\"");
        Command command = CommandParser.parse(sampleLine);

        System.out.println("Parsed Command parts:");
        System.out.println("  1. Transaction Type : " + command.type());
        System.out.println("  2. Account Number   : " + command.accountNumber());
        System.out.println("  3. Amount           : Rs." + command.amount());

        // Requirement 4 demonstration: StatementFormatter
        System.out.println("\n--- 3. Account Statement Test ---");
        Account account = new Account("Alice Smith", 5000);
        System.out.println("Initial Statement:");
        System.out.println(StatementFormatter.buildStatement(account));

        // Execute the parsed command on the account
        System.out.println("\nExecuting command on " + account.getAccountNumber() + "...");
        if (command.type() == TransactionType.DEPOSIT) {
            account.deposit(command.amount());
        }

        System.out.println("\nUpdated Statement after " + command.type() + ":");
        System.out.println(StatementFormatter.buildStatement(account));
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
