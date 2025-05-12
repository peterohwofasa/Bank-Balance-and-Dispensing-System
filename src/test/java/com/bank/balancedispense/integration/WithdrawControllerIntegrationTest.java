package com.bank.balancedispense.integration;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class WithdrawControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test a successful withdrawal when ATM, client, and account exist and have sufficient balance.
     */
    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 500.0, 1L);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.account.accountNumber").value("TX12345"))
                .andExpect(jsonPath("$.denomination").isArray())
                .andExpect(jsonPath("$.result.success").value(true));
    }

    /**
     * Test a withdrawal with an invalid (negative) amount.
     * Should still return 200 but with an appropriate error response body (depends on validation strategy).
     */
    @Test
    void shouldFailWithdrawalWhenAmountIsNegative() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", -100.0, 1L);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.statusReason", containsString("Validation failed")));
    }

    /**
     * Test withdrawal with an unknown ATM ID.
     */
    @Test
    void shouldFailWhenATMNotFound() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 500.0, 999L);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusReason", containsString("ATM not registered")));
    }

    /**
     * Test withdrawal with a nonexistent account.
     */
    @Test
    void shouldFailWhenAccountNotFound() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "NONEXIST123", 100.0, 1L);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusReason", containsString("not found")));
    }

    /**
     * Test withdrawal that exceeds available balance + overdraft.
     */
    @Test
    void shouldFailWhenInsufficientFunds() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 50000.0, 1L);

        mockMvc.perform(post("/discovery-atm/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusReason", containsString("Insufficient funds")));
    }
}
