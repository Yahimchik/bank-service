package com.example.bankcards.service.exception.transaction;

public class MonthlyLimitExceededException extends RuntimeException {
    public MonthlyLimitExceededException(String message) {
        super(message);
    }
}
