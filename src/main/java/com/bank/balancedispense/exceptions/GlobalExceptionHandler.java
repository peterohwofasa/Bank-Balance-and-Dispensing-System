package com.bank.balancedispense.exceptions;

import com.bank.balancedispense.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for REST APIs.
 *
 * This class handles all known custom and framework exceptions and formats them
 * into a standard {@link ErrorResponse} payload for client consumption.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Handles missing account scenario. */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /** Handles ATM not found or inactive scenario. */
    @ExceptionHandler(ATMNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleATMNotFound(ATMNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /** Handles insufficient balance including overdraft limit. */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /** Handles no eligible accounts found (transactional/currency). */
    @ExceptionHandler(NoAccountsFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoAccountsFound(NoAccountsFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /** Handles field-level validation errors triggered by `@Valid`. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse("Validation failed: " + errors, HttpStatus.BAD_REQUEST);
    }

    /** Handles missing required HTTP request parameters. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        return buildErrorResponse("Missing required parameter: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    /** Handles illegal arguments (e.g., unsupported currency code). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse("Invalid request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /** Handles ATM withdrawal logic errors related to note dispensing. */
    @ExceptionHandler(NoteCalculationException.class)
    public ResponseEntity<ErrorResponse> handleNoteCalculation(NoteCalculationException ex) {
        String message = ex.getMessage();
        if (ex.getFallbackAmount() != null) {
            message += " Would you like to draw " + ex.getFallbackAmount() + "?";
        }
        log.warn("Note calculation failed. Fallback: {}", ex.getFallbackAmount());
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    /** Fallback handler for all unhandled exceptions. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildErrorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Utility method to build a consistent error response.
     *
     * @param message Human-readable error message
     * @param status  Corresponding HTTP status code
     * @return A formatted {@link ResponseEntity} with {@link ErrorResponse}
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(false, status.value(), message), status);
    }
}
