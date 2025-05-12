package com.bank.balancedispense.services.impl;

import com.bank.balancedispense.common.Constants;
import com.bank.balancedispense.dto.*;
import com.bank.balancedispense.entities.Client;
import com.bank.balancedispense.entities.ClientAccount;
import com.bank.balancedispense.exceptions.NoAccountsFoundException;
import com.bank.balancedispense.repository.ClientAccountRepository;
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
 * Service implementation for retrieving client account balances.
 * Uses the normalized schema with ClientAccount, AccountType, and Currency mappings.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BalanceServiceImpl implements BalanceService {

    private final ClientAccountRepository accountRepo;
    private final ClientRepository clientRepo;
    private final CurrencyConversionUtil currencyUtil;

    /**
     * Retrieves all transactional accounts for the given client.
     * Sorted descending by balance.
     */
    @Override
    public TransactionalBalanceResponseWrapper getTransactionalBalances(Long clientId) {
        log.info("Fetching transactional balances for clientId={}", clientId);

        // Filter by account types that are marked transactional = true
        List<ClientAccount> accounts = accountRepo.findByClientIdAndAccountTypeTransactional(clientId, true);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No transactional accounts to display");
        }

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoAccountsFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        List<TransactionalAccountDto> accountDtos = accounts.stream()
                .sorted(Comparator.comparing(ClientAccount::getDisplayBalance).reversed())
                .map(acc -> {
                    BigDecimal rate = currencyUtil.getConversionRate(acc.getCurrency().getCode());
                    BigDecimal balance = acc.getDisplayBalance();
                    BigDecimal zarBalance = balance.multiply(rate);

                    return new TransactionalAccountDto(
                            acc.getAccountNumber(),
                            acc.getAccountType().getCode(),
                            acc.getAccountType().getDescription(),
                            acc.getCurrency().getCode(),
                            rate,
                            balance,
                            zarBalance,
                            BigDecimal.ZERO
                    );
                })
                .collect(Collectors.toList());

        ResultDto result = new ResultDto(true, 200, "Transactional balances retrieved successfully");
        return new TransactionalBalanceResponseWrapper(clientDto, accountDtos, result);
    }

    /**
     * Retrieves all currency accounts for the client and converts them to ZAR.
     * Sorted ascending by converted value.
     */
    @Override
    public CurrencyBalanceResponseWrapper getCurrencyBalances(Long clientId) {
        log.info("Fetching currency balances for clientId={}", clientId);

        // Filter by account types where transactional = false
        List<ClientAccount> accounts = accountRepo.findByClientIdAndAccountTypeTransactional(clientId, false);
        if (accounts.isEmpty()) {
            throw new NoAccountsFoundException("No currency accounts to display");
        }

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NoAccountsFoundException("Client not found"));

        ClientDto clientDto = new ClientDto(client.getId(), client.getTitle(), client.getName(), client.getSurname());

        List<CurrencyBalanceResponse> currencyResponses = accounts.stream()
                .map(acc -> {
                    BigDecimal rate = currencyUtil.getConversionRate(acc.getCurrency().getCode());
                    BigDecimal balance = acc.getDisplayBalance();
                    BigDecimal converted = balance.multiply(rate);

                    BigDecimal accountLimit = acc.getAccountType().isTransactional()
                            ? BigDecimal.valueOf(Math.abs(Constants.OVERDRAFT_LIMIT))
                            : BigDecimal.ZERO;

                    return new CurrencyBalanceResponse(
                            acc.getAccountNumber(),
                            acc.getAccountType().getCode(),
                            acc.getAccountType().getDescription(),
                            acc.getCurrency().getCode(),
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
