package com.bank.balancedispense.util;

import com.bank.balancedispense.enums.Currency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility component that provides currency-to-ZAR conversion rates.
 *
 * Rates are injected via application properties and used during currency balance evaluation.
 */
@Component
public class CurrencyConversionUtil {

    /** Injected conversion rate for USD to ZAR */
    @Value("${currency.rate.usd}")
    private double usdRate;

    /** Injected conversion rate for EUR to ZAR */
    @Value("${currency.rate.eur}")
    private double eurRate;

    /**
     * Returns the ZAR conversion rate for a supported currency.
     *
     * @param currency The currency to convert from
     * @return Conversion rate to ZAR
     * @throws IllegalArgumentException if the currency is unsupported
     */
    public double getConversionRate(Currency currency) {
        return switch (currency) {
            case USD -> usdRate;
            case EUR -> eurRate;
            case ZAR -> 1.0;
            default -> throw new IllegalArgumentException("Unsupported currency: " + currency);
        };
    }
}
