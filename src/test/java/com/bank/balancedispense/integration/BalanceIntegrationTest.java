package com.bank.balancedispense.integration;

import com.bank.balancedispense.dto.CurrencyBalanceResponse;
import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.entities.ATM;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.repository.ATMRepository;
import com.bank.balancedispense.repository.ATMNoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ATMRepository atmRepository;

    @Autowired
    private ATMNoteRepository atmNoteRepository;

    @BeforeEach
    void setupData() {
        Account transactional = new Account();
        transactional.setId(1L);
        transactional.setClientId(1L);
        transactional.setAccountNumber("TX12345");
        transactional.setDescription("Transactional Account");
        transactional.setBalance(2000.0);
        transactional.setAccountType(AccountType.TRANSACTIONAL);
        transactional.setCurrency(Currency.ZAR);

        Account currency = new Account();
        currency.setId(2L);
        currency.setClientId(1L);
        currency.setAccountNumber("FX12345");
        currency.setDescription("USD Wallet");
        currency.setBalance(100.0);
        currency.setAccountType(AccountType.CURRENCY);
        currency.setCurrency(Currency.USD);

        ATM atm = new ATM();
        atm.setId(1L);
        atm.setLocation("Test Location");
        atm.setActive(true);

        ATMNote note = new ATMNote();
        note.setId(1L);
        note.setAtmId(1L);
        note.setDenomination(200);
        note.setQuantity(10);

        accountRepository.save(transactional);
        accountRepository.save(currency);
        atmRepository.save(atm);
        atmNoteRepository.save(note);
    }

    @Test
    @Operation(summary = "Get transactional balances",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of transactional accounts",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TransactionalBalanceResponse.class)))
            })
    void testGetTransactionalBalances_success() throws Exception {
        mockMvc.perform(get("/balances/transactional")
                        .param("clientId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("TX12345"))
                .andExpect(jsonPath("$[0].balance").value(2000.0));
    }

    @Test
    @Operation(summary = "Get currency balances",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of currency accounts with ZAR conversion",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CurrencyBalanceResponse.class)))
            })
    void testGetCurrencyBalances_success() throws Exception {
        mockMvc.perform(get("/balances/currency")
                        .param("clientId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("FX12345"))
                .andExpect(jsonPath("$[0].convertedToRand").value(1850.0)); // 100 * 18.5
    }

    @Test
    @Operation(summary = "Transactional balances not found",
            responses = {
                    @ApiResponse(responseCode = "404", description = "No transactional accounts found")
            })
    void testGetTransactionalBalances_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/balances/transactional")
                        .param("clientId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Operation(summary = "Currency balances not found",
            responses = {
                    @ApiResponse(responseCode = "404", description = "No currency accounts found")
            })
    void testGetCurrencyBalances_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/balances/currency")
                        .param("clientId", "999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Operation(summary = "Withdraw funds",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful withdrawal",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = WithdrawResponse.class)))
            })
    void testWithdraw_success() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);

        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBalance").value(1800.0))
                .andExpect(jsonPath("$.notesDispensed['200']").value(1));
    }
}
