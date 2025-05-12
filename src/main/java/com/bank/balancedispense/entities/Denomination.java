package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a monetary denomination used in ATM allocations.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DENOMINATION")
public class Denomination {

    @Id
    @Column(name = "DENOMINATION_ID")
    private Long id;

    @Column(name = "DENOMINATION_VALUE", precision = 18, scale = 2, nullable = false)
    private BigDecimal value;

    // Optionally: constructor for tests
    public Denomination(Long id, BigDecimal value) {
        this.id = id;
        this.value = value;
    }
}
