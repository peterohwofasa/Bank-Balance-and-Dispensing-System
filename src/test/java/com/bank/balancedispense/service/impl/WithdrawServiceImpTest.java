package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.*;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.exceptions.*;
import com.bank.balancedispense.repository.*;
import com.bank.balancedispense.services.impl.WithdrawServiceImpl;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WithdrawServiceImpl}.
 *
 * These tests validate the withdrawal logic, including overdraft checks,
 * ATM note calculations, and exception flows.
 */
public class WithdrawServiceImpTest {

    @Mock private AccountRepository accountRepo;
    @Mock private ATMNoteRepository atmNoteRepo;
    @Mock private ATMRepository atmRepo;
    @Mock private ClientRepository clientRepo;
    @Mock private CurrencyConversionUtil currencyUtil;

    @InjectMocks
    private WithdrawServiceImpl withdrawService;

    @BeforeEach
    void init() {

        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests a successful withdrawal with valid ATM, account, and notes available.
     */
    @Test
    void withdraw_shouldReturnWrappedResponse_whenSuccessful() {
        Long clientId = 1L;

        // Prepare request
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 200.0, 1L);

        // Mock ATM
        ATM atm = new ATM(1L, "Branch", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        // Mock Account
        Account account = new Account();
        account.setClientId(clientId);
        account.setAccountNumber("TX12345");
        account.setBalance(1000.0);
        account.setCurrency(Currency.ZAR);
        account.setAccountType(AccountType.TRANSACTIONAL);
        when(accountRepo.findByClientIdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(account));

        // Mock ATM notes
        ATMNote note = new ATMNote();
        note.setDenomination(200);
        note.setQuantity(5);
        when(atmNoteRepo.findByAtmId(1L)).thenReturn(List.of(note));

        // Mock client
        Client client = new Client();
        client.setId(clientId);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        // Mock conversion rate
        when(currencyUtil.getConversionRate(Currency.ZAR)).thenReturn(1.0);

        // Call
        WithdrawResponseWrapper response = withdrawService.withdraw(request);

        // Assert
        assertEquals("John", response.client().name());
        assertEquals("TX12345", response.account().accountNumber());
        assertEquals(200, response.denomination().get(0).denominationValue());
        assertEquals(1, response.denomination().get(0).count());
        assertTrue(response.result().success());

        verify(accountRepo).save(any(Account.class));
    }

    /**
     * Test ATM not found scenario.
     */
    @Test
    void withdraw_shouldThrowATMNotFoundException_ifATMNotFound() {
        when(atmRepo.findById(1L)).thenReturn(Optional.empty());

        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 200.0, 1L);

        assertThrows(ATMNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    /**
     * Test account not found for given client ID and account number.
     */
    @Test
    void withdraw_shouldThrowAccountNotFound_ifNoAccount() {
        when(atmRepo.findById(1L)).thenReturn(Optional.of(new ATM(1L, "ATM", true)));
        when(accountRepo.findByClientIdAndAccountNumber(1L, "TX123")).thenReturn(Optional.empty());

        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 200.0, 1L);
        assertThrows(AccountNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    /**
     * Test when account balance exceeds overdraft threshold after withdrawal.
     */
    @Test
    void withdraw_shouldThrowInsufficientFundsException_whenFundsTooLow() {
        ATM atm = new ATM(1L, "ATM", true);
        Account acc = new Account();
        acc.setAccountNumber("TX123");
        acc.setBalance(-9500.0);  // Already overdrawn near the limit
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setCurrency(Currency.ZAR);
        acc.setClientId(1L);

        // Withdrawal of R1000 would result in -10,500 which exceeds overdraft limit of -10,000
        WithdrawRequest request = new WithdrawRequest(1L, "TX123", 1000.0, 1L);

        // Mocks
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));
        when(accountRepo.findByClientIdAndAccountNumber(1L, "TX123")).thenReturn(Optional.of(acc));

        // ATM has enough notes — irrelevant for this test but must not fail
        ATMNote note = new ATMNote();
        note.setDenomination(100);
        note.setQuantity(20);
        when(atmNoteRepo.findByAtmId(1L)).thenReturn(List.of(note));

        // Run & Assert
        assertThrows(InsufficientFundsException.class, () -> withdrawService.withdraw(request));
    }

    /**
     * Test that overdraft up to -10000 is allowed for transactional accounts.
     */
    @Test
    void withdraw_shouldAllowOverdraftForTransactionalAccounts() {
        Long clientId = 1L;

        // Prepare request: withdrawing R5000 from a R-4000 balance (final balance = -9000, valid overdraft)
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 5000.0, 1L);

        // Mock ATM
        ATM atm = new ATM(1L, "Branch", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        // Mock Account with balance -4000 (allowed overdraft is -10000)
        Account account = new Account();
        account.setClientId(clientId);
        account.setAccountNumber("TX12345");
        account.setBalance(-4000.0);
        account.setCurrency(Currency.ZAR);
        account.setAccountType(AccountType.TRANSACTIONAL);
        when(accountRepo.findByClientIdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(account));

        // Mock ATM notes
        ATMNote note = new ATMNote();
        note.setDenomination(1000);
        note.setQuantity(10);
        when(atmNoteRepo.findByAtmId(1L)).thenReturn(List.of(note));

        // Mock client
        Client client = new Client();
        client.setId(clientId);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        // Mock currency rate
        when(currencyUtil.getConversionRate(Currency.ZAR)).thenReturn(1.0);

        // Call service
        WithdrawResponseWrapper response = withdrawService.withdraw(request);

        // Assert: no exception thrown and withdrawal successful
        assertEquals("John", response.client().name());
        assertEquals("TX12345", response.account().accountNumber());
        assertEquals(1000, response.denomination().get(0).denominationValue());
        assertEquals(5, response.denomination().get(0).count()); // 5000 / 1000 = 5 notes
        assertTrue(response.result().success());

        verify(accountRepo).save(any(Account.class));
    }

    /**
     * Test fallback amount is returned when exact amount cannot be dispensed.
     */
    @Test
    void withdraw_shouldThrowNoteCalculationException_withFallbackAmount() {
        Long clientId = 1L;

        // Prepare request
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 300.0, 1L);

        // Mock ATM
        ATM atm = new ATM(1L, "ATM", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        // Mock Account
        Account acc = new Account();
        acc.setClientId(clientId);
        acc.setAccountNumber("TX12345");
        acc.setBalance(1000.0);
        acc.setCurrency(Currency.ZAR);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        when(accountRepo.findByClientIdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(acc));

        // ATM Notes: Can only dispense 200 + 50 (not enough for 300)
        ATMNote note200 = new ATMNote();
        note200.setDenomination(200);
        note200.setQuantity(1);

        ATMNote note50 = new ATMNote();
        note50.setDenomination(50);
        note50.setQuantity(1);

        when(atmNoteRepo.findByAtmId(1L)).thenReturn(List.of(note200, note50));

        // Client
        Client client = new Client();
        client.setId(clientId);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        // Conversion rate
        when(currencyUtil.getConversionRate(Currency.ZAR)).thenReturn(1.0);

        // Run and expect NoteCalculationException with fallback
        NoteCalculationException ex = assertThrows(NoteCalculationException.class, () -> withdrawService.withdraw(request));

        assertTrue(ex.getMessage().contains("Amount cannot be dispensed"));
        assertNotNull(ex.getFallbackAmount());
        assertEquals(250, ex.getFallbackAmount()); // 200 + 50 is the max we can offer
    }

    /**
     * Test note calculation fails without any fallback amount.
     */
    @Test
    void withdraw_shouldThrowNoteCalculationException_withoutFallbackAmount() {
        Long clientId = 1L;

        // Prepare request for amount that can't be dispensed
        WithdrawRequest request = new WithdrawRequest(clientId, "TX12345", 100.0, 1L);

        // Mock ATM
        ATM atm = new ATM(1L, "ATM", true);
        when(atmRepo.findById(1L)).thenReturn(Optional.of(atm));

        // Mock Account
        Account acc = new Account();
        acc.setClientId(clientId);
        acc.setAccountNumber("TX12345");
        acc.setBalance(1000.0);
        acc.setCurrency(Currency.ZAR);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        when(accountRepo.findByClientIdAndAccountNumber(clientId, "TX12345")).thenReturn(Optional.of(acc));

        // ATM Notes – no usable notes
        ATMNote note50 = new ATMNote();
        note50.setDenomination(50);
        note50.setQuantity(0); // zero quantity = cannot dispense

        when(atmNoteRepo.findByAtmId(1L)).thenReturn(List.of(note50));

        // Client
        Client client = new Client();
        client.setId(clientId);
        client.setTitle("Mr");
        client.setName("John");
        client.setSurname("Doe");
        when(clientRepo.findById(clientId)).thenReturn(Optional.of(client));

        // Conversion rate
        when(currencyUtil.getConversionRate(Currency.ZAR)).thenReturn(1.0);

        // Run and assert
        NoteCalculationException ex = assertThrows(NoteCalculationException.class, () -> withdrawService.withdraw(request));

        assertTrue(ex.getMessage().contains("Amount cannot be dispensed"));
        assertNull(ex.getFallbackAmount()); // fallback should be null if no suggestion is possible
    }

}
