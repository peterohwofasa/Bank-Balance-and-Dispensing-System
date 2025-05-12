package com.bank.balancedispense.dto;

public record ResultDto(
        boolean success,
        int statusCode,
        String statusReason
) {}