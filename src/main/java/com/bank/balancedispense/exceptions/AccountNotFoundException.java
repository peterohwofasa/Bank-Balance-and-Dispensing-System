package com.bank.balancedispense.exceptions;

/**
 * Thrown when a requested account cannot be found for the given client ID or account number.
 * Typically used in balance or withdrawal operations.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {

        super(message);
    }
}
