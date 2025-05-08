package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.common.Constants;
import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;
import com.bank.balancedispense.entities.ATM;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.exceptions.ATMNotFoundException;
import com.bank.balancedispense.exceptions.AccountNotFoundException;
import com.bank.balancedispense.exceptions.InsufficientFundsException;
import com.bank.balancedispense.repository.ATMNoteRepository;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.repository.ATMRepository;
import com.bank.balancedispense.services.WithdrawService;
import com.bank.balancedispense.util.NoteCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of WithdrawService.
 * Handles ATM note dispensing and account balance updates.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WithdrawServiceImpl implements WithdrawService {

    private final AccountRepository accountRepo;
    private final ATMNoteRepository atmNoteRepo;
    private final ATMRepository atmRepo;

    /**
     * Executes a withdrawal from a specified ATM and client account.
     * Handles ATM availability, fund validation, and note calculations.
     */
    @Override
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request) {
        log.info("Starting withdrawal for clientId={}, account={}, amount={}, atmId={}",
                request.clientId(), request.accountNumber(), request.amount(), request.atmId());

        ATM atm = atmRepo.findById(request.atmId())
                .filter(ATM::isActive)
                .orElseThrow(() -> new ATMNotFoundException(ErrorMessage.ATM_NOT_FOUND.get()));

        Account acc = accountRepo.findByClientIdAndAccountNumber(request.clientId(), request.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(ErrorMessage.ACCOUNT_NOT_FOUND.get()));

        validateFunds(acc, request.amount());

        List<ATMNote> notes = atmNoteRepo.findByAtmId(request.atmId());
        Map<Integer, Integer> dispensed = NoteCalculator.calculate(request.amount(), notes);

        updateATMNotes(notes, dispensed);
        updateAccountBalance(acc, request.amount());

        log.info("Withdrawal successful. Dispensed notes={}, new balance={}", dispensed, acc.getBalance());
        return new WithdrawResponse(dispensed, acc.getBalance());
    }

    /**
     * Ensures account has sufficient funds (handles overdraft limit if applicable).
     */
    private void validateFunds(Account acc, double amount) {
        double limit = acc.getAccountType() == AccountType.TRANSACTIONAL ? 0 : Constants.OVERDRAFT_LIMIT;
        if (acc.getBalance() - amount < limit) {
            log.error("Insufficient funds for account={}, balance={}, requested={}.", acc.getAccountNumber(), acc.getBalance(), amount);
            throw new InsufficientFundsException(ErrorMessage.INSUFFICIENT_FUNDS.get());
        }
    }

    /**
     * Deducts dispensed note quantities from ATM note inventory.
     */
    private void updateATMNotes(List<ATMNote> notes, Map<Integer, Integer> dispensed) {
        dispensed.forEach((den, qty) -> {
            notes.stream()
                    .filter(n -> n.getDenomination().equals(den))
                    .findFirst()
                    .ifPresent(n -> n.setQuantity(n.getQuantity() - qty));
        });
    }

    /**
     * Updates and saves new account balance after withdrawal.
     */
    private void updateAccountBalance(Account acc, double amount) {
        acc.setBalance(acc.getBalance() - amount);
        accountRepo.save(acc);
    }
}
