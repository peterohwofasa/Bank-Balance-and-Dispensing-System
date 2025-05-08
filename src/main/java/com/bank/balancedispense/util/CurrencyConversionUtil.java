package com.bank.balancedispense.util;

import com.bank.balancedispense.enums.Currency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting foreign currency to ZAR based on configured rates.
 */
@Component
public class CurrencyConversionUtil {

    @Value("${currency.rate.usd}")
    private double usdRate;

    @Value("${currency.rate.eur}")
    private double eurRate;

    /**
     * Returns the conversion rate based on the input currency.
     */
    public double getConversionRate(Currency currency) {
        return switch (currency) {
            case USD -> usdRate;
            case EUR -> eurRate;
            default -> 1.0;
        };
    }
}
