package com.bank.balancedispense.exceptions;

/**
 * Thrown when an ATM is not found or is inactive during a withdrawal operation.
 */
public class ATMNotFoundException extends RuntimeException {
    public ATMNotFoundException(String message) {
        super(message);
    }
}