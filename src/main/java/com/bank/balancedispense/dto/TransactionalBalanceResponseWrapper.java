package com.bank.balancedispense.dto;

import java.util.List;

/**
 * Wrapper for transactional balance API response including client info and status result.
 */
public record TransactionalBalanceResponseWrapper(
        ClientDto client,
        List<TransactionalAccountDto> accounts,
        ResultDto result
) {}
