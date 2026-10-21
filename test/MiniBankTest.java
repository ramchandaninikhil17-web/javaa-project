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
 * Milestone 3 / Practical 14 Requirement 4: MiniBank JUnit Test Suite
 * 
 * Verifies core banking rules using @Test annotated methods:
 * 1. A deposit increases the balance.
 * 2. A withdrawal over the limit throws InsufficientFundsException.
 * 3. Many concurrent deposits keep the balance correct under multithreading.
 * 4. Transfer safely moves funds between two accounts.
 * 5. State persistence saves and reloads accounts without data corruption.
 */
public class MiniBankTest {

    /**
     * Test 1: A deposit increases the account balance.
     */
    @Test
    public void testDepositIncreasesBalance() throws BankException {
        BankService service = new BankService();
        Account acc = service.openAccount(new SavingsAccount("Test Alice", 5000, 1000));
        long initialBalance = acc.getBalance();
        long depositAmount = 2500;

        service.deposit(acc.getAccountNumber(), depositAmount);

        assertEquals(initialBalance + depositAmount, acc.getBalance(), 
                "Deposit must increase the balance by the deposited amount");
    }

    /**
     * Test 2: A withdrawal exceeding available funds/overdraft limit throws InsufficientFundsException.
     */
    @Test
    public void testWithdrawalOverLimitThrowsException() throws BankException {
        BankService service = new BankService();
        // Savings account with balance 2000 and minimum balance 1000.
        // Maximum withdrawable is 1000.
        Account acc = service.openAccount(new SavingsAccount("Test Bob", 2000, 1000));

        // Attempting to withdraw 1500 will leave 500 (< minBalance 1000)
        assertThrows(InsufficientFundsException.class, () -> {
            service.withdraw(acc.getAccountNumber(), 1500);
        });

        // Current account with balance 1000 and overdraft limit 500.
        // Maximum withdrawable is 1500.
        Account currentAcc = service.openAccount(new CurrentAccount("Test Charlie", 1000, 500));
        assertThrows(InsufficientFundsException.class, () -> {
            service.withdraw(currentAcc.getAccountNumber(), 2000);
        });
    }

    /**
     * Test 3: Many concurrent deposits maintain exact mathematical balance without race conditions.
     */
    @Test
    public void testConcurrentDepositsKeepBalanceCorrect() throws Exception {
        BankService service = new BankService();
        Account acc = service.openAccount(new SavingsAccount("Concurrent Depositor", 0, 0));

        final int numThreads = 10;
        final int depositsPerThread = 100;
        final long amountPerDeposit = 10;
        final long expectedFinalBalance = numThreads * depositsPerThread * amountPerDeposit; // 10,000

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

        // Wait for all threads to terminate
        for (Thread t : threads) {
            t.join();
        }

        assertEquals(expectedFinalBalance, acc.getBalance(), 
                "Concurrent synchronized deposits must yield exact final balance without race condition");
    }

    /**
     * Test 4: Transfer properly deducts from source and credits to target account.
     */
    @Test
    public void testTransferBetweenAccounts() throws BankException {
        BankService service = new BankService();
        Account sender = service.openAccount(new SavingsAccount("Sender", 10000, 1000));
        Account receiver = service.openAccount(new SavingsAccount("Receiver", 3000, 1000));

        service.transfer(sender.getAccountNumber(), receiver.getAccountNumber(), 4000);

        assertEquals(6000, sender.getBalance(), "Sender balance must decrease by 4000");
        assertEquals(7000, receiver.getBalance(), "Receiver balance must increase by 4000");
    }

    /**
     * Test 5: State persistence correctly saves and restores accounts from disk.
     */
    @Test
    public void testStatePersistenceSaveAndLoad() throws Exception {
        Path testFile = Paths.get("data", "test_accounts.ser");
        Account[] originalAccounts = new Account[] {
            new SavingsAccount("Persist User 1", 12000, 1000),
            new CurrentAccount("Persist User 2", 25000, 5000)
        };

        // Save
        StatePersister.save(originalAccounts, testFile);
        assertTrue(Files.exists(testFile), "Persisted file must exist on disk");

        // Reload
        Account[] loadedAccounts = StatePersister.load(testFile);
        assertNotNull(loadedAccounts, "Loaded accounts array must not be null");
        assertEquals(2, loadedAccounts.length, "Loaded accounts count must match saved count");
        assertEquals(originalAccounts[0].getAccountNumber(), loadedAccounts[0].getAccountNumber(), "Account number must survive serialization");
        assertEquals(originalAccounts[0].getBalance(), loadedAccounts[0].getBalance(), "Balance must survive serialization");

        // Cleanup test artifact
        Files.deleteIfExists(testFile);
    }

    /**
     * Standalone Test Runner:
     * Reflectively locates all methods marked with @Test, executes them,
     * catches AssertionError or exceptions, and produces a complete test report.
     */
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
