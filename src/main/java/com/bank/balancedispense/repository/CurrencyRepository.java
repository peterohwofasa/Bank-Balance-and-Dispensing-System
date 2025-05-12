package com.bank.balancedispense.repository;

import com.bank.balancedispense.entities.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

}
