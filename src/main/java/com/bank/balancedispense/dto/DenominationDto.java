package com.bank.balancedispense.dto;

/**
 * DTO representing a single denomination used in cash withdrawals.
 * This includes the denomination ID, the currency value (e.g., 200), and the quantity dispensed.
 */
public record DenominationDto(
        Long denominationId,
        int denominationValue,
        int count
) {}
