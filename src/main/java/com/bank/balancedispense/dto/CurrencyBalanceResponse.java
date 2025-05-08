package com.bank.balancedispense.dto;

/**
 * Response DTO for currency account balances.
 * Includes the original foreign balance and its converted value in ZAR.
 */
public record CurrencyBalanceResponse(
        String accountNumber,
        String description,
        double foreignBalance,
        double convertedToRand
) {}
