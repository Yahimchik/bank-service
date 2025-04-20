package com.example.bankcards.data;

import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.entities.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionTestData {

    public static TransactionRequestDto buildTransactionRequestDto() {
        return TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(50))
                .description("transaction")
                .build();
    }

    public static TransactionFilterDto buildTransactionFilterDto() {
        return TransactionFilterDto.builder()
                .startDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .endDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .transactionType(TransactionType.TRANSFER)
                .minAmount(BigDecimal.valueOf(50))
                .maxAmount(BigDecimal.valueOf(500))
                .build();
    }
}
