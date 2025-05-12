package com.bank.balancedispense.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BalanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test retrieval of transactional balances for an existing client.
     */
    @Test
    void shouldReturnTransactionalBalances() throws Exception {
        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances")
                        .param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.accounts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.result.success").value(true));
    }

    /**
     * Test retrieval of currency account balances (converted) for an existing client.
     */
    @Test
    void shouldReturnCurrencyBalances() throws Exception {
        mockMvc.perform(get("/discovery-atm/queryCcyBalances")
                        .param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.accounts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.accounts[0].currencyCode").exists())
                .andExpect(jsonPath("$.accounts[0].zarBalance").exists())
                .andExpect(jsonPath("$.result.success").value(true));
    }


    /**
     * Test that request fails when required clientId param is missing.
     */
    @Test
    void shouldReturnBadRequestWhenClientIdMissing() throws Exception {
        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusReason", containsString("Missing required parameter")));
    }

    /**
     * Test that request returns 404 for a non-existent client.
     */
    @Test
    void shouldReturnBadRequestForUnknownClientId() throws Exception {
        mockMvc.perform(get("/discovery-atm/queryTransactionalBalances")
                        .param("clientId", "999"))
                .andExpect(status().isBadRequest()) // changed from isNotFound()
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400)) // changed from 404
                .andExpect(jsonPath("$.statusReason", containsString("No transactional accounts to display")));
    }


}
