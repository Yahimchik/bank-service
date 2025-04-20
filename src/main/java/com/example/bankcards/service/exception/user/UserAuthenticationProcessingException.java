package com.example.bankcards.service.exception.user;

public class UserAuthenticationProcessingException extends RuntimeException {
    public UserAuthenticationProcessingException(String message) {
        super(message);
    }
}
