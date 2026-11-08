package com.banking.dto;

import com.banking.model.Account;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID accountId, BigDecimal balance) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getBalance());
    }
}
