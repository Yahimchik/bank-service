package com.example.bankcards.service.exception.card;

import java.util.UUID;

public class CardLimitNotFoundException extends RuntimeException {
    public CardLimitNotFoundException(UUID cardId) {
        super("Card limits not found for card: " + cardId);
    }
}