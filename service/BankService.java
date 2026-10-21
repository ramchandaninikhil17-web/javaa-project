package service;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;
import model.annotation.Audit;
import util.AuditLogger;
import util.StatementFormatter;
import util.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Milestone 3 / Practical 14 Requirement 1: BankService
 * 
 * Core business-logic service layer for MiniBank.
 * - Wires Repository<Account>, input validation, and custom exceptions.
 * - Manages account lifecycle: openAccount, deposit, withdraw, transfer, statement, listAccounts.
 * - Coordinates thread-safe batch processing using TransactionProcessor.
 * - Implements object serialization persistence and NIO transaction reporting.
 * - Features @Audit annotations inspected at runtime by the reflective AuditLogger.
 */
public class BankService {

    private final Repository<Account> repository;
    private final AccountStore accountStore;
    private final TransactionProcessor transactionProcessor;

    private static final Path DEFAULT_DATA_FILE = Paths.get("data", "accounts.ser");
    private static final Path DEFAULT_LOG_FILE = Paths.get("logs", "transactions_daily.log");
    private static final Path DEFAULT_LOGS_DIR = Paths.get("logs");
    private static final Path DEFAULT_REPORT_FILE = Paths.get("reports", "end_of_day_report.txt");

    public BankService() {
        this.repository = new Repository<>();
        this.accountStore = new AccountStore();
        this.transactionProcessor = new TransactionProcessor();
    }

    /**
     * Opens a new account and persists it in the repository.
     * Uses reflection-based auditing via @Audit.
     * 
     * @param account the account instance to register
     * @return the saved Account
     * @throws InvalidAmountException if initial balance is negative
     */
    @Audit(action = "OPEN_ACCOUNT")
    public Account openAccount(Account account) throws InvalidAmountException {
        AuditLogger.logCall(BankService.class, "openAccount", new Class<?>[]{Account.class}, new Object[]{account});

        if (account == null) {
            throw new IllegalArgumentException("Cannot open null account.");
        }
        if (account.getBalance() < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative. Provided: Rs." + account.getBalance());
        }

        // Save into Generic Repository
        repository.save(account);

        // Keep AccountStore and thread-safe TransactionProcessor in sync
        accountStore.add(account);
        transactionProcessor.addAccount(account);

        // Log transaction to append-only log file
        logTransaction("OPEN_ACCOUNT " + account.getAccountNumber() + " " + account.getBalance());

        return account;
    }

    /**
     * Overloaded helper: Opens an account by type, owner name, initial balance, and optional parameter.
     */
    public Account openAccount(String type, String ownerName, long initialBalance, long extraParam) 
            throws InvalidAmountException {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account owner name cannot be empty.");
        }
        if (initialBalance < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative: Rs." + initialBalance);
        }

        Account account;
        String normalizedType = (type != null) ? type.trim().toUpperCase() : "SAVINGS";

        switch (normalizedType) {
            case "CURRENT" -> {
                long overdraft = extraParam > 0 ? extraParam : 5000;
                account = new CurrentAccount(ownerName.trim(), initialBalance, overdraft);
            }
            case "FD", "FIXED_DEPOSIT", "FIXED DEPOSIT" -> {
                account = new FixedDepositAccount(ownerName.trim(), initialBalance);
            }
            default -> { // SAVINGS
                long minBalance = extraParam > 0 ? extraParam : 1000;
                account = new SavingsAccount(ownerName.trim(), initialBalance, minBalance);
            }
        }

