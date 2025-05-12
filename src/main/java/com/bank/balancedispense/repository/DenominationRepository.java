package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.Denomination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenominationRepository extends JpaRepository<Denomination, Long> {

}