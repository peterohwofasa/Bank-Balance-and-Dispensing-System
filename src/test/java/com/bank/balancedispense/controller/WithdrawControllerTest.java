// Fully updated WithdrawControllerTest with support for fallback and error cases
package com.bank.balancedispense.controller;

import com.bank.balancedispense.controllers.WithdrawController;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.exceptions.InsufficientFundsException;
import com.bank.balancedispense.exceptions.NoteCalculationException;
import com.bank.balancedispense.services.WithdrawService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for WithdrawController using MockMvc.
 * Validates successful withdrawal operations and error scenarios.
 */
@WebMvcTest(WithdrawController.class)
public class WithdrawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WithdrawService withdrawService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testWithdrawEndpointSuccess() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);

        ClientDto client = new ClientDto(1L, "Mr", "John", "Doe");
        TransactionalAccountDto account = new TransactionalAccountDto(
                "TX12345",
                "TRANSACTIONAL",
                "Transactional Account",
                "ZAR",
                BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(1800.0),
                BigDecimal.valueOf(1800.0),
                BigDecimal.ZERO
        );
        List<DenominationDto> denomination = List.of(
                new DenominationDto(1L, 200, 1)
        );
        ResultDto result = new ResultDto(true, 200, "Withdrawal successful");
        WithdrawResponseWrapper mockResponse = new WithdrawResponseWrapper(client, account, denomination, result);

        when(withdrawService.withdraw(any(WithdrawRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.name").value("John"))
                .andExpect(jsonPath("$.client.surname").value("Doe"))
                .andExpect(jsonPath("$.account.accountNumber").value("TX12345"))
                .andExpect(jsonPath("$.account.currencyCode").value("ZAR"))
                .andExpect(jsonPath("$.denomination[0].denominationValue").value(200))
                .andExpect(jsonPath("$.denomination[0].count").value(1))
                .andExpect(jsonPath("$.result.success").value(true))
                .andExpect(jsonPath("$.result.statusCode").value(200))
                .andExpect(jsonPath("$.result.statusReason").value("Withdrawal successful"));
    }

    @Test
    void testWithdrawEndpointFailsWithInsufficientFunds() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 10000.0, 1L);

        when(withdrawService.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds."));

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.statusReason").value("Insufficient funds."))
                .andExpect(jsonPath("$.fallbackAmount").doesNotExist());
    }

    @Test
    void testWithdrawEndpointFailsWithFallbackSuggestion() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 300.0, 1L);

        when(withdrawService.withdraw(any(WithdrawRequest.class)))
                .thenThrow(new NoteCalculationException("Amount cannot be dispensed", 250));

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.statusReason").value("Amount cannot be dispensed"))
                .andExpect(jsonPath("$.fallbackAmount").value(250));
    }
}
