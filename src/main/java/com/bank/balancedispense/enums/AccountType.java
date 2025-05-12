package com.bank.balancedispense.enums;

/**
 * Represents supported types of bank accounts, with human-readable descriptions.
 */
public enum AccountType {
    TRANSACTIONAL("Transactional Account"),
    CURRENCY("Currency Account"),
    LOAN("Loan Account");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
