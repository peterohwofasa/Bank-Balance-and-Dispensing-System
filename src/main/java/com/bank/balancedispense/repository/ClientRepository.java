package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for performing CRUD operations on Client entities.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    // Additional query methods can be defined here if needed
}
