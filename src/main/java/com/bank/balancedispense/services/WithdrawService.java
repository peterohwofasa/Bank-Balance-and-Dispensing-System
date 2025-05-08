package com.bank.balancedispense.services;

import com.bank.balancedispense.dto.WithdrawRequest;
import com.bank.balancedispense.dto.WithdrawResponse;

/**
 * Service interface for processing ATM withdrawal requests.
 */
public interface WithdrawService {
    WithdrawResponse withdraw(WithdrawRequest request);
}
