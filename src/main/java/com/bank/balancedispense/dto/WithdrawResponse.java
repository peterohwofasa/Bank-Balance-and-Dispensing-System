package com.bank.balancedispense.dto;

import java.util.Map;

/**
 * Response DTO for withdrawal results.
 * Contains map of dispensed notes and the new account balance.
 */
public record WithdrawResponse(
        Map<Integer, Integer> notesDispensed,
        double newBalance
) {}
