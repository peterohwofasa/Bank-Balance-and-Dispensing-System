package com.bank.balancedispense.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
