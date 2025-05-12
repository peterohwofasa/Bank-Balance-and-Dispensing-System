package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity storing conversion rate and logic per currency.
 * This enables dynamic conversion to ZAR using either multiplication or division.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CURRENCY_CONVERSION_RATE")
public class CurrencyConversionRate {
    @Id
    @Column(name = "CURRENCY_CODE")
    private String currencyCode;

    @Column(name = "CONVERSION_INDICATOR")
    private String conversionIndicator;

    @Column(name = "RATE", precision = 18, scale = 8)
    private BigDecimal rate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENCY_CODE", insertable = false, updatable = false)
    private Currency currency;
}