package com.example.bankcards.dto.card;

import com.example.bankcards.entities.enums.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CardFilterDto {
    private UUID userId;
    private CardStatus status;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private Boolean isDeleted;
    private Boolean requestedForBlocking;
}
