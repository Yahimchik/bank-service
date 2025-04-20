package com.example.bankcards.service.exception.card;

public class BlockingRequestException extends RuntimeException {
    public BlockingRequestException(String message) {
        super(message);
    }
}
