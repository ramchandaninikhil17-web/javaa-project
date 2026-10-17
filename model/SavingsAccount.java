package model;

public class SavingsAccount extends Account implements Premium {
    private static final long serialVersionUID = 1L;
    private long minBalance;

    public SavingsAccount(String ownerName, long balance, long minBalance) {
        super(ownerName, balance);
        this.minBalance = minBalance;
    }

    public SavingsAccount(String ownerName, long balance) {
        this(ownerName, balance, 1000);
    }

    public long getMinBalance() {
        return minBalance;
    }

    @Override
    public double interestRate() {
        return 4.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return (getBalance() - amount) >= minBalance;
    }

    @Override
    public long getShortfall(long amount) {
        long maxWithdrawable = Math.max(0, getBalance() - minBalance);
        return amount > maxWithdrawable ? amount - maxWithdrawable : 0;
    }

    @Override
    public String toString() {
        return super.toString() + " [SavingsAccount - Min Balance: Rs." + minBalance + ", Interest: " + interestRate() + "%]";
    }
}
