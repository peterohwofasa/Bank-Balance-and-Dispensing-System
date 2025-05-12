package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.common.Constants;
import com.bank.balancedispense.common.ErrorMessage;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.*;
import com.bank.balancedispense.exceptions.*;
import com.bank.balancedispense.repository.*;
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
 * Refactored implementation of WithdrawService.
 * Now aligned with Version 2 schema using normalized DB entities.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WithdrawServiceImpl implements WithdrawService {

    private final ClientAccountRepository accountRepo;
    private final ATMAllocationRepository atmAllocationRepo;
    private final ATMRepository atmRepo;
    private final ClientRepository clientRepo;
    private final CurrencyConversionUtil currencyUtil;

    /**
     * Executes a withdrawal from a specified ATM and client account.
     * Uses normalized entities (ClientAccount, Denomination, etc.).
     */
    @Override
    @Transactional
    public WithdrawResponseWrapper withdraw(WithdrawRequest request) {
        log.info("Starting withdrawal for clientId={}, account={}, amount={}, atmId={}",
                request.clientId(), request.accountNumber(), request.amount(), request.atmId());

        // Validate ATM is active
        ATM atm = atmRepo.findById(request.atmId())
                .filter(ATM::isActive)
                .orElseThrow(() -> new ATMNotFoundException(ErrorMessage.ATM_NOT_FOUND.get()));

        // Lookup account by client and account number
        ClientAccount account = accountRepo.findByClient_IdAndAccountNumber(request.clientId(), request.accountNumber())

                .orElseThrow(() -> new AccountNotFoundException(ErrorMessage.ACCOUNT_NOT_FOUND.get()));

        // Check if account has enough funds (consider overdraft)
        validateFunds(account, request.amount());

        // Get available ATM allocations
        List<ATMAllocation> allocations = atmAllocationRepo.findByAtm_Id(request.atmId());

        // Calculate optimal notes to dispense
        Map<Integer, Integer> dispensed;
        try {
            dispensed = NoteCalculator.calculate(request.amount(), allocations);
        } catch (NoteCalculationException e) {
            Optional<Integer> fallback = NoteCalculator.suggestFallbackAmount(request.amount(), allocations);
            throw new NoteCalculationException(ErrorMessage.NOTE_CALCULATION_FAILED.get(), fallback.orElse(null));
        }

        // Update ATM inventory and account balance
        updateATMInventory(allocations, dispensed);
        updateAccountBalance(account, request.amount());

        // Get client details
        Client client = clientRepo.findById(request.clientId())
                .orElseThrow(() -> new AccountNotFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        // Convert to ZAR
        BigDecimal rate = currencyUtil.getConversionRate(account.getCurrency().getCode());
        BigDecimal balance = account.getDisplayBalance();
        BigDecimal zarBalance = balance.multiply(rate);

        // Build account DTO
        TransactionalAccountDto accountDto = new TransactionalAccountDto(
                account.getAccountNumber(),
                account.getAccountType().getCode(),
                account.getAccountType().getDescription(),
                account.getCurrency().getCode(),
                rate,
                balance,
                zarBalance,
                BigDecimal.ZERO
        );

        // Build denomination breakdown
        List<DenominationDto> denominationDtos = allocations.stream()
                .filter(a -> dispensed.containsKey(a.getDenomination().getValue().intValue()))
                .map(a -> new DenominationDto(
                        a.getDenomination().getId(),
                        a.getDenomination().getValue().intValue(),
                        dispensed.get(a.getDenomination().getValue().intValue())
                ))
                .collect(Collectors.toList());

        ResultDto result = new ResultDto(true, 200, "Withdrawal completed successfully");
        log.info("Withdrawal successful. Dispensed={}, New balance={}", dispensed, account.getDisplayBalance());

        return new WithdrawResponseWrapper(clientDto, accountDto, denominationDtos, result);
    }

    /**
     * Validates whether the account has enough funds for withdrawal.
     * Allows overdraft for transactional accounts only.
     */
    private void validateFunds(ClientAccount acc, double amount) {
        boolean isTransactional = acc.getAccountType().isTransactional();
        double allowedLimit = isTransactional ? Constants.OVERDRAFT_LIMIT : 0.0;

        double newBalance = acc.getDisplayBalance().doubleValue() - amount;
        if (newBalance < allowedLimit) {
            log.error("Insufficient funds: balance={}, requested={}, allowedLimit={}",
                    acc.getDisplayBalance(), amount, allowedLimit);
            throw new InsufficientFundsException(ErrorMessage.INSUFFICIENT_FUNDS.get());
        }
    }

    /**
     * Updates ATM allocations by deducting used note quantities.
     */
    private void updateATMInventory(List<ATMAllocation> allocations, Map<Integer, Integer> dispensed) {
        dispensed.forEach((denVal, qty) -> allocations.stream()
                .filter(a -> a.getDenomination().getValue().intValue() == denVal)
                .findFirst()
                .ifPresent(a -> a.setQuantity(a.getQuantity() - qty)));
    }

    /**
     * Deducts withdrawal amount from the account and persists the change.
     */
    private void updateAccountBalance(ClientAccount acc, double amount) {
        acc.setDisplayBalance(acc.getDisplayBalance().subtract(BigDecimal.valueOf(amount)));
        accountRepo.save(acc);
    }
}
