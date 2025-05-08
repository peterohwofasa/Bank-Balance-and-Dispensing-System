package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.Account;
import com.bank.balancedispense.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for managing Account entity operations.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Finds all accounts for a client based on the account type (TRANSACTIONAL or CURRENCY)
    List<Account> findByClientIdAndAccountType(Long clientId, AccountType type);

    // Finds a specific account using client ID and account number
    Optional<Account> findByClientIdAndAccountNumber(Long clientId, String accountNumber);
}
