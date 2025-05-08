package com.bank.balancedispense.entities;

import com.bank.balancedispense.enums.AccountType;
import com.bank.balancedispense.enums.Currency; // ✅ Correct custom import
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing an ATM.
 * Stores its status (active/inactive) and physical location.
 */
@Getter
@Setter
@Entity
public class Account {
    @Id
    private Long id;

    @NotNull
    @Size(min = 1)
    private String accountNumber;

    @NotNull
    @Size(min = 1)
    private String description;

    @NotNull
    @Size(min = 1)
    private Double balance;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull
    private Long clientId;
}