        return openAccount(account);
    }

    /**
     * Deposits money into the specified account.
     * 
     * @param accountNumber target account number
     * @param amount deposit amount (must be positive)
     * @throws AccountNotFoundException if account does not exist
     * @throws InvalidAmountException if amount <= 0
     */
    @Audit(action = "DEPOSIT")
    public void deposit(String accountNumber, long amount) 
            throws AccountNotFoundException, InvalidAmountException {
        AuditLogger.logCall(BankService.class, "deposit", 
                new Class<?>[]{String.class, long.class}, new Object[]{accountNumber, amount});

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive. Provided: Rs." + amount);
        }

        Account account = repository.findById(accountNumber.trim());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }

        // Thread-safe synchronized deposit on account
        account.deposit(amount);

        // Log history and append to daily log
        accountStore.addTransaction("DEPOSIT: Rs." + amount + " credited to " + account.getAccountNumber());
        logTransaction("DEPOSIT " + account.getAccountNumber() + " " + amount);
    }

    /**
     * Withdraws money from the specified account.
     * 
     * @param accountNumber target account number
     * @param amount withdrawal amount
     * @throws AccountNotFoundException if account does not exist
     * @throws InsufficientFundsException if withdrawal violates account limits
     * @throws InvalidAmountException if amount <= 0
     */
    @Audit(action = "WITHDRAW")
    public void withdraw(String accountNumber, long amount) 
            throws AccountNotFoundException, InsufficientFundsException, InvalidAmountException {
        AuditLogger.logCall(BankService.class, "withdraw", 
                new Class<?>[]{String.class, long.class}, new Object[]{accountNumber, amount});

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive. Provided: Rs." + amount);
        }

        Account account = repository.findById(accountNumber.trim());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }

        // Thread-safe synchronized withdrawal on account (checks canWithdraw)
        account.withdraw(amount);

        // Log history and append to daily log
        accountStore.addTransaction("WITHDRAW: Rs." + amount + " debited from " + account.getAccountNumber());
        logTransaction("WITHDRAW " + account.getAccountNumber() + " " + amount);
    }

    /**
     * Transfers money between two accounts with deadlock avoidance.
     * 
     * @param fromAccountNum source account number
     * @param toAccountNum destination account number
     * @param amount amount to transfer
     * @throws AccountNotFoundException if either account is missing
     * @throws InsufficientFundsException if source has insufficient funds
     * @throws InvalidAmountException if amount <= 0 or source equals destination
     * @throws BankException on any general banking error
     */
    @Audit(action = "TRANSFER")
    public void transfer(String fromAccountNum, String toAccountNum, long amount) 
            throws BankException {
        AuditLogger.logCall(BankService.class, "transfer", 
                new Class<?>[]{String.class, String.class, long.class}, 
                new Object[]{fromAccountNum, toAccountNum, amount});

        if (fromAccountNum == null || toAccountNum == null) {
            throw new AccountNotFoundException("Account numbers cannot be null.");
        }
        if (fromAccountNum.trim().equalsIgnoreCase(toAccountNum.trim())) {
            throw new InvalidAmountException("Cannot transfer funds to the same account: " + fromAccountNum);
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive. Provided: Rs." + amount);
        }

        Account from = repository.findById(fromAccountNum.trim());
        if (from == null) {
            throw new AccountNotFoundException("Source account not found: " + fromAccountNum);
        }

        Account to = repository.findById(toAccountNum.trim());
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found: " + toAccountNum);
        }

        // Deadlock-free transfer using TransferManager lock ordering
        TransferManager.transferSafe(from, to, amount);

        // Record history and append log entries
        accountStore.addTransaction("TRANSFER: Rs." + amount + " moved from " 
                + from.getAccountNumber() + " to " + to.getAccountNumber());
        logTransaction("WITHDRAW " + from.getAccountNumber() + " " + amount);
        logTransaction("DEPOSIT " + to.getAccountNumber() + " " + amount);
    }

    /**
     * Builds and returns a formatted statement for the specified account.
     * 
     * @param accountNumber account number to format statement for
     * @return formatted multi-line statement
     * @throws AccountNotFoundException if account does not exist
     */
    public String statement(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        Account account = repository.findById(accountNumber.trim());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return StatementFormatter.buildStatement(account);
    }

    /**
     * Returns an unmodifiable list of all accounts registered in the repository.
     * 
     * @return list of accounts
     */
    public List<Account> listAccounts() {
        return repository.findAll();
    }

    /**
     * Finds an account by account number.
     * 
     * @param accountNumber account number
     * @return Account or null
     */
    public Account findAccount(String accountNumber) {
        if (accountNumber == null) return null;
        return repository.findById(accountNumber.trim());
    }

    /**
     * Requirement 3: Processes a batch of transactions concurrently and safely using
     * the managed TransactionProcessor thread pool over the thread-safe store.
     * 
     * @param tasks list of transaction tasks to execute
     */
    public void processBatch(List<TransactionTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("  [Batch Processor] No tasks to process.");
            return;
        }

        System.out.println("  [Batch Processor] Submitting " + tasks.size() + " tasks to TransactionProcessor pool...");
        // Ensure all target accounts are present in the shared ConcurrentHashMap
        for (TransactionTask task : tasks) {
            if (task.getAccount() != null) {
                transactionProcessor.addAccount(task.getAccount());
            }
        }

        // Submit all tasks
        for (TransactionTask task : tasks) {
            transactionProcessor.submit(task);
        }

        // Stop and await completion of the batch
        transactionProcessor.stop();
        System.out.println("  [Batch Processor] Batch execution completed safely across worker threads.");
    }

    /**
     * Loads saved accounts from disk (Practical 11) using StatePersister.
     * 
     * @param file path to serialized accounts file
     * @return number of accounts successfully loaded
     */
    public int loadAccounts(Path file) {
        Path targetFile = (file != null) ? file : DEFAULT_DATA_FILE;
        if (!Files.exists(targetFile)) {
            System.out.println("  [Persistence] No saved accounts file found at " + targetFile.toAbsolutePath().normalize());
            return 0;
        }

        try {
            Account[] accounts = StatePersister.load(targetFile);
            int count = 0;
            for (Account acc : accounts) {
                if (acc != null) {
                    repository.save(acc);
                    accountStore.add(acc);
                    transactionProcessor.addAccount(acc);
                    count++;
                }
            }
            System.out.println("  [Persistence] Successfully loaded " + count + " accounts from " + targetFile);
            return count;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("  [Persistence] Failed to load accounts: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Saves all current accounts to disk (Practical 11) using StatePersister.
     * 
     * @param file destination path
     * @return true if successfully saved, false otherwise
     */
    public boolean saveAccounts(Path file) {
        Path targetFile = (file != null) ? file : DEFAULT_DATA_FILE;
        try {
            List<Account> all = repository.findAll();
            Account[] array = all.toArray(new Account[0]);
            StatePersister.save(array, targetFile);
            System.out.println("  [Persistence] Successfully saved " + array.length + " accounts to " 
                    + targetFile.toAbsolutePath().normalize());
            return true;
        } catch (IOException e) {
            System.err.println("  [Persistence] Failed to save accounts: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates and returns the End-of-Day summary report (Practical 11) via ReportGenerator.
     * 
     * @return formatted report summary string
     * @throws IOException on I/O error
     */
    public String generateEndOfDayReport() throws IOException {
        return ReportGenerator.generateReport(DEFAULT_LOGS_DIR, DEFAULT_REPORT_FILE);
    }

    private void logTransaction(String logLine) {
        try {
            TransactionLog.append(DEFAULT_LOG_FILE, logLine);
        } catch (IOException e) {
            System.err.println("  [Log Warning] Could not append to transaction log: " + e.getMessage());
        }
    }

    public Repository<Account> getRepository() {
        return repository;
    }

    public AccountStore getAccountStore() {
        return accountStore;
    }
}
