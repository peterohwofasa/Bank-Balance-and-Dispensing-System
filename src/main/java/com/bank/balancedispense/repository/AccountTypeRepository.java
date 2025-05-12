package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTypeRepository extends JpaRepository<AccountType, String> {

}
