package com.bank.balancedispense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for performing a cash withdrawal via ATM.
 * <p>
 * This record ensures the basic structure and validation of the input
 * required to initiate a withdrawal transaction.
 */
public record WithdrawRequest(

        @NotNull(message = "Client ID is required")
        Long clientId,

        @NotNull(message = "Account number is required")
        @Size(min = 1, message = "Account number must not be empty")
        String accountNumber,

        /**
         * The amount of money the client wishes to withdraw.
         * Must be a positive number greater than 0.
         */
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        Double amount,

        @NotNull(message = "ATM ID is required")
        Long atmId
) {}
