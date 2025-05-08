package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.dto.CurrencyBalanceResponse;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.services.BalanceService;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Implementation of BalanceService.
 * Handles retrieval of transactional and currency accounts.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepo;
    private final CurrencyConversionUtil currencyUtil;


    /**
     * Returns sorted transactional accounts for the client.
     * Throws NoAccountsFoundException if none exist.
     */
    public List<TransactionalBalanceResponse> getTransactionalBalances(Long clientId) {
        log.info("Fetching transactional balances for clientId={}", clientId);
        List<Account> accounts = accountRepo.findByClientIdAndAccountType(clientId, AccountType.TRANSACTIONAL);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No transactional accounts to display");
        }
        return accounts.stream()
                .sorted(Comparator.comparingDouble(Account::getBalance).reversed())
                .map(acc -> new TransactionalBalanceResponse(acc.getAccountNumber(), acc.getDescription(), acc.getBalance()))
                .toList();
    }

    /**
     * Returns sorted currency accounts with conversion to ZAR.
     * Throws NoAccountsFoundException if none exist.
     */
    public List<CurrencyBalanceResponse> getCurrencyBalances(Long clientId) {
        log.info("Fetching currency balances for clientId={}", clientId);
        List<Account> accounts = accountRepo.findByClientIdAndAccountType(clientId, AccountType.CURRENCY);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No currency accounts to display");
        }
        return accounts.stream()
                .map(acc -> {
                    double rate = currencyUtil.getConversionRate(acc.getCurrency());
                    return new CurrencyBalanceResponse(acc.getAccountNumber(), acc.getDescription(), acc.getBalance(), acc.getBalance() * rate);
                })
                .sorted(Comparator.comparingDouble(CurrencyBalanceResponse::convertedToRand))
                .toList();
    }
}
