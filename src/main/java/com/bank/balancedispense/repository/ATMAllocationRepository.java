package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.ATMAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ATM note inventory operations.
 */
@Repository
public interface ATMAllocationRepository extends JpaRepository<ATMAllocation, Long> {

    // Retrieves all notes available in a specific ATM
    List<ATMAllocation> findByAtm_Id(Long atmId);

}