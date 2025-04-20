package com.example.bankcards.service.impl;

import com.example.bankcards.service.CardCryptoService;
import com.example.bankcards.service.exception.card.CardCryptoException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardCryptoServiceImpl implements CardCryptoService {

    @Value("${spring.application.security.crypto.secret}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;

    @PostConstruct
    public void init() {
        try {
            byte[] key = secretKey.substring(0, 16).getBytes();
            secretKeySpec = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            log.error("Failed to initialize crypto key");
            throw new CardCryptoException("Error initializing crypto key", e);
        }
    }

    @Override
    public String encryptCardNumber(String cardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            String encoded = Base64.getEncoder().encodeToString(encrypted);
            log.info("Card number encrypted successfully.");
            return encoded;
        } catch (Exception e) {
            log.error("Error while encrypting card number", e);
            throw new CardCryptoException("Error while encrypting card number", e);
        }
    }

    @Override
    public String decryptCardNumber(String encryptedCardNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            String result = new String(decrypted);
            log.info("Card number decrypted successfully.");
            return result;
        } catch (Exception e) {
            log.error("Error while decrypting card number -> " + encryptedCardNumber);
            throw new CardCryptoException("Error while decrypting card number", e);
        }
    }

    @Override
    public String maskCardNumberDecrypted(String fullCardNumber) {
        String decrypted = decryptCardNumber(fullCardNumber);
        String masked = maskCardNumber(decrypted);
        log.info("Card number masked successfully after decryption.");
        return masked;
    }

    public static String maskCardNumber(String fullCardNumber) {
        if (fullCardNumber.length() < 4) {
            log.warn("Card number too short to mask, returning ****.");
            return "****";
        }
        String last4 = fullCardNumber.substring(fullCardNumber.length() - 4);
        String masked = "**** **** **** " + last4;
        log.debug("Card number masked successfully.");
        return masked;
    }
}
