package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.CurrencyBalanceResponseWrapper;
import com.bank.balancedispense.dto.TransactionalBalanceResponseWrapper;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.entities.Client;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.repository.ClientRepository;
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

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    private CurrencyConversionUtil currencyConversionUtil;

    private BalanceServiceImpl balanceService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        currencyConversionUtil = new CurrencyConversionUtil(); // ✅ Use real instance
        balanceService = new BalanceServiceImpl(accountRepository, clientRepository, currencyConversionUtil);
    }

    @Test
    void testGetTransactionalBalances_success() {
        Account acc = new Account();
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setBalance(2000.0);
        acc.setAccountNumber("TX123");
        acc.setCurrency(Currency.ZAR);
        acc.setDescription("Main");

        Client client = new Client();
        client.setId(1L);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");

        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.TRANSACTIONAL))
                .thenReturn(List.of(acc));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        TransactionalBalanceResponseWrapper wrapper = balanceService.getTransactionalBalances(1L);

        assertNotNull(wrapper);
        assertEquals("John", wrapper.client().name());
        assertEquals("TX123", wrapper.accounts().get(0).accountNumber());
        assertEquals("ZAR", wrapper.accounts().get(0).currencyCode());
        assertEquals(BigDecimal.valueOf(2000.0), wrapper.accounts().get(0).zarBalance());
        assertTrue(wrapper.result().success());
        assertEquals(200, wrapper.result().statusCode());
    }

    @Test
    void testGetCurrencyBalances_success() throws Exception {
        // Setup account
        Account acc = new Account();
        acc.setAccountType(AccountType.CURRENCY);
        acc.setBalance(100.0);
        acc.setCurrency(Currency.USD);
        acc.setAccountNumber("FX123");
        acc.setDescription("USD Wallet");

        // Setup client
        Client client = new Client();
        client.setId(1L);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");

        // Mock repositories
        when(accountRepository.findByClientIdAndAccountType(1L, AccountType.CURRENCY)).thenReturn(List.of(acc));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        // Inject currency rate manually via reflection
        var usdRateField = CurrencyConversionUtil.class.getDeclaredField("usdRate");
        usdRateField.setAccessible(true);
        usdRateField.set(currencyConversionUtil, 18.5);

        var eurRateField = CurrencyConversionUtil.class.getDeclaredField("eurRate");
        eurRateField.setAccessible(true);
        eurRateField.set(currencyConversionUtil, 20.0);

        // Call service
        CurrencyBalanceResponseWrapper wrapper = balanceService.getCurrencyBalances(1L);

        // Assertions
        assertNotNull(wrapper);
        assertEquals("John", wrapper.client().name());
        assertEquals("USD", wrapper.accounts().get(0).currencyCode());
        assertEquals("CURRENCY", wrapper.accounts().get(0).typeCode());
        assertEquals("Currency Account", wrapper.accounts().get(0).accountTypeDescription());

        assertEquals(0, wrapper.accounts().get(0).conversionRate().compareTo(BigDecimal.valueOf(18.5)));
        assertEquals(0, wrapper.accounts().get(0).balance().compareTo(BigDecimal.valueOf(100.0)));
        assertEquals(0, wrapper.accounts().get(0).zarBalance().compareTo(BigDecimal.valueOf(1850.0)));
        assertEquals(0, wrapper.accounts().get(0).accountLimit().compareTo(BigDecimal.ZERO));

        assertTrue(wrapper.result().success());
        assertEquals(200, wrapper.result().statusCode());
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
