package com.bank.balancedispense.exceptions;


/**
 * Thrown when no accounts of a specified type (e.g., TRANSACTIONAL or CURRENCY) are found for a client.
 * Used in balance retrieval endpoints.
 */
public class NoAccountsFoundException extends RuntimeException {
    public NoAccountsFoundException(String message) {
        super(message);
    }
}

