package com.bank.balancedispense.dto;

/**
 * Response DTO for transactional account balances.
 */
public record TransactionalBalanceResponse(
        String accountNumber,
        String description,
        double balance
) {}
