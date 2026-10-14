package service;

import exception.BankException;
import model.Account;

/**
 * Practical 9 - Part B (1): AccountWorker
 * 
 * Implements Runnable to execute repeated transactions concurrently against a shared Account.
 * Supports observing thread names and lifecycle states during execution.
 */
public class AccountWorker implements Runnable {
    private final Account account;
    private final int times;
    private final long amount;
    private final boolean isWithdrawal;

    /**
     * Requirement 1: Constructor with account, times, and amount for concurrent deposits.
     * 
     * @param account the target shared Account
     * @param times the number of operations to perform
     * @param amount the currency amount per transaction
     */
    public AccountWorker(Account account, int times, long amount) {
        this(account, times, amount, false);
    }

    /**
     * Supplementary constructor supporting concurrent withdrawals as well.
     */
    public AccountWorker(Account account, int times, long amount, boolean isWithdrawal) {
        this.account = account;
        this.times = times;
        this.amount = amount;
        this.isWithdrawal = isWithdrawal;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        String opType = isWithdrawal ? "withdraw" : "deposit";

        System.out.println("  [" + threadName + "] State: RUNNABLE | Starting " 
                + times + " " + opType + "s of Rs." + amount);

        for (int i = 0; i < times; i++) {
            try {
                if (isWithdrawal) {
                    account.withdraw(amount);
                } else {
                    account.deposit(amount);
                }
            } catch (BankException e) {
                System.err.println("  [" + threadName + "] Error during " + opType + ": " + e.getMessage());
            }
        }

        System.out.println("  [" + threadName + "] Completed all " + times + " " + opType + " operations.");
    }

    public Account getAccount() {
        return account;
    }

    public int getTimes() {
        return times;
    }

    public long getAmount() {
        return amount;
    }

    public boolean isWithdrawal() {
        return isWithdrawal;
    }
}
