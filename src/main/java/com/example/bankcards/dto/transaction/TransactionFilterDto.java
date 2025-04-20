package com.example.bankcards.dto.transaction;

import com.example.bankcards.entities.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionFilterDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TransactionType transactionType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private UUID userId;
    private UUID cardId;
}