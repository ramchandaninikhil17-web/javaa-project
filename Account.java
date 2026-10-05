public class Account {
    private static long accountCounter = 1;

    private final String accountNumber;
    private String ownerName;
    private long balance;
    private boolean active;

    private static String generateAccountNumber() {
        return String.format("AC%04d", accountCounter++);
    }

    // Constructor taking ownerName and opening balance
    public Account(String ownerName, long balance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = balance;
        this.active = true;
    }

    // Constructor taking only ownerName, calls the first constructor with 0 balance
    public Account(String ownerName) {
        this(ownerName, 0);
    }

    // Adds amount to balance
    public void deposit(long amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    // Subtracts amount if sufficient balance, returns true; otherwise returns false
    public boolean withdraw(long amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // Getter methods
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    public static void main(String[] args) {
        MiniBank.main(args);
    }
}
