package com.banking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountRequest(
    @NotNull UUID accountId,
    @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal openingBalance
) { }
