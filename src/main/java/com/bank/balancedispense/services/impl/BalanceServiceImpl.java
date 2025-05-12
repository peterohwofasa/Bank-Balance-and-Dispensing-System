package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.common.Constants;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.entities.Client;
import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.AccountRepository;
import com.bank.balancedispense.repository.ClientRepository;
import com.bank.balancedispense.services.BalanceService;
import com.bank.balancedispense.util.CurrencyConversionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of BalanceService.
 * Handles retrieval of transactional and currency accounts.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepo;
    private final ClientRepository clientRepo;
    private final CurrencyConversionUtil currencyUtil;

    /**
     * Returns a wrapped response of transactional accounts with client and status info.
     */
    @Override
    public TransactionalBalanceResponseWrapper getTransactionalBalances(Long clientId) {
        log.info("Fetching transactional balances for clientId={}", clientId);

        List<Account> accounts = accountRepo.findByClientIdAndAccountType(clientId, AccountType.TRANSACTIONAL);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No transactional accounts to display");
        }

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoAccountsFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        List<TransactionalAccountDto> accountDtos = accounts.stream()
                .sorted(Comparator.comparingDouble(Account::getBalance).reversed())
                .map(acc -> new TransactionalAccountDto(
                        acc.getAccountNumber(),
                        acc.getAccountType().name(),
                        "Transactional Account",
                        acc.getCurrency().name(),
                        BigDecimal.valueOf(currencyUtil.getConversionRate(acc.getCurrency())),
                        BigDecimal.valueOf(acc.getBalance()),
                        BigDecimal.valueOf(acc.getBalance()),
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());

        ResultDto result = new ResultDto(true, 200, "Transactional balances retrieved successfully");

        return new TransactionalBalanceResponseWrapper(clientDto, accountDtos, result);
    }

    /**
     * Returns sorted currency accounts with conversion to ZAR.
     * Throws NoAccountsFoundException if none exist.
     */
    @Override
    public CurrencyBalanceResponseWrapper getCurrencyBalances(Long clientId) {
        log.info("Fetching currency balances for clientId={}", clientId);

        List<Account> accounts = accountRepo.findByClientIdAndAccountType(clientId, AccountType.CURRENCY);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No currency accounts to display");
        }

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoAccountsFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        List<CurrencyBalanceResponse> currencyResponses = accounts.stream()
                .map(acc -> {
                    BigDecimal rate = BigDecimal.valueOf(currencyUtil.getConversionRate(acc.getCurrency()));
                    BigDecimal balance = BigDecimal.valueOf(acc.getBalance());
                    BigDecimal converted = balance.multiply(rate);

                    // Use defined overdraft limit logic if applicable
                    BigDecimal accountLimit = acc.getAccountType() == AccountType.TRANSACTIONAL
                            ? BigDecimal.valueOf(Math.abs(Constants.OVERDRAFT_LIMIT)) // 10000.0
                            : BigDecimal.ZERO;

                    return new CurrencyBalanceResponse(
                            acc.getAccountNumber(),
                            acc.getAccountType().name(),
                            acc.getAccountType().getDescription(),
                            acc.getCurrency().name(),
                            rate,
                            balance,
                            converted,
                            accountLimit
                    );
                })
                .sorted(Comparator.comparing(CurrencyBalanceResponse::zarBalance))
                .toList();


        ResultDto result = new ResultDto(true, 200, "Currency balances retrieved successfully");

        return new CurrencyBalanceResponseWrapper(clientDto, currencyResponses, result);
    }

}
