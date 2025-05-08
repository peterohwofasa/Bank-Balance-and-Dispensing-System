package com.bank.balancedispense.service.impl;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;
import com.bank.balancedispense.entities.ATM;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency;
import com.bank.balancedispense.exceptions.ATMNotFoundException;
import com.bank.balancedispense.exceptions.AccountNotFoundException;
import com.bank.balancedispense.exceptions.InsufficientFundsException;
import com.bank.balancedispense.exceptions.NoteCalculationException;
import com.bank.balancedispense.repository.ATMNoteRepository;
import com.bank.balancedispense.repository.ATMRepository;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.services.WithdrawService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
class WithdrawServiceImplTest {

    @Autowired
    WithdrawService withdrawService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ATMNoteRepository atmNoteRepository;

    @Autowired
    ATMRepository atmRepository;

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
        atmNoteRepository.deleteAll();
        atmRepository.deleteAll();
    }

    @Test
    void testWithdrawSuccess() {
        ATM atm = new ATM(1L, "Test ATM", true);
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
        acc.setDescription("Test Account");
        acc.setBalance(2000.0);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setCurrency(Currency.ZAR);
        accountRepository.save(acc);

        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);
        WithdrawResponse response = withdrawService.withdraw(request);

        assertNotNull(response);
        assertEquals(200, response.notesDispensed().entrySet().stream().mapToInt(e -> e.getKey() * e.getValue()).sum());
    }

    @Test
    void testWithdraw_ATMNotFound_shouldThrowException() {
        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 99L);
        assertThrows(ATMNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void testWithdraw_AccountNotFound_shouldThrowException() {
        ATM atm = new ATM(1L, "Test ATM", true);
        atmRepository.save(atm);
        WithdrawRequest request = new WithdrawRequest(1L, "INVALID", 200.0, 1L);
        assertThrows(AccountNotFoundException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void testWithdraw_InsufficientFunds_shouldThrowException() {
        ATM atm = new ATM(1L, "ATM", true);
        atmRepository.save(atm);

        ATMNote note = new ATMNote();
        note.setId(1L);
        note.setAtmId(1L);
        note.setDenomination(100);
        note.setQuantity(5);
        atmNoteRepository.save(note);

        Account acc = new Account();
        acc.setId(1L);
        acc.setClientId(1L);
        acc.setAccountNumber("TX12345");
        acc.setDescription("Low Balance");
        acc.setBalance(100.0);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setCurrency(Currency.ZAR);
        accountRepository.save(acc);

        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 500.0, 1L);
        assertThrows(InsufficientFundsException.class, () -> withdrawService.withdraw(request));
    }

    @Test
    void testWithdraw_InsufficientATMNotes_shouldThrowException() {
        ATM atm = new ATM(1L, "ATM", true);
        atmRepository.save(atm);

        ATMNote note = new ATMNote();
        note.setId(1L);
        note.setAtmId(1L);
        note.setDenomination(200);
        note.setQuantity(0);
        atmNoteRepository.save(note);

        Account acc = new Account();
        acc.setId(1L);
        acc.setClientId(1L);
        acc.setAccountNumber("TX12345");
        acc.setDescription("Valid Account");
        acc.setBalance(2000.0);
        acc.setAccountType(AccountType.TRANSACTIONAL);
        acc.setCurrency(Currency.ZAR);
        accountRepository.save(acc);

        WithdrawRequest request = new WithdrawRequest(1L, "TX12345", 200.0, 1L);
        assertThrows(NoteCalculationException.class, () -> withdrawService.withdraw(request));
    }
}
