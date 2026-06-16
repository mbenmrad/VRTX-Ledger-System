package com.vrtx.ledgersystem.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "owner_reference", length = 100)
    private String ownerReference;

    @Column(nullable = false, length = 3)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Account() {
    }

    public Account(AccountType accountType, String ownerReference, String currency) {
        this.accountType = accountType;
        this.ownerReference = ownerReference;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum AccountType {
        USER, MERCHANT, FEES, SETTLEMENT
    }
}
