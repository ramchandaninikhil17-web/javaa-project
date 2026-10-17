package service;

import model.Account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Milestone 2 Checkpoint - Requirements 1, 2, & 3: AccountStore
 * 
 * Manages bank accounts and transaction history utilizing Java Collections Framework.
 * - Stores accounts in a HashMap<String, Account> keyed by account number for O(1) lookups.
 * - Maintains an append-only transaction history in an ArrayList<String>.
 * - Provides sorted views using Comparator and natural ordering Comparable.
 */
public class AccountStore {

    // Requirement 1: Keeps accounts in a HashMap<String, Account> using accountNumber as key
    private final Map<String, Account> accounts = new HashMap<>();

    // Requirement 2: Keep transaction history in an ArrayList<String>
    private final List<String> transactionHistory = new ArrayList<>();

    public AccountStore() {}

    /**
     * Requirement 1: Adds an Account to the store keyed by its account number.
     * 
     * @param account the account to store
     */
    public void add(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Cannot add null account to AccountStore");
        }
        accounts.put(account.getAccountNumber(), account);
        addTransaction("REGISTER_ACCOUNT: Added account " + account.getAccountNumber() 
                + " (" + account.getOwnerName() + ") with balance Rs." + account.getBalance());
    }

    /**
     * Requirement 1: Retrieves an Account by its unique account number in O(1) time.
     * 
     * @param accountNumber the account number key
     * @return the matching Account, or null if not found
     */
    public Account get(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /**
     * Requirement 1: Returns all accounts stored in the store.
     * 
     * @return a collection view of all accounts
     */
    public Collection<Account> getAll() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Requirement 2: Appends a transaction record to the history list.
     * 
     * @param transaction the transaction description string
     */
    public void addTransaction(String transaction) {
        if (transaction != null) {
            transactionHistory.add(transaction);
        }
    }

    /**
     * Requirement 2: Returns the complete transaction history list.
     * 
     * @return an ArrayList containing all transaction records
     */
    public ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    /**
     * Requirement 3: Returns an ArrayList<Account> sorted by balance using a Comparator.
     * 
     * @return an ArrayList<Account> sorted by balance in ascending order
     */
    public ArrayList<Account> sortedByBalance() {
        ArrayList<Account> sortedList = new ArrayList<>(accounts.values());
        sortedList.sort(Comparator.comparingLong(Account::getBalance));
        return sortedList;
    }

    /**
     * Convenience helper: Returns an ArrayList<Account> sorted by balance in descending order.
     * 
     * @return an ArrayList<Account> sorted by highest balance first
     */
    public ArrayList<Account> sortedByBalanceDescending() {
        ArrayList<Account> sortedList = new ArrayList<>(accounts.values());
        sortedList.sort(Comparator.comparingLong(Account::getBalance).reversed());
        return sortedList;
    }

    /**
     * Convenience helper: Returns an ArrayList<Account> sorted by natural ordering (account number)
     * using the Comparable<Account> implementation.
     * 
     * @return an ArrayList<Account> sorted by account number
     */
    public ArrayList<Account> sortedByAccountNumber() {
        ArrayList<Account> sortedList = new ArrayList<>(accounts.values());
        java.util.Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * Returns the total count of accounts currently in the store.
     * 
     * @return number of stored accounts
     */
    public int size() {
        return accounts.size();
    }
}
