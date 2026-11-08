package com.banking.model;

import com.banking.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Version
    private Long version;

    protected Account() { }

    public Account(UUID id, BigDecimal openingBalance) {
        this.id = id;
        this.balance = openingBalance;
    }

    public UUID getId() { return id; }
    public BigDecimal getBalance() { return balance; }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Debit amount must be positive"
            );
        }

        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account: " + this.id
            );
        }

        this.balance = this.balance.subtract(amount);
    }
}
