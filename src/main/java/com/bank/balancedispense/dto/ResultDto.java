package com.bank.balancedispense.dto;

/**
 * Represents a standard metadata response block included in all successful and failed API responses.
 * Contains outcome status, HTTP code, and optional fallback data (e.g., for note dispensing).
 */
public record ResultDto(
        boolean success,
        int statusCode,
        String statusReason,
        Integer fallbackAmount // Nullable: used only when fallback suggestions apply (e.g., withdrawals)
) {
    /**
     * Convenience constructor without fallback value for general success/failure responses.
     */
    public ResultDto(boolean success, int statusCode, String statusReason) {
        this(success, statusCode, statusReason, null);
    }
}
