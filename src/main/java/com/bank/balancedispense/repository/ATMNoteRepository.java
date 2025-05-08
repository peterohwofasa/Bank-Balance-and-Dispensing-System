package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.ATMNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ATM note inventory operations.
 */
@Repository
public interface ATMNoteRepository extends JpaRepository<ATMNote, Long> {

    // Retrieves all notes available in a specific ATM
    List<ATMNote> findByAtmId(Long atmId);

}