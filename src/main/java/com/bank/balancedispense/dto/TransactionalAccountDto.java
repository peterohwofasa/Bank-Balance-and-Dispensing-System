package com.bank.balancedispense.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a client's transactional account details.
 * Used in balance queries and withdrawal response payloads.
 */
public record TransactionalAccountDto(
        String accountNumber,
        String typeCode,
        String accountTypeDescription,
        String currencyCode,
        BigDecimal conversionRate,
        BigDecimal balance,
        BigDecimal zarBalance,
        BigDecimal accountLimit
) {}