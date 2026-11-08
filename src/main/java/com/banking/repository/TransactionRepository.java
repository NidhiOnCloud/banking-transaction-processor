package com.banking.repository;

import com.banking.model.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntry, UUID> {
    List<TransactionEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
