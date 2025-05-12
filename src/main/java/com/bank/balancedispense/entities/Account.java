package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a client account.
 * Maps to normalized ACCOUNT_TYPE and CURRENCY reference tables.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1)
    private String accountNumber;

    @NotNull
    @Size(min = 1)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double balance;

    //referencedColumnName must match the DB column
    @ManyToOne
    @JoinColumn(name = "account_type_code", referencedColumnName = "ACCOUNT_TYPE_CODE")
    private AccountType accountType;

    // referencedColumnName must match the DB column
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "CURRENCY_CODE")
    private Currency currency;

    @Column(name = "client_id")
    private Long clientId;
}
