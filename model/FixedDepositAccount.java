package model;

public class FixedDepositAccount extends Account {
    public FixedDepositAccount(String ownerName, long balance) {
        super(ownerName, balance);
    }

    @Override
    public double interestRate() {
        return 7.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return false; // Deposit is locked
    }

    @Override
    public String toString() {
        return super.toString() + " [FixedDepositAccount - Locked Deposit, Interest: " + interestRate() + "%]";
    }
}
