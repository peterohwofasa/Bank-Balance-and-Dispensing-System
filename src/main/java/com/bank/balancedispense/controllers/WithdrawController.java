package com.bank.balancedispense.controllers;

import com.bank.balancedispense.dto.ErrorResponse;
import com.bank.balancedispense.dto.ResultDto;
import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponseWrapper;
import com.bank.balancedispense.exceptions.InsufficientFundsException;
import com.bank.balancedispense.exceptions.NoteCalculationException;
import com.bank.balancedispense.services.WithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST controller for handling ATM cash withdrawals.
 * Provides validation and structured responses for both success and failure.
 */
@RestController
@RequestMapping("/discovery-atm")
@Tag(name = "Withdrawal API", description = "Endpoint for ATM cash withdrawal operations")
public class WithdrawController {

    @Autowired
    private WithdrawService withdrawService;

    @Operation(
            summary = "Withdraw amount from transactional account",
            description = "Processes a cash withdrawal request by validating funds, calculating denominations, and updating account balance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(schema = @Schema(implementation = WithdrawResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or insufficient funds",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "ATM, client, or account not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/withdraw", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> withdraw(
            @Parameter(description = "Withdrawal request details", required = true)
            @RequestBody @Valid WithdrawRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(false, 400, "Validation failed: " + errors)
            );
        }

        WithdrawResponseWrapper response = withdrawService.withdraw(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles cases where withdrawal fails due to insufficient funds.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ResultDto> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.badRequest().body(
                new ResultDto(false, 400, ex.getMessage())
        );
    }

    /**
     * Handles note calculation failures and returns a fallback suggestion if available.
     */
    @ExceptionHandler(NoteCalculationException.class)
    public ResponseEntity<ResultDto> handleNoteCalculation(NoteCalculationException ex) {
        return ResponseEntity.badRequest().body(
                new ResultDto(false, 400, ex.getMessage(), ex.getFallbackAmount())
        );
    }
}
