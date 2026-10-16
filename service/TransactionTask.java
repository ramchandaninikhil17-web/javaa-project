package service;

import exception.BankException;
import model.Account;

/**
 * Lab 10 - Part B (Requirement 2): TransactionTask
 * 
 * A simple Runnable task that performs either a deposit or a withdrawal
 * on a given Account. It gets submitted to TransactionProcessor.
 */
public class TransactionTask implements Runnable {

    // Simple enum for 2nd year students: DEPOSIT or WITHDRAW
    public enum Type {
        DEPOSIT,
        WITHDRAW
    }

    private final int taskId;
    private final Account account;
    private final Type type;
    private final long amount;

    public TransactionTask(int taskId, Account account, Type type, long amount) {
        this.taskId = taskId;
        this.account = account;
        this.type = type;
        this.amount = amount;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            if (type == Type.DEPOSIT) {
                account.deposit(amount);
                System.out.println("  [" + threadName + "] Task #" + taskId 
                        + " -> Deposited Rs." + amount + " into " + account.getAccountNumber()
                        + " (" + account.getOwnerName() + ") | Balance: Rs." + account.getBalance());
            } else {
                account.withdraw(amount);
                System.out.println("  [" + threadName + "] Task #" + taskId 
                        + " -> Withdrew Rs." + amount + " from " + account.getAccountNumber()
                        + " (" + account.getOwnerName() + ") | Balance: Rs." + account.getBalance());
            }

            // Small sleep so we can see the 4 worker threads interleave nicely
            Thread.sleep(60);

        } catch (BankException e) {
            System.err.println("  [" + threadName + "] Task #" + taskId + " failed: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("  [" + threadName + "] Task #" + taskId + " was interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    public int getTaskId() {
        return taskId;
    }

    public Account getAccount() {
        return account;
    }

    public Type getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Task #" + taskId + " [" + type + " Rs." + amount + " on " + account.getAccountNumber() + "]";
    }
}
