package com.example.bankcards.service.exception.transaction;

public class CardOwnershipException extends RuntimeException {
    public CardOwnershipException(String message) {
        super(message);
    }
}
