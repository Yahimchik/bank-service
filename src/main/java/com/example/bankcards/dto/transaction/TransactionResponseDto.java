package com.example.bankcards.dto.transaction;

import com.example.bankcards.entities.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponseDto {
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;
}
