import java.util.Scanner;

public class MiniBank {
    public static void main(String[] args) {
        // Application header
        BankInfo bankInfo = new BankInfo("MiniBank", "Downtown Branch");
        System.out.println("=========================================");
        System.out.println(bankInfo);
        System.out.println("=========================================\n");

        // Requirement 6: Place objects of the three types in an Account[] array
        System.out.println("--- Creating Different Account Types (Polymorphism) ---");
        Account[] accounts = new Account[3];
        accounts[0] = new SavingsAccount("Alice Smith", 5000, 1000);
        accounts[1] = new CurrentAccount("Bob Jones", 2000, 5000);
        accounts[2] = new FixedDepositAccount("Charlie Brown", 50000);

        // Requirement 6: Loop through array and call interestRate() on each
        System.out.println("\n--- Account Details & Polymorphic Interest Rates ---");
        for (Account acc : accounts) {
            System.out.println(acc.toString());
            System.out.println("  -> Account Type: " + acc.getClass().getSimpleName() + 
                               " | Annual Interest Rate: " + acc.interestRate() + "%");

            // Requirement 6: Pattern instanceof check to handle types specially
            if (acc instanceof SavingsAccount sa) {
                System.out.println("     [Special Rule]: Requires maintaining minimum balance of Rs." + sa.getMinBalance());
            } else if (acc instanceof CurrentAccount ca) {
                System.out.println("     [Special Rule]: Overdraft allowed up to Rs." + ca.getOverdraftLimit());
            } else if (acc instanceof FixedDepositAccount fda) {
                System.out.println("     [Special Rule]: Fixed deposit is locked; no withdrawals permitted.");
            }
        }

        // Demonstration of polymorphic withdrawal rules
        System.out.println("\n--- Testing Withdrawal Rules for Each Account Type ---");

        // 1. SavingsAccount: can withdraw as long as balance >= minBalance (1000)
        System.out.println("\n1. Testing SavingsAccount (Balance: Rs." + accounts[0].getBalance() + ", MinBalance: Rs.1000):");
        System.out.println("   Withdrawing Rs.3500...");
        boolean saSuccess1 = accounts[0].withdraw(3500);
        System.out.println("   Status: " + (saSuccess1 ? "Success" : "Failed") + " | Balance now: Rs." + accounts[0].getBalance());

        System.out.println("   Withdrawing another Rs.1000 (would breach Rs.1000 min balance)...");
        boolean saSuccess2 = accounts[0].withdraw(1000);
        System.out.println("   Status: " + (saSuccess2 ? "Success" : "Failed (Balance cannot fall below minBalance)") + 
                           " | Balance now: Rs." + accounts[0].getBalance());

        // 2. CurrentAccount: can withdraw up to -overdraftLimit (-5000)
        System.out.println("\n2. Testing CurrentAccount (Balance: Rs." + accounts[1].getBalance() + ", OverdraftLimit: Rs.5000):");
        System.out.println("   Withdrawing Rs.4000 (exceeding balance into overdraft)...");
        boolean caSuccess1 = accounts[1].withdraw(4000);
        System.out.println("   Status: " + (caSuccess1 ? "Success" : "Failed") + " | Balance now: Rs." + accounts[1].getBalance());

        System.out.println("   Withdrawing Rs.4000 more (would exceed overdraft limit of -Rs.5000)...");
        boolean caSuccess2 = accounts[1].withdraw(4000);
        System.out.println("   Status: " + (caSuccess2 ? "Success" : "Failed (Overdraft limit exceeded)") + 
                           " | Balance now: Rs." + accounts[1].getBalance());

        // 3. FixedDepositAccount: cannot withdraw (locked)
        System.out.println("\n3. Testing FixedDepositAccount (Balance: Rs." + accounts[2].getBalance() + "):");
        System.out.println("   Attempting to withdraw Rs.5000...");
        boolean fdaSuccess = accounts[2].withdraw(5000);
        System.out.println("   Status: " + (fdaSuccess ? "Success" : "Failed (Fixed deposit is locked)") + 
                           " | Balance now: Rs." + accounts[2].getBalance());

        // Command parsing and Statement formatting demonstration
        System.out.println("\n--- Command Parsing & Statement Formatting ---");
        String sampleCommand = "DEPOSIT " + accounts[0].getAccountNumber() + " 2000";
        Command cmd = CommandParser.parse(sampleCommand);
        System.out.println("Parsed Command: " + cmd.type() + " Rs." + cmd.amount() + " into " + cmd.accountNumber());

        if (cmd.type() == TransactionType.DEPOSIT) {
            accounts[0].deposit(cmd.amount());
        }

        System.out.println("\nGenerated Statement:");
        System.out.println(StatementFormatter.buildStatement(accounts[0]));
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
