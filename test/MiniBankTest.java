package test;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.CurrentAccount;
import model.SavingsAccount;
import service.BankService;
import service.StatePersister;
import test.annotation.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static test.Assertions.*;

/**
 * Test suite verifying account transactions, concurrency, and persistence.
 */
public class MiniBankTest {

    @Test
    public void testDepositIncreasesBalance() throws BankException {
        BankService service = new BankService();
        Account acc = service.openAccount(new SavingsAccount("Test Alice", 5000, 1000));
        long initialBalance = acc.getBalance();
        long depositAmount = 2500;

        service.deposit(acc.getAccountNumber(), depositAmount);

        assertEquals(initialBalance + depositAmount, acc.getBalance(), 
                "Deposit must increase balance by deposited amount");
    }

    @Test
    public void testWithdrawalOverLimitThrowsException() throws BankException {
        BankService service = new BankService();
        Account savings = service.openAccount(new SavingsAccount("Test Bob", 2000, 1000));

        // Attempt to withdraw beyond allowed minimum balance
        assertThrows(InsufficientFundsException.class, () -> {
            service.withdraw(savings.getAccountNumber(), 1500);
        });

        // Attempt to withdraw beyond allowed overdraft limit
        Account current = service.openAccount(new CurrentAccount("Test Charlie", 1000, 500));
        assertThrows(InsufficientFundsException.class, () -> {
            service.withdraw(current.getAccountNumber(), 2000);
        });
    }

    @Test
    public void testConcurrentDepositsKeepBalanceCorrect() throws Exception {
        BankService service = new BankService();
        Account acc = service.openAccount(new SavingsAccount("Concurrent Depositor", 0, 0));

        final int numThreads = 10;
        final int depositsPerThread = 100;
        final long amountPerDeposit = 10;
        final long expectedFinalBalance = numThreads * depositsPerThread * amountPerDeposit;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < depositsPerThread; j++) {
                    try {
                        service.deposit(acc.getAccountNumber(), amountPerDeposit);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        assertEquals(expectedFinalBalance, acc.getBalance(), 
                "Concurrent deposits must match expected balance without race conditions");
    }

    @Test
    public void testTransferBetweenAccounts() throws BankException {
        BankService service = new BankService();
        Account sender = service.openAccount(new SavingsAccount("Sender", 10000, 1000));
        Account receiver = service.openAccount(new SavingsAccount("Receiver", 3000, 1000));

        service.transfer(sender.getAccountNumber(), receiver.getAccountNumber(), 4000);

        assertEquals(6000, sender.getBalance(), "Sender balance should be debited");
        assertEquals(7000, receiver.getBalance(), "Receiver balance should be credited");
    }

    @Test
    public void testStatePersistenceSaveAndLoad() throws Exception {
        Path testFile = Paths.get("data", "test_accounts.ser");
        Account[] originalAccounts = new Account[] {
            new SavingsAccount("Persist User 1", 12000, 1000),
            new CurrentAccount("Persist User 2", 25000, 5000)
        };

        StatePersister.save(originalAccounts, testFile);
        assertTrue(Files.exists(testFile), "File should be created on disk");

        Account[] loadedAccounts = StatePersister.load(testFile);
        assertNotNull(loadedAccounts);
        assertEquals(2, loadedAccounts.length);
        assertEquals(originalAccounts[0].getAccountNumber(), loadedAccounts[0].getAccountNumber());
        assertEquals(originalAccounts[0].getBalance(), loadedAccounts[0].getBalance());

        Files.deleteIfExists(testFile);
    }

    // Runner reflectively executes all @Test methods
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("        MINIBANK JUNIT TEST SUITE RUNNER         ");
        System.out.println("=================================================");

        MiniBankTest suite = new MiniBankTest();
        Method[] methods = MiniBankTest.class.getDeclaredMethods();

        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;

        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                totalTests++;
                String testName = method.getName();
                System.out.printf("  Running: %-42s ... ", testName);
                try {
                    method.invoke(suite);
                    System.out.println("[PASSED]");
                    passedTests++;
                } catch (Throwable t) {
                    System.out.println("[FAILED]");
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    System.err.println("    -> Error: " + cause.getMessage());
                    cause.printStackTrace(System.err);
                    failedTests++;
                }
            }
        }

        System.out.println("-------------------------------------------------");
        System.out.printf("Test Results: Total: %d | Passed: %d | Failed: %d%n",
                totalTests, passedTests, failedTests);
        System.out.println("=================================================");

        if (failedTests > 0) {
            System.err.println("ERROR: Some unit tests failed.");
            System.exit(1);
        } else {
            System.out.println("SUCCESS: All MiniBank unit tests passed successfully!");
        }
    }
}
