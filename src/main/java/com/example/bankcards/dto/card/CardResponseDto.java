package com.example.bankcards.dto.card;

import com.example.bankcards.entities.enums.CardStatus;
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
public class CardResponseDto {
    private UUID id;
    private UUID userId;
    private String maskedCardNumber;
    private LocalDateTime expirationDate;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private boolean requestedForBlocking;
}
