package com.example.bankcards.service;

import com.example.bankcards.service.exception.card.CardCryptoException;
import com.example.bankcards.service.impl.CardCryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CardCryptoServiceTest {
    private CardCryptoServiceImpl cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CardCryptoServiceImpl();
        String secret = "26C7mFn/ZOTMq1+caJYNDw==";
        ReflectionTestUtils.setField(cryptoService, "secretKey", secret);
        cryptoService.init();
    }

    @Test
    @DisplayName("Should encrypt and decrypt card number correctly")
    void testEncryptDecrypt() {
        String original = "1234567812345678";

        String encrypted = cryptoService.encryptCardNumber(original);
        String decrypted = cryptoService.decryptCardNumber(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("Should mask decrypted card number")
    void testMaskDecryptedCardNumber() {
        String original = "1234567812345678";
        String encrypted = cryptoService.encryptCardNumber(original);

        String masked = cryptoService.maskCardNumberDecrypted(encrypted);

        assertThat(masked).isEqualTo("**** **** **** 5678");
    }

    @Test
    @DisplayName("Should mask short card number with ****")
    void testMaskShortCardNumber() {
        String result = CardCryptoServiceImpl.maskCardNumber("123");
        assertThat(result).isEqualTo("****");
    }

    @Test
    @DisplayName("Should throw CardCryptoException on encryption error")
    void testEncryptException() {
        ReflectionTestUtils.setField(cryptoService, "secretKey", "short-key");

        assertThatThrownBy(() -> cryptoService.init())
                .isInstanceOf(CardCryptoException.class)
                .hasMessageContaining("Error initializing crypto key");
    }

    @Test
    @DisplayName("Should throw CardCryptoException on decryption error")
    void testDecryptException() {
        String brokenData = "broken-data";
        assertThatThrownBy(() -> cryptoService.decryptCardNumber(brokenData))
                .isInstanceOf(CardCryptoException.class)
                .hasMessageContaining("Error while decrypting card number");
    }
}
