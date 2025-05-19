package com.banking.concurrency;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a bank account with thread-safe operations using ReentrantLock.
 * 
 * This class demonstrates the proper use of explicit locking for critical sections
 * in a concurrent banking environment. ReentrantLock is preferred over synchronized
 * because it offers:
 * - Explicit control over lock acquisition and release
 * - Non-block-structured locking (unlike synchronized)
 * - Ability to implement timeouts on lock attempts
 * - Interruptible lock acquisition
 * - Better control over fairness policies
 */
public class BankAccount {
    
    private final int accountId;
    private double balance;
    
    /**
     * Lock used to protect the critical sections in this account's operations.
     * ReentrantLock provides more flexibility than synchronized blocks.
     */
    private final Lock lock = new ReentrantLock();
    
    /**
     * Creates a new bank account with the specified ID and initial balance.
     *
     * @param accountId The unique identifier for this account
     * @param initialBalance The starting balance of the account
     * @throws IllegalArgumentException if initialBalance is negative
     */
    public BankAccount(int accountId, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.accountId = accountId;
        this.balance = initialBalance;
    }
    
    /**
     * Gets the unique identifier of this account.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }
    
    /**
     * Safely retrieves the current balance, protected by a lock to ensure
     * a consistent view even during concurrent operations.
     *
     * @return The current account balance
     */
    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Deposits the specified amount into this account.
     * The operation is protected by a lock to ensure thread safety.
     *
     * @param amount The amount to deposit
     * @throws IllegalArgumentException if amount is negative or zero
     */
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Withdraws the specified amount from this account if sufficient funds are available.
     * The operation is protected by a lock to ensure thread safety and data consistency.
     *
     * @param amount The amount to withdraw
     * @return true if the withdrawal was successful, false if insufficient funds
     * @throws IllegalArgumentException if amount is negative or zero
     */
    public boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        lock.lock();
        try {
            // Critical section: check and update balance atomically
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Attempts to transfer the specified amount from this account to the destination account.
     * This method acquires locks on both accounts to prevent deadlock by using a consistent
     * locking order based on account IDs.
     *
     * @param destination The account to transfer funds to
     * @param amount The amount to transfer
     * @return true if the transfer was successful, false if insufficient funds
     * @throws IllegalArgumentException if amount is negative or zero
     */
    public boolean transferTo(BankAccount destination, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        if (this.accountId == destination.accountId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Prevent deadlocks by always acquiring locks in a consistent order (lowest ID first)
        BankAccount firstLock = this.accountId < destination.accountId ? this : destination;
        BankAccount secondLock = this.accountId < destination.accountId ? destination : this;
        
        firstLock.lock.lock();
        try {
            secondLock.lock.lock();
            try {
                // Critical section: ensure atomic operation across two accounts
                if (this.balance >= amount) {
                    this.balance -= amount;
                    destination.balance += amount;
                    return true;
                }
                return false;
            } finally {
                secondLock.lock.unlock();
            }
        } finally {
            firstLock.lock.unlock();
        }
    }
}