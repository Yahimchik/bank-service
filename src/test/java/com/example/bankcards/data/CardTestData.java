package com.example.bankcards.data;

import com.example.bankcards.dto.card.CardLimitRequestDto;
import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.entities.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CardTestData {
    private static final UUID CARD_ID = UUID.fromString("0a05885c-2386-4e21-af1a-eb5f473249e3");
    private static final UUID CARD_LIMIT_ID = UUID.fromString("ded94437-6c3c-4cc1-bf8e-d88e9bd8a11a");
    private static final UUID USER_ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");
    private static final UUID TO_CARD_ID = UUID.fromString("bd721116-820a-4647-9b9e-70f3e526fbcb");

    private CardTestData() {
    }

    public static Card buildCard() {
        return Card.builder()
                .id(CARD_ID)
                .user(UserTestData.buildUser())
                .cardNumberEncrypted("123456789023456")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .requestedForBlocking(false)
                .build();
    }

    public static Card buildCardTo(){
        Card card = buildCard();
        card.setId(TO_CARD_ID);
        return card;
    }

    public static Card buildCardSaved(){
        Card card = buildCard();
        card.setCardNumberEncrypted("ENCRYPTED");
        return card;
    }

    public static Card buildExpiredCard() {
        Card card = buildCard();
        card.setExpirationDate(LocalDate.now().minusDays(1));
        return card;
    }

    public static Card buildCardForDeleting() {
        return Card.builder()
                .id(CARD_ID)
                .user(UserTestData.buildUser())
                .cardNumberEncrypted("123456789023456")
                .expirationDate(LocalDate.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .requestedForBlocking(false)
                .build();
    }

    public static CardRequestDto buildCardRequestDto() {
        return CardRequestDto.builder()
                .cardNumberEncrypted("1234567890123456")
                .expirationDate(LocalDateTime.now().plusYears(1))
                .balance(BigDecimal.valueOf(100))
                .build();
    }

    public static CardResponseDto buildCardResponseDto() {
        return CardResponseDto.builder()
                .id(CARD_ID)
                .userId(USER_ID)
                .maskedCardNumber("ENCRYPTED")
                .expirationDate(LocalDateTime.now().plusYears(1))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .requestedForBlocking(false)
                .build();
    }

    public static CardLimit buildCardLimit() {
        return CardLimit.builder()
                .id(CARD_LIMIT_ID)
                .dailyLimit(BigDecimal.valueOf(100))
                .monthlyLimit(BigDecimal.valueOf(100))
                .build();
    }

    public static CardLimit buildCardLimitSaved() {
        return CardLimit.builder()
                .id(CARD_LIMIT_ID)
                .dailyLimit(BigDecimal.valueOf(500))
                .monthlyLimit(BigDecimal.valueOf(1000))
                .build();
    }

    public static CardLimitResponseDto buildCardLimitResponseDto() {
        return CardLimitResponseDto.builder()
                .cardId(CARD_ID)
                .dailyLimit(BigDecimal.valueOf(100))
                .monthlyLimit(BigDecimal.valueOf(100))
                .build();
    }

    public static CardLimitResponseDto buildCardLimitResponseDtoUpdated() {
        return CardLimitResponseDto.builder()
                .cardId(CARD_ID)
                .dailyLimit(BigDecimal.valueOf(500))
                .monthlyLimit(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.WITHDRAWAL)
                .build();
    }

    public static CardLimitRequestDto buildCardLimitRequestDto() {
        return CardLimitRequestDto.builder()
                .dailyLimit(BigDecimal.valueOf(500))
                .monthlyLimit(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.WITHDRAWAL)
                .build();
    }
}
