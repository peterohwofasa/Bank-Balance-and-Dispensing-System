package com.bank.balancedispense.exceptions;

import lombok.Getter;

/**
 * Thrown when a requested withdrawal amount cannot be dispensed
 * using the available notes in the ATM.
 *
 * Optionally includes a fallback amount that could be dispensed instead.
 */
@Getter
public class NoteCalculationException extends RuntimeException {

    /** Suggested fallback amount if the requested amount can't be dispensed. */
    private final Integer fallbackAmount;

    /**
     * Constructor for standard message-only exception.
     *
     * @param message Error message
     */
    public NoteCalculationException(String message) {
        super(message);
        this.fallbackAmount = null;
    }

    /**
     * Constructor with message and fallback suggestion.
     *
     * @param message         Error message
     * @param fallbackAmount  Suggested lower amount that can be dispensed
     */
    public NoteCalculationException(String message, Integer fallbackAmount) {
        super(message);
        this.fallbackAmount = fallbackAmount;
    }
}
