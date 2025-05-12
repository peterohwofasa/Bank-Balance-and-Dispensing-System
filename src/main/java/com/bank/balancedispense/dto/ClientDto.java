package com.bank.balancedispense.dto;

/**
 * A lightweight DTO used to carry client identification and display name information in responses.
 * Shared across all API responses (e.g., balance, withdrawal).
 */
public record ClientDto(
        Long id,
        String title,
        String name,
        String surname
) {}