package com.bank.balancedispense.dto;

public record ClientDto(
        Long id,
        String title,
        String name,
        String surname
) {}