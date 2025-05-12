package com.bank.balancedispense.controllers;

import com.bank.balancedispense.dto.CurrencyBalanceResponseWrapper;
import com.bank.balancedispense.dto.ErrorResponse;
import com.bank.balancedispense.dto.ResultDto;
import com.bank.balancedispense.dto.TransactionalBalanceResponseWrapper;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.services.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for retrieving client account balances.
 * Provides transactional and currency balance lookups.
 */
@RestController
@RequestMapping("/discovery-atm")
@Validated
@Tag(name = "Balance API", description = "Endpoints for retrieving account balances")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @Operation(
            summary = "Get all transactional balances for a client",
            description = "Returns all transactional accounts with available balances, sorted in descending order by balance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval",
                    content = @Content(schema = @Schema(implementation = TransactionalBalanceResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid client ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No accounts found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/queryTransactionalBalances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionalBalanceResponseWrapper> getTransactionalBalances(
            @Parameter(description = "Client ID", required = true, example = "12345")
            @RequestParam @Min(1) Long clientId) {
        return ResponseEntity.ok(balanceService.getTransactionalBalances(clientId));
    }

    @Operation(
            summary = "Get all currency balances for a client with converted Rand values",
            description = "Returns all currency accounts with original and ZAR-converted balances, sorted by ZAR amount."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval",
                    content = @Content(schema = @Schema(implementation = CurrencyBalanceResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid client ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No accounts found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/queryCcyBalances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CurrencyBalanceResponseWrapper> getCurrencyBalances(
            @Parameter(description = "Client ID", required = true, example = "12345")
            @RequestParam @Min(1) Long clientId) {
        return ResponseEntity.ok(balanceService.getCurrencyBalances(clientId));
    }

    /**
     * Handles cases where no qualifying accounts were found for a client.
     */
    @ExceptionHandler(NoAccountsFoundException.class)
    public ResponseEntity<ResultDto> handleNoAccountsFound(NoAccountsFoundException ex) {
        return ResponseEntity.badRequest().body(
                new ResultDto(false, 400, ex.getMessage())
        );
    }
}
