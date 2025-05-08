package com.bank.balancedispense.services;

import com.bank.balancedispense.dto.TransactionalBalanceResponse;
import com.bank.balancedispense.dto.CurrencyBalanceResponse;

import java.util.List;

/**
 * Service interface for retrieving account balances for a client.
 */
public interface BalanceService {
    /**
     * Service interface for retrieving account balances for a client.
     */
    List<TransactionalBalanceResponse> getTransactionalBalances(Long clientId);

    /**
     * Returns all foreign currency account balances with conversion to ZAR.
     */
    List<CurrencyBalanceResponse> getCurrencyBalances(Long clientId);
}
