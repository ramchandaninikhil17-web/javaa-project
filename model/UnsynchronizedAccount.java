package model;

import exception.InvalidAmountException;

/**
 * Concrete account type used to demonstrate the lost-update race condition
 * when deposit is NOT synchronized.
 */
public class UnsynchronizedAccount extends Account {

    public UnsynchronizedAccount(String ownerName, long balance) {
        super(ownerName, balance);
    }

    /**
     * Intentionally non-synchronized deposit to demonstrate race conditions.
     * Multiple threads reading and writing the balance simultaneously will
     * interleave and overwrite each other's increments.
     */
    @Override
    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero. Provided: Rs." + amount);
        }
        long current = super.getBalance();
        // Artificial short yield/sleep could widen the race, but direct read-modify-write
        // under high contention already causes significant lost updates.
        setBalanceDirectly(current + amount);
    }

    @Override
    public double interestRate() {
        return 0.0;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return getBalance() >= amount;
    }
}
