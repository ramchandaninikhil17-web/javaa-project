package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Practical 9 - Supplementary Problem:
 * Lock-free, thread-safe Account implementation using AtomicLong.
 * 
 * Uses hardware-level Compare-And-Swap (CAS) instructions instead of
 * intrinsic monitor locks (synchronized).
 */
public class AtomicAccount extends Account {
    private static final long serialVersionUID = 1L;
    private final AtomicLong atomicBalance;

    public AtomicAccount(String ownerName, long initialBalance) {
        super(ownerName, initialBalance);
        this.atomicBalance = new AtomicLong(initialBalance);
    }

    @Override
    public void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero. Provided: Rs." + amount);
        }
        // Atomic lock-free fetch-and-add
        atomicBalance.addAndGet(amount);
    }

    @Override
    public void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero. Provided: Rs." + amount);
        }

        // Lock-free optimistic retry loop (CAS loop)
        while (true) {
            long current = atomicBalance.get();
            if (current < amount) {
                long shortfall = amount - current;
                throw new InsufficientFundsException(
                    "Insufficient funds in AtomicAccount: Cannot withdraw Rs." + amount + ". Shortfall: Rs." + shortfall,
                    shortfall
                );
            }
            if (atomicBalance.compareAndSet(current, current - amount)) {
                break; // Successfully deducted balance atomically
            }
        }
    }

    @Override
    public long getBalance() {
        return atomicBalance != null ? atomicBalance.get() : super.getBalance();
    }

    @Override
    public double interestRate() {
        return 4.5;
    }

    @Override
    public boolean canWithdraw(long amount) {
        return getBalance() >= amount;
    }
}
