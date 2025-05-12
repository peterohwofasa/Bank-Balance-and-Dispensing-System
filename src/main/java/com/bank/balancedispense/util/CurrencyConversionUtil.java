package com.bank.balancedispense.util;

import com.bank.balancedispense.entities.CurrencyConversionRate;
import com.bank.balancedispense.repository.CurrencyConversionRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility that provides currency-to-ZAR conversion using the database-backed currency conversion rate table.
 */
@Component
@RequiredArgsConstructor
public class CurrencyConversionUtil {

    private final CurrencyConversionRateRepository rateRepo;

    /**
     * Returns the ZAR conversion rate for the given currency code.
     *
     * @param currencyCode The 3-letter ISO currency code (e.g. "USD", "EUR", "ZAR")
     * @return Conversion rate to ZAR as BigDecimal
     */
    public BigDecimal getConversionRate(String currencyCode) {
        if (currencyCode.equalsIgnoreCase("ZAR")) {
            return BigDecimal.ONE;
        }

        CurrencyConversionRate rate = rateRepo.findById(currencyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported currency: " + currencyCode));

        return switch (rate.getConversionIndicator()) {
            case "*" -> rate.getRate();
            case "/" -> BigDecimal.ONE.divide(rate.getRate(), 8, RoundingMode.HALF_UP);
            default -> throw new IllegalStateException("Invalid conversion indicator for " + currencyCode);
        };
    }
}
