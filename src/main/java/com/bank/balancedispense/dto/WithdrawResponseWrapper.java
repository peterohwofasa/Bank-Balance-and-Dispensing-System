package com.bank.balancedispense.dto;

import java.util.List;

/**
 * Response DTO wrapper returned after a successful withdrawal operation.
 *
 * @param client        Basic client information (name, ID, etc.)
 * @param account       Account from which the withdrawal was made (includes updated balance)
 * @param denomination  List of denominations dispensed (e.g., 2x R200, 1x R100)
 * @param result        Standard API response metadata (success flag, status code, message)
 */
public record WithdrawResponseWrapper(
        ClientDto client,
        TransactionalAccountDto account,
        List<DenominationDto> denomination,
        ResultDto result
) {}