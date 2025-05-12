package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a supported currency.
 * Used as a lookup table for account currency and conversions.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CURRENCY")
public class Currency {
    @Id
    @Column(name = "CURRENCY_CODE", length = 3)
    private String code;

    @Column(name = "DECIMAL_PLACES")
    private int decimalPlaces;

    @Column(name = "DESCRIPTION")
    private String description;
}