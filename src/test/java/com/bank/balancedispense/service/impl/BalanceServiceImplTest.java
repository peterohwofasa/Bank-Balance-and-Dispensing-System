package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.dto.CurrencyBalanceResponse;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.services.impl.BalanceServiceImpl;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BalanceServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyConversionUtil currencyConversionUtil;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransactionalBalances_success() {
        Account acc = new Account();
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setBalance(2000.0);
        acc.setAccountNumber("TX123");
        acc.setDescription("Main");

        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.TRANSACTIONAL))
                .thenReturn(List.of(acc));

        List<TransactionalBalanceResponse> responses = balanceService.getTransactionalBalances(1L);
        assertEquals(1, responses.size());
        assertEquals("TX123", responses.get(0).accountNumber());
    }

    @Test
    void testGetCurrencyBalances_success() {
        Account acc = new Account();
        acc.setAccountType(AccountType.CURRENCY);
        acc.setBalance(100.0);
        acc.setCurrency(Currency.USD);
        acc.setAccountNumber("FX123");
        acc.setDescription("USD Wallet");

        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.CURRENCY))
                .thenReturn(List.of(acc));
        when(currencyConversionUtil.getConversionRate(Currency.USD)).thenReturn(18.5);

        List<CurrencyBalanceResponse> responses = balanceService.getCurrencyBalances(1L);
        assertEquals(1, responses.size());
        assertEquals("FX123", responses.get(0).accountNumber());
        assertEquals(1850.0, responses.get(0).convertedToRand());
    }

    @Test
    void testGetTransactionalBalances_empty_shouldThrow() {
        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.TRANSACTIONAL))
                .thenReturn(List.of());

        assertThrows(NoAccountsFoundException.class, () -> balanceService.getTransactionalBalances(1L));
    }

    @Test
    void testGetCurrencyBalances_empty_shouldThrow() {
        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.CURRENCY))
                .thenReturn(List.of());

        assertThrows(NoAccountsFoundException.class, () -> balanceService.getCurrencyBalances(1L));
    }
}
