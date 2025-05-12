package com.bank.balancedispense.services;

import com.bank.balancedispense.dto.TransactionalBalanceResponseWrapper;
import com.bank.balancedispense.dto.CurrencyBalanceResponseWrapper;

import java.util.List;

/**
 * Service interface for retrieving account balances for a client.
 */
public interface BalanceService {

    /**
     * Returns transactional balances wrapped with client and result metadata.
     */
    TransactionalBalanceResponseWrapper getTransactionalBalances(Long clientId);

    /**
     * Returns currency balances wrapped with client and result metadata.
     */
    CurrencyBalanceResponseWrapper getCurrencyBalances(Long clientId);
}
