package com.bank.balancedispense.common;

/**
 * Enum representing common error messages used across the application.
 */
public enum ErrorMessage {
    ACCOUNT_NOT_FOUND("Account not found"),
    ATM_NOT_FOUND("ATM not registered or not active"),
    INSUFFICIENT_FUNDS("Insufficient funds"),
    NOTE_CALCULATION_FAILED("Amount cannot be dispensed. Try a different amount.");

    private final String message;
    ErrorMessage(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
