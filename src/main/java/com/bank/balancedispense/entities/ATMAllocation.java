package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity that maps the relationship between an ATM and available denominations.
 * Captures how many notes of each denomination are stocked in a given ATM.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ATM_ALLOCATION")
public class ATMAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATM_ALLOCATION_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ATM_ID")
    private ATM atm;

    @ManyToOne
    @JoinColumn(name = "DENOMINATION_ID")
    private Denomination denomination;

    @Column(name = "COUNT")
    private Integer quantity;
}
