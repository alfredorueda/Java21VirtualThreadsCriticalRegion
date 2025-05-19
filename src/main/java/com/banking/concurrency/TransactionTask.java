package com.banking.concurrency;

/**
 * Represents a banking transaction task to be executed by a virtual thread.
 * 
 * This class encapsulates the different types of banking operations (deposit, withdraw, transfer)
 * that can be performed concurrently in the banking system.
 */
public class TransactionTask implements Runnable {

    /**
     * Defines the supported transaction types in the banking system.
     */
    public enum TransactionType {
        DEPOSIT,
        WITHDRAW,
        TRANSFER
    }

    private final TransactionType type;
    private final BankAccount sourceAccount;
    private final BankAccount destinationAccount;  // Only used for transfers
    private final double amount;

    /**
     * Creates a deposit or withdrawal transaction task.
     *
     * @param type The transaction type (DEPOSIT or WITHDRAW)
     * @param account The account to operate on
     * @param amount The amount to deposit or withdraw
     * @throws IllegalArgumentException if type is TRANSFER or amount is not positive
     */
    public TransactionTask(TransactionType type, BankAccount account, double amount) {
        if (type == TransactionType.TRANSFER) {
            throw new IllegalArgumentException("For transfers, use the constructor with source and destination accounts");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        this.type = type;
        this.sourceAccount = account;
        this.destinationAccount = null;
        this.amount = amount;
    }

    /**
     * Creates a transfer transaction task.
     *
     * @param source The account to transfer from
     * @param destination The account to transfer to
     * @param amount The amount to transfer
     * @throws IllegalArgumentException if amount is not positive
     */
    public TransactionTask(BankAccount source, BankAccount destination, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        this.type = TransactionType.TRANSFER;
        this.sourceAccount = source;
        this.destinationAccount = destination;
        this.amount = amount;
    }

    /**
     * Executes this transaction task based on its type.
     * This method will be run by a virtual thread when scheduled.
     */
    @Override
    public void run() {
        try {
            switch (type) {
                case DEPOSIT -> sourceAccount.deposit(amount);
                case WITHDRAW -> sourceAccount.withdraw(amount);
                case TRANSFER -> {
                    if (destinationAccount == null) {
                        throw new IllegalStateException("Destination account cannot be null for transfers");
                    }
                    sourceAccount.transferTo(destinationAccount, amount);
                }
            }
        } catch (Exception e) {
            System.err.println("Transaction failed: " + e.getMessage());
            // In a production system, we'd use proper logging and potentially retry logic
        }
    }

    /**
     * Gets the transaction type.
     * 
     * @return The type of this transaction
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Gets the transaction amount.
     * 
     * @return The amount involved in this transaction
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the source account for this transaction.
     * 
     * @return The account being operated on (or source account for transfers)
     */
    public BankAccount getSourceAccount() {
        return sourceAccount;
    }

    /**
     * Gets the destination account for transfer transactions.
     * 
     * @return The destination account for transfers, or null for other transaction types
     */
    public BankAccount getDestinationAccount() {
        return destinationAccount;
    }
}