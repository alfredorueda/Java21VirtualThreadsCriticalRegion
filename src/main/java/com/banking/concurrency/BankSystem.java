package com.banking.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a banking system that manages accounts and processes transactions concurrently.
 * 
 * This class demonstrates the use of Java 21 virtual threads for scalable concurrency.
 * It showcases how to manage thread-safe collections alongside explicit synchronization
 * for complex operations that span multiple steps or objects.
 */
public class BankSystem {

    /**
     * Thread-safe collection to store all bank accounts.
     * ConcurrentHashMap provides thread safety for individual operations but
     * not for compound operations that involve multiple steps.
     */
    private final Map<Integer, BankAccount> accounts = new ConcurrentHashMap<>();
    
    /**
     * Creates a new banking system with no accounts.
     */
    public BankSystem() {
    }
    
    /**
     * Creates a new account with the given ID and initial balance.
     *
     * @param accountId The unique identifier for the account
     * @param initialBalance The starting balance of the account
     * @return The newly created bank account
     * @throws IllegalArgumentException if an account with the given ID already exists
     */
    public BankAccount createAccount(int accountId, double initialBalance) {
        if (accounts.containsKey(accountId)) {
            throw new IllegalArgumentException("Account with ID " + accountId + " already exists");
        }
        
        BankAccount newAccount = new BankAccount(accountId, initialBalance);
        accounts.put(accountId, newAccount);
        return newAccount;
    }
    
    /**
     * Retrieves an account by its ID.
     *
     * @param accountId The ID of the account to retrieve
     * @return The account with the specified ID
     * @throws IllegalArgumentException if no account with the given ID exists
     */
    public BankAccount getAccount(int accountId) {
        BankAccount account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account with ID " + accountId + " does not exist");
        }
        return account;
    }
    
    /**
     * Gets the total balance across all accounts in the system.
     * This method demonstrates how to safely iterate over a concurrent collection.
     *
     * @return The sum of all account balances
     */
    public double getTotalSystemBalance() {
        return accounts.values().stream()
                .mapToDouble(BankAccount::getBalance)
                .sum();
    }
    
    /**
     * Gets all accounts in the system.
     *
     * @return A list of all bank accounts
     */
    public List<BankAccount> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
    
    /**
     * Gets the number of accounts in the system.
     *
     * @return The number of accounts
     */
    public int getAccountCount() {
        return accounts.size();
    }
    
    /**
     * Processes a list of transactions concurrently using virtual threads.
     * This method demonstrates the efficient use of Java 21 virtual threads
     * for concurrent transaction processing.
     *
     * @param transactions The list of transaction tasks to process
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void processTransactions(List<TransactionTask> transactions) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(transactions.size());
        AtomicBoolean error = new AtomicBoolean(false);
        
        for (TransactionTask task : transactions) {
            Thread.startVirtualThread(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("Error processing transaction: " + e.getMessage());
                    error.set(true);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all transactions to complete
        latch.await();
        
        if (error.get()) {
            throw new RuntimeException("One or more transactions failed");
        }
    }
}