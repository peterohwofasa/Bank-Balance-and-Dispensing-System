package com.bank.balancedispense.dto;

import java.math.BigDecimal;

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