// Updated BalanceControllerTest to assert structured error response on NoAccountsFoundException
package com.bank.balancedispense.controller;

import com.bank.balancedispense.controllers.BalanceController;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.services.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link BalanceController} using MockMvc.
 * Validates correct behavior for balance retrieval and error responses.
 */
@WebMvcTest(BalanceController.class)
public class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BalanceService balanceService;

    @Test
    void testGetTransactionalBalances() throws Exception {
        ClientDto client = new ClientDto(1L, "Mr", "John", "Doe");
        TransactionalAccountDto account = new TransactionalAccountDto(
                "TX12345", "TRANSACTIONAL", "Main Account",
                "ZAR", BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(2000.0), BigDecimal.valueOf(2000.0), BigDecimal.ZERO
        );
        ResultDto result = new ResultDto(true, 200, "Success");
        TransactionalBalanceResponseWrapper wrapper = new TransactionalBalanceResponseWrapper(
                client,
                List.of(account),
                result
        );

        when(balanceService.getTransactionalBalances(1L)).thenReturn(wrapper);

        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances?clientId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.name").value("John"))
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("TX12345"))
                .andExpect(jsonPath("$.accounts[0].zarBalance").value(2000.0))
                .andExpect(jsonPath("$.result.success").value(true));
    }

    @Test
    void testGetCurrencyBalances() throws Exception {
        ClientDto client = new ClientDto(1L, "Mr", "John", "Doe");
        List<CurrencyBalanceResponse> accounts = List.of(
                new CurrencyBalanceResponse(
                        "FX12345",
                        "CURRENCY",
                        "Currency Account",
                        "USD",
                        BigDecimal.valueOf(18.5),
                        BigDecimal.valueOf(100.0),
                        BigDecimal.valueOf(1850.0),
                        BigDecimal.ZERO
                )
        );

        ResultDto result = new ResultDto(true, 200, "Currency balances retrieved successfully");
        CurrencyBalanceResponseWrapper wrapper = new CurrencyBalanceResponseWrapper(client, accounts, result);

        when(balanceService.getCurrencyBalances(1L)).thenReturn(wrapper);

        mockMvc.perform(get("/discovery-atm/queryCcyBalances?clientId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.name").value("John"))
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("FX12345"))
                .andExpect(jsonPath("$.accounts[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$.accounts[0].balance").value(100.0))
                .andExpect(jsonPath("$.accounts[0].zarBalance").value(1850.0))
                .andExpect(jsonPath("$.result.success").value(true))
                .andExpect(jsonPath("$.result.statusCode").value(200));
    }

    @Test
    void testGetTransactionalBalances_notFound() throws Exception {
        when(balanceService.getTransactionalBalances(999L))
                .thenThrow(new NoAccountsFoundException("No transactional accounts found."));

        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances?clientId=999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.statusReason").value("No transactional accounts found."));
    }

    @Test
    void testGetTransactionalBalances_invalidClientId_shouldFailValidation() throws Exception {
        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingServletRequestParameterException))
                .andExpect(result -> assertTrue(result.getResolvedException().getMessage().contains("clientId")));
    }
}
