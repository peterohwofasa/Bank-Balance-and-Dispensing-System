package com.bank.balancedispense.integration;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.entities.ATM;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.repository.ATMNoteRepository;
import com.bank.balancedispense.repository.ATMRepository;
import com.bank.balancedispense.repository.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional; // ✅ Correct


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WithdrawIntegrationTest {

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
        ATM atm = new ATM();
        atm.setId(1L);
        atm.setLocation("Main Branch");
        atm.setActive(true);
        atmRepository.save(atm);

        ATMNote note = new ATMNote();
        note.setId(1L);
        note.setAtmId(1L);
        note.setDenomination(200);
        note.setQuantity(10);
        atmNoteRepository.save(note);

        Account acc = new Account();
        acc.setId(1L);
        acc.setClientId(1L);
        acc.setAccountNumber("TX12345");
        acc.setDescription("Main Transaction Account");
        acc.setBalance(2000.0);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setCurrency(Currency.ZAR);
        accountRepository.save(acc);
    }

    @Test
    void testWithdrawSuccess() throws Exception {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);

        mockMvc.perform(post("/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newBalance").value(1800.0))
                .andExpect(jsonPath("$.notesDispensed['200']").value(1));
    }
}
