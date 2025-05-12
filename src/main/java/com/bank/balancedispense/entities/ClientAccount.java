package com.bank.balancedispense.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing a client account.
 * This is the normalized version that ties clients to accounts with specific currencies and types.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CLIENT_ACCOUNT")
public class ClientAccount {
    @Id
    @Column(name = "CLIENT_ACCOUNT_NUMBER", length = 10)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "CLIENT_ID")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_TYPE_CODE")
    private AccountType accountType;

    @ManyToOne
    @JoinColumn(name = "CURRENCY_CODE")
    private Currency currency;

    @Column(name = "DISPLAY_BALANCE", precision = 18, scale = 3)
    private BigDecimal displayBalance;
}
