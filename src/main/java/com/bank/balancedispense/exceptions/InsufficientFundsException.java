package com.bank.balancedispense.exceptions;

/**
 * Thrown when a withdrawal is attempted on an account with insufficient balance.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {

        super(message);
    }
}