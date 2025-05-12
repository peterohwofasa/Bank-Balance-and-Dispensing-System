package com.bank.balancedispense.services;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponseWrapper;

/**
 * Service interface for processing ATM withdrawal requests.
 */
public interface WithdrawService {
    WithdrawResponseWrapper withdraw(WithdrawRequest request);
}
