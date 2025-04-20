package com.example.bankcards.service;

public interface CardCryptoService {
    String encryptCardNumber(String cardNumber);

    String decryptCardNumber(String encryptedCardNumber);

    String maskCardNumberDecrypted(String fullCardNumber);
}
