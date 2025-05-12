package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.ClientAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientAccountRepository extends JpaRepository<ClientAccount, String> {

    List<ClientAccount> findByClientId(Long clientId);

    List<ClientAccount> findByClientIdAndAccountType_Code(Long clientId, String accountTypeCode);

    /**
     * Finds a specific client account using the client's ID and account number.
     * Aligns with the normalized schema where client is an object in ClientAccount.
     */
    Optional<ClientAccount> findByClient_IdAndAccountNumber(Long clientId, String accountNumber);
    List<ClientAccount> findByClientIdAndAccountTypeTransactional(Long clientId, boolean transactional);


}
