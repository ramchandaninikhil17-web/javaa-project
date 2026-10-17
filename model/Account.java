package model;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.annotation.Id;
import model.annotation.MaxLength;
import model.annotation.Positive;

import java.io.Serializable;
import java.util.Objects;

public abstract class Account implements Transactable, InterestBearing, Serializable, Comparable<Account> {
    private static final long serialVersionUID = 1L;
    private static long accountCounter = 1;

    @Id
    private final String accountNumber;

    @MaxLength(30)
    private String ownerName;

    @Positive
    private long balance;

    private boolean active;

    private static String generateAccountNumber() {
        return String.format("AC%04d", accountCounter++);
    }

    public Account(String ownerName, long balance) {
        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = balance;
        this.active = true;
    }

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    @Override
    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero. Provided: Rs." + amount);
        }
        balance += amount;
    }

    /**
     * Unsynchronized deposit used to demonstrate race conditions under concurrent access.
     */
    public void depositUnsafe(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero. Provided: Rs." + amount);
        }
        balance += amount;
    }

    protected void setBalanceDirectly(long newBalance) {
        this.balance = newBalance;
    }

    @Override
    public synchronized void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero. Provided: Rs." + amount);
        }
        if (!canWithdraw(amount)) {
            long shortfall = getShortfall(amount);
            throw new InsufficientFundsException(
                "Insufficient funds: Cannot withdraw Rs." + amount + " from " + accountNumber + ". Shortfall: Rs." + shortfall,
                shortfall
            );
        }
        balance -= amount;
    }

    /**
     * Transfers funds to another account with transactional safety.
     * Uses try-catch-finally and re-throws BankException.
     *
     * @param to the target destination account
     * @param amount the amount to transfer
     * @throws BankException if validation, withdrawal, or deposit fails
     */
    public void transfer(Account to, long amount) throws BankException {
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found (null account specified).");
        }
        if (to.equals(this)) {
            throw new InvalidAmountException("Cannot transfer funds to the same account: " + accountNumber);
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero. Provided: Rs." + amount);
        }

        boolean withdrawn = false;
        try {
            this.withdraw(amount);
            withdrawn = true;
            to.deposit(amount);
        } catch (BankException e) {
            // Rollback withdrawal if the deposit step fails
            if (withdrawn) {
                try {
                    this.deposit(amount);
                } catch (InvalidAmountException ignored) {
                    // Internal rollback
                }
            }
            throw e;
        } finally {
            System.out.println("  [Audit Log] Transfer attempt of Rs." + amount + " from "
                    + this.accountNumber + " to " + to.getAccountNumber() + " completed.");
        }
    }

    /**
     * Calculates the deficit or shortfall if the given amount cannot be withdrawn.
     */
    public long getShortfall(long amount) {
        return Math.max(0, amount - balance);
    }

    // Abstract methods
    @Override
    public abstract double interestRate();
    public abstract boolean canWithdraw(long amount);

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public synchronized long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "Account [Account Number: " + accountNumber + ", Owner: " + ownerName + ", Balance: Rs." + balance + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountNumber);
    }

    /**
     * Requirement 4: Implements natural ordering for Account by comparing account numbers.
     */
    @Override
    public int compareTo(Account other) {
        if (other == null) {
            return 1;
        }
        return this.accountNumber.compareTo(other.accountNumber);
    }
}
