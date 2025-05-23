package com.bank.balancedispense.dto;

import java.math.BigDecimal;

/**
 * Response DTO for currency account balances with conversion details.
 * Matches the Version 2 spec response structure.
 */
public record CurrencyBalanceResponse(
        String accountNumber,
        String typeCode,
        String accountTypeDescription,
        String currencyCode,
        BigDecimal conversionRate,
        BigDecimal balance,
        BigDecimal zarBalance,
        BigDecimal accountLimit
) {}
