package com.bank.balancedispense.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a specific denomination of note available in an ATM.
 */
@Getter
@Setter
@Entity
public class ATMNote {
    @Id
    private Long id;
    private Long atmId;
    private Integer denomination;
    private Integer quantity;
}
