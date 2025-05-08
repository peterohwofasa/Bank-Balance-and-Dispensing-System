package com.bank.balancedispense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Request DTO for performing a withdrawal.
 * Includes client ID, account number, amount to withdraw, and ATM ID.
 */
public record WithdrawRequest(
        @NotNull Long clientId,
        @NotNull @Size(min = 1) String accountNumber,
        @NotNull double amount,
        @NotNull Long atmId
) {}