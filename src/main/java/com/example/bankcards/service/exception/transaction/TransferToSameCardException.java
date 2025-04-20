package com.example.bankcards.service.exception.transaction;

public class TransferToSameCardException extends RuntimeException {
    public TransferToSameCardException(String message) {
        super(message);
    }
}
