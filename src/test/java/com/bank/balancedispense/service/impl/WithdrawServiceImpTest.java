package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.*;
import com.bank.balancedispense.exceptions.*;
import com.bank.balancedispense.repository.*;
import com.bank.balancedispense.services.impl.WithdrawServiceImpl;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WithdrawServiceImpTest {

    @Mock private ClientAccountRepository accountRepo;
    @Mock private ATMAllocationRepository atmNoteRepo;
    @Mock private ATMRepository atmRepo;
    @Mock private ClientRepository clientRepo;

    private WithdrawServiceImpl withdrawService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        CurrencyConversionRateRepository mockRateRepo = mock(CurrencyConversionRateRepository.class);
        CurrencyConversionUtil currencyUtil = new CurrencyConversionUtil(mockRateRepo);
        withdrawService = new WithdrawServiceImpl(accountRepo, atmNoteRepo, atmRepo, clientRepo, currencyUtil);

        CurrencyConversionRate rate = new CurrencyConversionRate();
        rate.setCurrencyCode("ZAR");
        rate.setConversionIndicator("/");
        rate.setRate(BigDecimal.ONE);

        when(mockRateRepo.findById("ZAR")).thenReturn(Optional.of(rate));
    }

    @Test
    void withdraw_shouldThrowATMNotFoundException_ifATMNotFound() {
        when(atmRepo.findById(1L)).thenReturn(Optional.empty());
        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 200.0, 1L);
        assertThrows(ATMNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void withdraw_shouldThrowAccountNotFound_ifNoAccount() {
        when(atmRepo.findById(1L)).thenReturn(Optional.of(new ATM(1L, "ATM", true)));
        when(accountRepo.findByClient_IdAndAccountNumber(1L, "TX123")).thenReturn(Optional.empty());
        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 200.0, 1L);
        assertThrows(AccountNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void withdraw_shouldThrowInsufficientFundsException_whenFundsTooLow() {
        ATM atm = new ATM(1L, "ATM", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        ClientAccount acc = new ClientAccount();
        acc.setAccountNumber("TX123");
        acc.setDisplayBalance(BigDecimal.valueOf(-9500.0));
        acc.setAccountType(new AccountType("CHQ", "Cheque Account", true));
        acc.setCurrency(new Currency("ZAR", 2, "Rand"));

        when(accountRepo.findByClient_IdAndAccountNumber(1L, "TX123")).thenReturn(Optional.of(acc));

        ATMAllocation note = new ATMAllocation();
        Denomination denom = new Denomination();
        denom.setValue(BigDecimal.valueOf(100));
        note.setDenomination(denom);
        note.setQuantity(20);
        when(atmNoteRepo.findByAtm_Id(1L)).thenReturn(List.of(note));

        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 1000.0, 1L);
        assertThrows(InsufficientFundsException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void withdraw_shouldThrowNoteCalculationException_withoutFallbackAmount() {
        Long clientId = 1L;
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 100.0, 1L);

        ATM atm = new ATM(1L, "ATM", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        ClientAccount acc = new ClientAccount();
        acc.setAccountNumber("TX12345");
        acc.setDisplayBalance(BigDecimal.valueOf(1000.0));
        acc.setCurrency(new Currency("ZAR", 2, "Rand"));
        acc.setAccountType(new AccountType("CHQ", "Cheque Account", true));
        acc.setClient(new Client(1L, "Mr", "John", "Doe"));

        when(accountRepo.findByClient_IdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(acc));
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(acc.getClient()));

        ATMAllocation note50 = new ATMAllocation();
        Denomination denom50 = new Denomination();
        denom50.setValue(BigDecimal.valueOf(50));
        note50.setDenomination(denom50);
        note50.setQuantity(0);

        when(atmNoteRepo.findByAtm_Id(1L)).thenReturn(List.of(note50));

        NoteCalculationException ex = assertThrows(NoteCalculationException.class, () -> withdrawService.withdraw(request));
        assertTrue(ex.getMessage().contains("Amount cannot be dispensed"));
        assertNull(ex.getFallbackAmount());
    }

    @Test
    void withdraw_shouldReturnWrappedResponse_whenSuccessful() {
        Long clientId = 1L;
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 200.0, 1L);

        ATM atm = new ATM(1L, "Branch", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        ClientAccount account = new ClientAccount();
        account.setAccountNumber("TX12345");
        account.setDisplayBalance(BigDecimal.valueOf(1000.0));
        account.setCurrency(new Currency("ZAR", 2, "Rand"));
        account.setAccountType(new AccountType("CHQ", "Cheque Account", true));

        Client client = new Client(1L, "Mr", "John", "Doe");
        account.setClient(client);

        when(accountRepo.findByClient_IdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(account));
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        Denomination denomination = new Denomination();
        denomination.setId(1L);
        denomination.setValue(BigDecimal.valueOf(200));

        ATMAllocation alloc = new ATMAllocation();
        alloc.setId(1L);
        alloc.setQuantity(5);
        alloc.setDenomination(denomination);

        when(atmNoteRepo.findByAtm_Id(1L)).thenReturn(List.of(alloc));

        WithdrawResponseWrapper response = withdrawService.withdraw(request);

        assertEquals("John", response.client().name());
        assertEquals("TX12345", response.account().accountNumber());
        assertEquals(200, response.denomination().get(0).denominationValue());
        assertEquals(1, response.denomination().get(0).count());
        assertTrue(response.result().success());

        verify(accountRepo).save(any(ClientAccount.class));
    }

    @Test
    void withdraw_shouldSuggestFallback_whenExactNotPossible() {
        Long clientId = 1L;
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 300.0, 1L);

        ATM atm = new ATM(1L, "ATM", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        ClientAccount acc = new ClientAccount();
        acc.setAccountNumber("TX12345");
        acc.setDisplayBalance(BigDecimal.valueOf(1000.0));
        acc.setCurrency(new Currency("ZAR", 2, "Rand"));
        acc.setAccountType(new AccountType("CHQ", "Cheque Account", true));
        acc.setClient(new Client(1L, "Mr", "John", "Doe"));

        when(accountRepo.findByClient_IdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(acc));
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(acc.getClient()));

        Denomination note200 = new Denomination(1L, BigDecimal.valueOf(200));
        Denomination note50 = new Denomination(2L, BigDecimal.valueOf(50));

        ATMAllocation alloc200 = new ATMAllocation();
        alloc200.setDenomination(note200);
        alloc200.setQuantity(1);

        ATMAllocation alloc50 = new ATMAllocation();
        alloc50.setDenomination(note50);
        alloc50.setQuantity(1);

        when(atmNoteRepo.findByAtm_Id(1L)).thenReturn(List.of(alloc200, alloc50));

        NoteCalculationException ex = assertThrows(NoteCalculationException.class, () -> withdrawService.withdraw(request));
        assertEquals(250, ex.getFallbackAmount());
    }

}
