package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.common.Constants;
import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.ATM;
import com.bank.balancedispense.entities.ATMNote;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.entities.Client;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.exceptions.ATMNotFoundException;
import com.bank.balancedispense.exceptions.AccountNotFoundException;
import com.bank.balancedispense.exceptions.InsufficientFundsException;
import com.bank.balancedispense.exceptions.NoteCalculationException;
import com.bank.balancedispense.repository.ATMNoteRepository;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.repository.ATMRepository;
import com.bank.balancedispense.repository.ClientRepository;
import com.bank.balancedispense.services.WithdrawService;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import com.bank.balancedispense.util.NoteCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ClientRepository clientRepo;
    private final CurrencyConversionUtil currencyUtil;

    /**
     * Executes a withdrawal from a specified ATM and client account.
     * Handles ATM availability, fund validation, and note calculations.
     */
    @Override
    @Transactional
    public WithdrawResponseWrapper withdraw(WithdrawRequest request) {
        log.info("Starting withdrawal for clientId={}, account={}, amount={}, atmId={}",
                request.clientId(), request.accountNumber(), request.amount(), request.atmId());

        ATM atm = atmRepo.findById(request.atmId())
                .filter(ATM::isActive)
                .orElseThrow(() -> new ATMNotFoundException(ErrorMessage.ATM_NOT_FOUND.get()));

        Account acc = accountRepo.findByClientIdAndAccountNumber(request.clientId(), request.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException(ErrorMessage.ACCOUNT_NOT_FOUND.get()));

        validateFunds(acc, request.amount());

        List<ATMNote> notes = atmNoteRepo.findByAtmId(request.atmId());
        Map<Integer, Integer> dispensed;

        try {
            dispensed = NoteCalculator.calculate(request.amount(), notes);
        } catch (NoteCalculationException e) {
            // Try to suggest fallback amount and throw enriched exception
            Optional<Integer> fallback = NoteCalculator.suggestFallbackAmount(request.amount(), notes);
            throw new NoteCalculationException(ErrorMessage.NOTE_CALCULATION_FAILED.get(), fallback.orElse(null));
        }

        updateATMNotes(notes, dispensed);
        updateAccountBalance(acc, request.amount());

        Client client = clientRepo.findById(request.clientId())
                .orElseThrow(() -> new AccountNotFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        BigDecimal conversionRate = BigDecimal.valueOf(currencyUtil.getConversionRate(acc.getCurrency()));
        BigDecimal zarBalance = BigDecimal.valueOf(acc.getBalance()).multiply(conversionRate);

        TransactionalAccountDto accountDto = new TransactionalAccountDto(
                acc.getAccountNumber(),
                acc.getAccountType().name(),
                "Transactional Account",
                acc.getCurrency().name(),
                conversionRate,
                BigDecimal.valueOf(acc.getBalance()),
                zarBalance,
                BigDecimal.valueOf(0) // Account limit placeholder
        );

        List<DenominationDto> denominationDtos = notes.stream()
                .filter(n -> dispensed.containsKey(n.getDenomination()))
                .map(n -> new DenominationDto(n.getId(), n.getDenomination(), dispensed.get(n.getDenomination())))
                .collect(Collectors.toList());


        ResultDto result = new ResultDto(true, 200, "Withdrawal completed successfully");

        log.info("Withdrawal successful. Dispensed notes={}, new balance={}", dispensed, acc.getBalance());
        return new WithdrawResponseWrapper(clientDto, accountDto, denominationDtos, result);
    }

    /**
     * Ensures account has sufficient funds (allows overdraft for transactional accounts).
     */
    private void validateFunds(Account acc, double amount) {
        double allowedLimit = acc.getAccountType() == AccountType.TRANSACTIONAL
                ? Constants.OVERDRAFT_LIMIT  // Allow overdraft of up to -10,000 for transactional (cheque) accounts
                : 0;// No overdraft for currency/loan accounts

        // According to the spec, overdraft up to -10,000 is allowed for cheque accounts.
        // In this implementation, we treat all TRANSACTIONAL accounts as cheque accounts by design.

        double newBalance = acc.getBalance() - amount;

        if (newBalance < allowedLimit) {
            log.error("Insufficient funds: account={}, balance={}, requested={}, allowedLimit={}",
                    acc.getAccountNumber(), acc.getBalance(), amount, allowedLimit);
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
