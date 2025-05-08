package com.bank.balancedispense.controllers;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;
import com.bank.balancedispense.services.WithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for processing withdrawal requests.
 */
@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

    @Autowired
    private WithdrawService withdrawService;

    /**
     * Endpoint to perform a withdrawal operation from an ATM.
     * Validates the request before processing.
     */
    @Operation(summary = "Withdraw amount from transactional account")
    @ApiResponse(responseCode = "200", description = "Withdrawal successful")
    @PostMapping
    public ResponseEntity<?> withdraw(@RequestBody @Valid WithdrawRequest request, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "details", errors));
        }
        return ResponseEntity.ok(withdrawService.withdraw(request));
    }
}
