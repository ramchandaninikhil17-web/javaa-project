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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service layer coordinating account operations, persistence,
 * and transaction processing across the repository.
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

    @Audit(action = "OPEN_ACCOUNT")
    public Account openAccount(Account account) throws InvalidAmountException {
        AuditLogger.logCall(BankService.class, "openAccount", new Class<?>[]{Account.class}, new Object[]{account});

        if (account == null) {
            throw new IllegalArgumentException("Cannot open null account.");
        }
        if (account.getBalance() < 0) {
            throw new InvalidAmountException("Opening balance cannot be negative: Rs." + account.getBalance());
        }

        repository.save(account);
        accountStore.add(account);
        transactionProcessor.addAccount(account);
        logTransaction("OPEN_ACCOUNT " + account.getAccountNumber() + " " + account.getBalance());

        return account;
    }

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
            default -> {
                long minBalance = extraParam > 0 ? extraParam : 1000;
                account = new SavingsAccount(ownerName.trim(), initialBalance, minBalance);
            }
        }

        return openAccount(account);
    }

    @Audit(action = "DEPOSIT")
    public void deposit(String accountNumber, long amount) 
            throws AccountNotFoundException, InvalidAmountException {
        AuditLogger.logCall(BankService.class, "deposit", 
                new Class<?>[]{String.class, long.class}, new Object[]{accountNumber, amount});

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be positive: Rs." + amount);
        }

        Account account = repository.findById(accountNumber.trim());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }

        account.deposit(amount);
        accountStore.addTransaction("DEPOSIT: Rs." + amount + " credited to " + account.getAccountNumber());
        logTransaction("DEPOSIT " + account.getAccountNumber() + " " + amount);
    }

    @Audit(action = "WITHDRAW")
    public void withdraw(String accountNumber, long amount) 
            throws AccountNotFoundException, InsufficientFundsException, InvalidAmountException {
        AuditLogger.logCall(BankService.class, "withdraw", 
                new Class<?>[]{String.class, long.class}, new Object[]{accountNumber, amount});

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty.");
        }
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be positive: Rs." + amount);
        }

        Account account = repository.findById(accountNumber.trim());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }

        account.withdraw(amount);
        accountStore.addTransaction("WITHDRAW: Rs." + amount + " debited from " + account.getAccountNumber());
        logTransaction("WITHDRAW " + account.getAccountNumber() + " " + amount);
    }

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
            throw new InvalidAmountException("Transfer amount must be positive: Rs." + amount);
        }

        Account from = repository.findById(fromAccountNum.trim());
        if (from == null) {
            throw new AccountNotFoundException("Source account not found: " + fromAccountNum);
        }

        Account to = repository.findById(toAccountNum.trim());
        if (to == null) {
            throw new AccountNotFoundException("Destination account not found: " + toAccountNum);
        }

        // Ordered lock acquisition to prevent deadlock
        TransferManager.transferSafe(from, to, amount);

        accountStore.addTransaction("TRANSFER: Rs." + amount + " moved from " 
                + from.getAccountNumber() + " to " + to.getAccountNumber());
        logTransaction("WITHDRAW " + from.getAccountNumber() + " " + amount);
        logTransaction("DEPOSIT " + to.getAccountNumber() + " " + amount);
    }

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

    public List<Account> listAccounts() {
        return repository.findAll();
    }

    public Account findAccount(String accountNumber) {
        if (accountNumber == null) return null;
        return repository.findById(accountNumber.trim());
    }

    public void processBatch(List<TransactionTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("  [Batch Processor] No tasks to process.");
            return;
        }

        System.out.println("  [Batch Processor] Submitting " + tasks.size() + " tasks to worker pool...");
        for (TransactionTask task : tasks) {
            if (task.getAccount() != null) {
                transactionProcessor.addAccount(task.getAccount());
            }
        }

        for (TransactionTask task : tasks) {
            transactionProcessor.submit(task);
        }

        transactionProcessor.stop();
        System.out.println("  [Batch Processor] Batch execution completed.");
    }

    public int loadAccounts(Path file) {
        Path targetFile = (file != null) ? file : DEFAULT_DATA_FILE;
        if (!Files.exists(targetFile)) {
            System.out.println("  [Persistence] No saved accounts found at " + targetFile.toAbsolutePath().normalize());
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
            System.out.println("  [Persistence] Loaded " + count + " accounts from " + targetFile);
            return count;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("  [Persistence] Failed to load accounts: " + e.getMessage());
            return 0;
        }
    }

    public boolean saveAccounts(Path file) {
        Path targetFile = (file != null) ? file : DEFAULT_DATA_FILE;
        try {
            List<Account> all = repository.findAll();
            Account[] array = all.toArray(new Account[0]);
            StatePersister.save(array, targetFile);
            System.out.println("  [Persistence] Saved " + array.length + " accounts to " 
                    + targetFile.toAbsolutePath().normalize());
            return true;
        } catch (IOException e) {
            System.err.println("  [Persistence] Failed to save accounts: " + e.getMessage());
            return false;
        }
    }

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
