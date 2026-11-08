package com.banking.dto;

import com.banking.model.TransactionEntry;
import com.banking.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(UUID transactionId, UUID accountId, TransactionType type,
                                  BigDecimal amount, UUID referenceId, Instant createdAt) {
    public static TransactionResponse from(TransactionEntry entry) {
        return new TransactionResponse(entry.getId(), entry.getAccountId(), entry.getType(),
                entry.getAmount(), entry.getReferenceId(), entry.getCreatedAt());
    }
}
