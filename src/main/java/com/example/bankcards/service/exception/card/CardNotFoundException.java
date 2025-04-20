package com.example.bankcards.service.exception.card;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(UUID cardId) {
        super("Card not found: " + cardId);
    }
}