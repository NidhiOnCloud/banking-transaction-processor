package com.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_account_time", columnList = "account_id, created_at")
})
public class TransactionEntry {
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private UUID referenceId;

    @Column(nullable = false)
    private Instant createdAt;

    protected TransactionEntry() { }

    public TransactionEntry(UUID accountId, TransactionType type, BigDecimal amount, UUID referenceId) {
        this.id = UUID.randomUUID();
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public UUID getReferenceId() { return referenceId; }
    public Instant getCreatedAt() { return createdAt; }
}
