package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.ATM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for ATM lookup and persistence operations.
 */
public interface ATMRepository extends JpaRepository<ATM, Long> {
    Optional<ATM> findById(Long id);
}