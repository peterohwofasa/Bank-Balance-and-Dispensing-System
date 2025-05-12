package com.bank.balancedispense.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a standard API error structure returned to clients when an exception occurs.
 * Provides consistent messaging for validation errors, business rule violations, and system failures.
 */
@Schema(description = "Standard structure for API error responses")
public record ErrorResponse(

        @Schema(
                description = "Indicates if the operation was successful",
                example = "false"
        )
        boolean success,

        @Schema(
                description = "HTTP status code corresponding to the error",
                example = "400"
        )
        int statusCode,

        @Schema(
                description = "Human-readable description of the error",
                example = "Insufficient funds"
        )
        String statusReason
) {}
