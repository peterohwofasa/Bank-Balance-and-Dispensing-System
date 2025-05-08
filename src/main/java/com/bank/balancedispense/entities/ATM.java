package com.bank.balancedispense.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an ATM.
 * Stores its status (active/inactive) and physical location.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ATM {
    @Id
    private Long id;
    private String location;
    private boolean active;
}

