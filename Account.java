import java.util.Objects;

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

    // Requirement 1: Override toString() with readable line containing accountNumber, ownerName, balance
    @Override
    public String toString() {
        return "Account [Account Number: " + accountNumber + ", Owner: " + ownerName + ", Balance: Rs." + balance + "]";
    }

    // Requirement 2: Override equals(Object o) based on accountNumber
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    // Requirement 2: Override hashCode() consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hashCode(accountNumber);
    }

    public static void main(String[] args) {
        MiniBank.main(args);
    }
}
