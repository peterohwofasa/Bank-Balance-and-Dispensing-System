package com.bank.balancedispense.controllers;

import com.bank.balancedispense.dto.CurrencyBalanceResponse;
import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.services.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for retrieving client balances.
 * Supports both transactional and foreign currency balances.
 */
@RestController
@RequestMapping("/balances")
@Validated
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    /**
     * Endpoint to retrieve all transactional accounts for a given client.
     * @param clientId Client ID (must be >= 1)
     */
    @Operation(summary = "Get all transactional balances for a client")
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @GetMapping("/transactional")
    public List<TransactionalBalanceResponse> getTransactionalBalances(@RequestParam @Min(1) Long clientId) {
        return balanceService.getTransactionalBalances(clientId);
    }

    /**
     * Endpoint to retrieve all currency accounts with ZAR conversion.
     * @param clientId Client ID (must be >= 1)
     */
    @Operation(summary = "Get all currency balances for a client with converted Rand values")
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @GetMapping("/currency")
    public List<CurrencyBalanceResponse> getCurrencyBalances(@RequestParam @Min(1) Long clientId) {
        return balanceService.getCurrencyBalances(clientId);
    }
}
