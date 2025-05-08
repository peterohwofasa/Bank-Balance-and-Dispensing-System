package com.bank.balancedispense.controller;

import com.bank.balancedispense.controllers.WithdrawController;
import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;
import com.bank.balancedispense.services.WithdrawService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for WithdrawController using MockMvc.
 * Validates successful withdrawal operations.
 */
@SuppressWarnings("removal")
@WebMvcTest(WithdrawController.class)
public class WithdrawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WithdrawService withdrawService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the /withdraw POST endpoint with valid request data.
     * Asserts the HTTP response contains the expected note breakdown and new balance.
     */
    @Test
    void testWithdrawEndpointSuccess() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);

        // Simulated response returned by the withdrawService
        WithdrawResponse mockResponse = new WithdrawResponse(Map.of(200, 1), 1800.0);

        // Mock withdraw behavior
        when(withdrawService.withdraw(any(WithdrawRequest.class))).thenReturn(mockResponse);


        // Execute POST request and assert returned JSON
        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBalance").value(1800.0))
                .andExpect(jsonPath("$.notesDispensed['200']").value(1));
    }
}