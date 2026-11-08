package com.banking.controller;

import com.banking.dto.*;
import com.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final BankingService banking;

    public AccountController(BankingService banking) { this.banking = banking; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return banking.createAccount(request.accountId(), request.openingBalance());
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) { return banking.getAccount(id); }

    @PostMapping("/{id}/deposits")
    public AccountResponse deposit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return banking.deposit(id, request.amount());
    }

    @PostMapping("/{id}/withdrawals")
    public AccountResponse withdraw(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return banking.withdraw(id, request.amount());
    }

    @GetMapping("/{id}/transactions")
    public java.util.List<TransactionResponse> history(@PathVariable UUID id) {
        return banking.history(id);
    }
}
