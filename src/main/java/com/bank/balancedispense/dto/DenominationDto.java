package com.bank.balancedispense.dto;

public record DenominationDto(
        Long denominationId,
        int denominationValue,
        int count
) {}
