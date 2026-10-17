package model;

public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private long overdraftLimit;

    public CurrentAccount(String ownerName, long balance, long overdraftLimit) {
        super(ownerName, balance);
        this.overdraftLimit = overdraftLimit;
    }

    public CurrentAccount(String ownerName, long balance) {
        this(ownerName, balance, 5000);
    }

    public long getOverdraftLimit() {
        return overdraftLimit;
    }

    @Override
    public double interestRate() {
        return 0.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (getBalance() - amount) >= -overdraftLimit;
    }

    @Override
    public long getShortfall(long amount) {
        long maxWithdrawable = Math.max(0, getBalance() + overdraftLimit);
        return amount > maxWithdrawable ? amount - maxWithdrawable : 0;
    }

    @Override
    public String toString() {
        return super.toString() + " [CurrentAccount - Overdraft Limit: Rs." + overdraftLimit + ", Interest: " + interestRate() + "%]";
    }
}
