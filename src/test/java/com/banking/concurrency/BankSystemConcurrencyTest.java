package com.banking.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Banking System with focus on concurrent operations.
 * 
 * This test suite validates the thread safety and correctness of the banking
 * system under high concurrency using Java 21 virtual threads.
 */
public class BankSystemConcurrencyTest {

    private BankSystem bankSystem;
    private double initialSystemBalance;
    private static final int NUM_ACCOUNTS = 5;
    private static final double INITIAL_ACCOUNT_BALANCE = 1000.0;
    private static final int NUM_TRANSACTIONS = 5000;
    private static final double MAX_TRANSACTION_AMOUNT = 100.0;
    private static final Random random = new Random();

    @BeforeEach
    void setUp() {
        bankSystem = new BankSystem();
        initialSystemBalance = 0;
        
        // Create test accounts
        for (int i = 1; i <= NUM_ACCOUNTS; i++) {
            bankSystem.createAccount(i, INITIAL_ACCOUNT_BALANCE);
            initialSystemBalance += INITIAL_ACCOUNT_BALANCE;
        }
    }

    /**
     * Tests that concurrent deposits to multiple accounts are handled correctly.
     * All deposits should succeed, and the system balance should increase by
     * the total deposited amount.
     */
    @Test
    @DisplayName("Test concurrent deposits to multiple accounts")
    void testConcurrentDeposits() throws InterruptedException {
        // Create deposit tasks
        List<TransactionTask> depositTasks = new ArrayList<>();
        double totalDepositAmount = 0;
        
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            int accountId = random.nextInt(NUM_ACCOUNTS) + 1;
            double amount = random.nextDouble() * MAX_TRANSACTION_AMOUNT;
            totalDepositAmount += amount;
            
            depositTasks.add(new TransactionTask(
                    TransactionTask.TransactionType.DEPOSIT,
                    bankSystem.getAccount(accountId),
                    amount
            ));
        }
        
        // Process deposits concurrently
        bankSystem.processTransactions(depositTasks);
        
        // Validate results
        double expectedTotalBalance = initialSystemBalance + totalDepositAmount;
        double actualTotalBalance = bankSystem.getTotalSystemBalance();
        
        assertEquals(expectedTotalBalance, actualTotalBalance, 0.001,
                "System total balance should match the initial balance plus all deposits");
        
