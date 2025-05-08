package com.bank.balancedispense.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Entity representing a bank client.
 */
@Entity
public class Client {
    @Id
    private Long id;
    private String title;
    private String name;
    private String surname;
}

