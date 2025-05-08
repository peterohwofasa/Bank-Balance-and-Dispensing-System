package com.bank.balancedispense.exceptions;

/**
 * Thrown when the ATM cannot dispense the requested amount using available note denominations.
 */
public class NoteCalculationException extends RuntimeException {
    public NoteCalculationException(String message) {
        super(message);
    }
}