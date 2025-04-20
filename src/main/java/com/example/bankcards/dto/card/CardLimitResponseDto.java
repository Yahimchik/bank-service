package com.example.bankcards.dto.card;

import com.example.bankcards.entities.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLimitResponseDto {
    private UUID cardId;
    private TransactionType transactionType;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
}
