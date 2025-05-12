package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.CurrencyConversionRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyConversionRateRepository extends JpaRepository<CurrencyConversionRate, String> {

}
