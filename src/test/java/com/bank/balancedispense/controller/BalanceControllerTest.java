package com.bank.balancedispense.controller;

import com.bank.balancedispense.controllers.BalanceController;
import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.dto.CurrencyBalanceResponse;
import com.bank.balancedispense.services.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BalanceController using MockMvc.
 * Tests both transactional and currency balance endpoints.
 */
@SuppressWarnings("removal")
@WebMvcTest(BalanceController.class)
public class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BalanceService balanceService;

    /**
     * Tests the /balances/transactional endpoint for a successful response.
     * Validates JSON structure and field values.
     */
    @Test
    void testGetTransactionalBalances() throws Exception {
        List<TransactionalBalanceResponse> responses = List.of(
                new TransactionalBalanceResponse("TX12345", "Main Account", 2000.0)
        );

        // Mock the service method call
        when(balanceService.getTransactionalBalances(1L)).thenReturn(responses);

        // Perform GET request and validate response fields
        mockMvc.perform(get("/balances/transactional?clientId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("TX12345"))
                .andExpect(jsonPath("$[0].description").value("Main Account"))
                .andExpect(jsonPath("$[0].balance").value(2000.0));
    }

    /**
     * Tests the /balances/currency endpoint for a successful response.
     * Ensures converted values are returned as expected.
     */
    @Test
    void testGetCurrencyBalances() throws Exception {
        List<CurrencyBalanceResponse> responses = List.of(
                new CurrencyBalanceResponse("FX12345", "USD Account", 100.0, 1850.0)
        );

        // Mock the service call
        when(balanceService.getCurrencyBalances(1L)).thenReturn(responses);

        // Perform GET request and check response structure and content
        mockMvc.perform(get("/balances/currency?clientId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("FX12345"))
                .andExpect(jsonPath("$[0].description").value("USD Account"))
                .andExpect(jsonPath("$[0].foreignBalance").value(100.0))
                .andExpect(jsonPath("$[0].convertedToRand").value(1850.0));
    }
}

