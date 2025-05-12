package com.bank.balancedispense.dto;

import java.util.List;

/**
 * Wrapper for currency balance API response including client info and result metadata.
 */
public record CurrencyBalanceResponseWrapper(
        ClientDto client,
        List<CurrencyBalanceResponse> accounts,
        ResultDto result
) {}
