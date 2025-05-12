package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the type of an account.
 * Examples include CHQ (Cheque Account), CCY (Currency Wallet), LOAN, etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ACCOUNT_TYPE")
public class AccountType {
    @Id
    @Column(name = "ACCOUNT_TYPE_CODE", length = 10)
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TRANSACTIONAL")
    private boolean transactional;
}