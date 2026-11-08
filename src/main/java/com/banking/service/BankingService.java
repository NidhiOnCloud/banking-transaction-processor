package com.banking.service;

import com.banking.dto.*;
import com.banking.exception.*;
import com.banking.model.*;
import com.banking.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BankingService {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public BankingService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public AccountResponse createAccount(UUID id, BigDecimal openingBalance) {
        validateNonNegative(openingBalance);
        if (accounts.existsById(id)) throw new ConflictException("Account already exists: " + id);
        try {
            return AccountResponse.from(accounts.save(new Account(id, openingBalance)));
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Account already exists: " + id);
        }
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        return AccountResponse.from(findAccount(id));
    }

    @Transactional
    public AccountResponse deposit(UUID id, BigDecimal amount) {
        validatePositive(amount);
        Account account = accounts.findByIdForUpdate(id).orElseThrow(() ->
                new ResourceNotFoundException("Account not found: " + id));
        account.credit(amount);
        transactions.save(new TransactionEntry(id, TransactionType.DEPOSIT, amount, UUID.randomUUID()));
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse withdraw(UUID id, BigDecimal amount) {
        validatePositive(amount);
        Account account = accounts.findByIdForUpdate(id).orElseThrow(() ->
                new ResourceNotFoundException("Account not found: " + id));
        account.debit(amount);
        transactions.save(new TransactionEntry(id, TransactionType.WITHDRAWAL, amount, UUID.randomUUID()));
        return AccountResponse.from(account);
    }

    @Transactional
    public UUID transfer(UUID fromId, UUID toId, BigDecimal amount) {
        validatePositive(amount);
        if (fromId.equals(toId)) throw new IllegalArgumentException("Source and destination must differ");
        UUID firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;
        Account first = accounts.findByIdForUpdate(firstId).orElseThrow(() ->
                new ResourceNotFoundException("Account not found: " + firstId));
        Account second = accounts.findByIdForUpdate(secondId).orElseThrow(() ->
                new ResourceNotFoundException("Account not found: " + secondId));

        Account source = fromId.equals(firstId) ? first : second;
        Account destination = toId.equals(firstId) ? first : second;
        source.debit(amount);
        destination.credit(amount);

        UUID transferId = UUID.randomUUID();
        transactions.save(new TransactionEntry(fromId, TransactionType.TRANSFER_OUT, amount, transferId));
        transactions.save(new TransactionEntry(toId, TransactionType.TRANSFER_IN, amount, transferId));
        return transferId;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(UUID accountId) {
        if (!accounts.existsById(accountId)) throw new ResourceNotFoundException("Account not found: " + accountId);
        return transactions.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionResponse::from).toList();
    }

    private Account findAccount(UUID id) {
        return accounts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");
        if (amount.scale() > 2)
            throw new IllegalArgumentException("Amount must have at most two decimal places");
    }

    private void validateNonNegative(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Opening balance cannot be negative");
        if (amount.scale() > 2)
            throw new IllegalArgumentException("Amount must have at most two decimal places");
    }
}
