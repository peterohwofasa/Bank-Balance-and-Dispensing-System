package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.CurrencyBalanceResponseWrapper;
import com.bank.balancedispense.dto.TransactionalBalanceResponseWrapper;
import com.bank.balancedispense.entities.*;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.ClientAccountRepository;
import com.bank.balancedispense.repository.ClientRepository;
import com.bank.balancedispense.repository.CurrencyConversionRateRepository;
import com.bank.balancedispense.services.impl.BalanceServiceImpl;
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

public class BalanceServiceImplTest {

    @Mock private ClientAccountRepository accountRepository;
    @Mock private ClientRepository clientRepository;

    private CurrencyConversionUtil currencyUtil;
    private BalanceServiceImpl balanceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        CurrencyConversionRateRepository mockRateRepo = mock(CurrencyConversionRateRepository.class);

        currencyUtil = new CurrencyConversionUtil(mockRateRepo);
        balanceService = new BalanceServiceImpl(accountRepository, clientRepository, currencyUtil);

        CurrencyConversionRate zarRate = new CurrencyConversionRate();
        zarRate.setCurrencyCode("ZAR");
        zarRate.setConversionIndicator("/");
        zarRate.setRate(BigDecimal.ONE);

        CurrencyConversionRate usdRate = new CurrencyConversionRate();
        usdRate.setCurrencyCode("USD");
        usdRate.setConversionIndicator("*");
        usdRate.setRate(new BigDecimal("18.5"));

        when(mockRateRepo.findById("ZAR")).thenReturn(Optional.of(zarRate));
        when(mockRateRepo.findById("USD")).thenReturn(Optional.of(usdRate));
    }

    @Test
    void testGetTransactionalBalances_success() {
        ClientAccount acc = new ClientAccount();
        acc.setAccountNumber("TX123");
        acc.setDisplayBalance(BigDecimal.valueOf(2000.0));

        Currency currency = new Currency();
        currency.setCode("ZAR");
        acc.setCurrency(currency);

        AccountType type = new AccountType();
        type.setCode("CHQ");
        type.setDescription("Cheque Account");
        type.setTransactional(true);
        acc.setAccountType(type);

        Client client = new Client();
        client.setId(1L);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        acc.setClient(client);

        when(accountRepository.findByClientIdAndAccountTypeTransactional(1L, true)).thenReturn(List.of(acc));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        TransactionalBalanceResponseWrapper wrapper = balanceService.getTransactionalBalances(1L);

        assertNotNull(wrapper);
        assertEquals("John", wrapper.client().name());
        assertEquals("TX123", wrapper.accounts().get(0).accountNumber());
        assertEquals("ZAR", wrapper.accounts().get(0).currencyCode());
        assertEquals(0, wrapper.accounts().get(0).zarBalance().compareTo(BigDecimal.valueOf(2000.0)));
        assertTrue(wrapper.result().success());
        assertEquals(200, wrapper.result().statusCode());
    }

    @Test
    void testGetCurrencyBalances_success() {
        ClientAccount acc = new ClientAccount();
        acc.setAccountNumber("FX123");
        acc.setDisplayBalance(BigDecimal.valueOf(100.0));

        Currency currency = new Currency();
        currency.setCode("USD");
        currency.setDecimalPlaces(2);
        currency.setDescription("US Dollar");
        acc.setCurrency(currency);

        AccountType type = new AccountType();
        type.setCode("CFCA");
        type.setDescription("Currency Account");
        type.setTransactional(false);
        acc.setAccountType(type);

        Client client = new Client();
        client.setId(1L);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        acc.setClient(client);

        when(accountRepository.findByClientIdAndAccountTypeTransactional(1L, false)).thenReturn(List.of(acc));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        CurrencyBalanceResponseWrapper wrapper = balanceService.getCurrencyBalances(1L);

        assertNotNull(wrapper);
        assertEquals("John", wrapper.client().name());
        assertEquals("USD", wrapper.accounts().get(0).currencyCode());
        assertEquals("CFCA", wrapper.accounts().get(0).typeCode());
        assertEquals("Currency Account", wrapper.accounts().get(0).accountTypeDescription());
        assertEquals(0, wrapper.accounts().get(0).conversionRate().compareTo(BigDecimal.valueOf(18.5)));
        assertEquals(0, wrapper.accounts().get(0).balance().compareTo(BigDecimal.valueOf(100.0)));
        assertEquals(0, wrapper.accounts().get(0).zarBalance().compareTo(BigDecimal.valueOf(1850.0)));
        assertEquals(0, wrapper.accounts().get(0).accountLimit().compareTo(BigDecimal.ZERO));
        assertTrue(wrapper.result().success());
    }

    @Test
    void testGetTransactionalBalances_empty_shouldThrow() {
        when(accountRepository.findByClientIdAndAccountTypeTransactional(1L, true)).thenReturn(List.of());
        assertThrows(NoAccountsFoundException.class, () -> balanceService.getTransactionalBalances(1L));
    }

    @Test
    void testGetCurrencyBalances_empty_shouldThrow() {
        when(accountRepository.findByClientIdAndAccountTypeTransactional(1L, false)).thenReturn(List.of());
        assertThrows(NoAccountsFoundException.class, () -> balanceService.getCurrencyBalances(1L));
    }
}