        // Ensure no account has a negative balance
        for (BankAccount account : bankSystem.getAllAccounts()) {
            assertTrue(account.getBalance() >= INITIAL_ACCOUNT_BALANCE,
                    "Account balance should not be less than initial balance after deposits");
        }
    }

    /**
     * Tests that concurrent withdrawals from accounts are handled correctly.
     * Withdrawals should only succeed if sufficient funds are available,
     * and no account should end up with a negative balance.
     */
    @Test
    @DisplayName("Test concurrent withdrawals from multiple accounts")
    void testConcurrentWithdrawals() throws InterruptedException {
        // Create withdrawal tasks
        List<TransactionTask> withdrawalTasks = new ArrayList<>();
        
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            int accountId = random.nextInt(NUM_ACCOUNTS) + 1;
            double amount = random.nextDouble() * (MAX_TRANSACTION_AMOUNT / 2); // Smaller amount to avoid too many failures
            
            withdrawalTasks.add(new TransactionTask(
                    TransactionTask.TransactionType.WITHDRAW,
                    bankSystem.getAccount(accountId),
                    amount
            ));
        }
        
        // Process withdrawals concurrently
        bankSystem.processTransactions(withdrawalTasks);
        
        // Validate results
        double actualTotalBalance = bankSystem.getTotalSystemBalance();
        assertTrue(actualTotalBalance <= initialSystemBalance,
                "System balance should not exceed the initial balance after withdrawals");
        
        // Ensure no account has a negative balance
        for (BankAccount account : bankSystem.getAllAccounts()) {
            assertTrue(account.getBalance() >= 0,
                    "No account should have a negative balance");
        }
    }

    /**
     * Tests that concurrent transfers between accounts are handled correctly.
     * The total system balance should remain constant, and no account
     * should end up with a negative balance.
     */
    @Test
    @DisplayName("Test concurrent transfers between accounts")
    void testConcurrentTransfers() throws InterruptedException {
        // Create transfer tasks
        List<TransactionTask> transferTasks = new ArrayList<>();
        
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            int sourceId = random.nextInt(NUM_ACCOUNTS) + 1;
            int destId;
            do {
                destId = random.nextInt(NUM_ACCOUNTS) + 1;
            } while (destId == sourceId); // Ensure different accounts
            
            double amount = random.nextDouble() * (MAX_TRANSACTION_AMOUNT / 2);
            
            transferTasks.add(new TransactionTask(
                    bankSystem.getAccount(sourceId),
                    bankSystem.getAccount(destId),
                    amount
            ));
        }
        
        // Process transfers concurrently
        bankSystem.processTransactions(transferTasks);
        
        // Validate results
        double actualTotalBalance = bankSystem.getTotalSystemBalance();
        assertEquals(initialSystemBalance, actualTotalBalance, 0.001,
                "System total balance should remain unchanged after transfers");
        
        // Ensure no account has a negative balance
        for (BankAccount account : bankSystem.getAllAccounts()) {
            assertTrue(account.getBalance() >= 0,
                    "No account should have a negative balance");
        }
    }

    /**
     * Tests a mixed workload of concurrent deposits, withdrawals, and transfers.
     * This simulates a realistic banking scenario with various operations
     * happening simultaneously.
     */
    @Test
    @DisplayName("Test mixed concurrent banking operations")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testMixedConcurrentOperations() throws InterruptedException {
        // Create mixed transaction tasks
        List<TransactionTask> mixedTasks = new ArrayList<>();
        double totalDeposits = 0;
        
        for (int i = 0; i < NUM_TRANSACTIONS; i++) {
            int type = random.nextInt(3); // 0: deposit, 1: withdraw, 2: transfer
            int accountId1 = random.nextInt(NUM_ACCOUNTS) + 1;
            double amount = random.nextDouble() * (MAX_TRANSACTION_AMOUNT / 2);
            
            switch (type) {
                case 0: // Deposit
                    mixedTasks.add(new TransactionTask(
                            TransactionTask.TransactionType.DEPOSIT,
                            bankSystem.getAccount(accountId1),
                            amount
                    ));
                    totalDeposits += amount;
                    break;
                    
                case 1: // Withdraw
                    mixedTasks.add(new TransactionTask(
                            TransactionTask.TransactionType.WITHDRAW,
                            bankSystem.getAccount(accountId1),
                            amount
                    ));
                    break;
                    
                case 2: // Transfer
                    int accountId2;
                    do {
                        accountId2 = random.nextInt(NUM_ACCOUNTS) + 1;
                    } while (accountId2 == accountId1);
                    
                    mixedTasks.add(new TransactionTask(
                            bankSystem.getAccount(accountId1),
                            bankSystem.getAccount(accountId2),
                            amount
                    ));
                    break;
            }
        }
        
        // Process mixed transactions concurrently
        bankSystem.processTransactions(mixedTasks);
        
        // Validate results
        double actualTotalBalance = bankSystem.getTotalSystemBalance();
        double expectedMaxBalance = initialSystemBalance + totalDeposits;
        
        assertTrue(actualTotalBalance <= expectedMaxBalance,
                "System balance should not exceed initial balance plus deposits");
        
        // Ensure no account has a negative balance
        for (BankAccount account : bankSystem.getAllAccounts()) {
            assertTrue(account.getBalance() >= 0,
                    "No account should have a negative balance");
        }
    }
    
    /**
     * This test intentionally creates an unsafe version of a bank account operation
     * to demonstrate the types of bugs that arise without proper synchronization.
     * 
     * WARNING: This test is expected to fail sometimes, demonstrating race conditions.
     */
    @Test
    @DisplayName("Demonstration of race conditions without synchronization")
    void demonstrationOfRaceConditions() throws InterruptedException {
        // Create an account with a deliberate race condition
        class UnsafeBankAccount {
            private double balance = INITIAL_ACCOUNT_BALANCE;
            
            // Unsafe withdraw without synchronization
            public boolean withdraw(double amount) {
                if (balance >= amount) {
                    // Simulate some processing time that makes race conditions more likely
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    balance -= amount;  // This operation is not atomic!
                    return true;
                }
                return false;
            }
            
            public double getBalance() {
                return balance;
            }
        }
        
        UnsafeBankAccount unsafeAccount = new UnsafeBankAccount();
        int numWithdrawals = 1000;
        double withdrawalAmount = 1.0; // Small enough that all should succeed if synchronized
        
        // Total withdrawal amount would be exactly the initial balance
        double totalWithdrawalAmount = withdrawalAmount * numWithdrawals;
        assertEquals(INITIAL_ACCOUNT_BALANCE, totalWithdrawalAmount, 
                "Test is set up so all withdrawals should succeed if synchronized");
        
        // Create and start threads to perform concurrent withdrawals
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numWithdrawals; i++) {
            Thread t = Thread.startVirtualThread(() -> {
                unsafeAccount.withdraw(withdrawalAmount);
            });
            threads.add(t);
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        // In the absence of proper synchronization, we'll likely see:
        // 1. The account balance is less than zero (negative balance)
        // 2. The account balance is not zero (some withdrawals were lost)
        double finalBalance = unsafeAccount.getBalance();
        System.out.println("DEMONSTRATION - Unsafe final balance: " + finalBalance);
        System.out.println("DEMONSTRATION - With proper synchronization, balance would be exactly 0");
        
        // Note: We don't assert anything here because the point is to demonstrate
        // the unpredictable behavior that occurs without proper synchronization
    }
}