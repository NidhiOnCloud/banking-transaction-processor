package com.banking.controller;

import com.banking.dto.TransferRequest;
import com.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final BankingService banking;

    public TransferController(BankingService banking) { this.banking = banking; }

    @PostMapping
    public Map<String, Object> transfer(@Valid @RequestBody TransferRequest request) {
        return Map.of("transferId", banking.transfer(request.fromAccountId(),
                request.toAccountId(), request.amount()));
    }
}
