package com.example.bankcards.factory;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.User;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.entities.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class CardFactory {
    public Card createCard(CardRequestDto request, User user, String encryptedCardNumber) {
        return Card.builder()
                .user(user)
                .cardNumberEncrypted(encryptedCardNumber)
                .expirationDate(request.getExpirationDate().toLocalDate())
                .status(CardStatus.ACTIVE)
                .balance(request.getBalance())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public CardLimit createDefaultLimit(Card card) {
        return CardLimit.builder()
                .card(card)
                .transactionType(TransactionType.WITHDRAWAL)
                .dailyLimit(BigDecimal.ZERO)
                .monthlyLimit(BigDecimal.ZERO)
                .build();
    }

    public List<CardLimit> createDefaultLimitsForAllTransactionTypes(Card card) {
        return Arrays.stream(TransactionType.values())
                .map(type -> CardLimit.builder()
                        .card(card)
                        .transactionType(type)
                        .dailyLimit(BigDecimal.valueOf(5_000))
                        .monthlyLimit(BigDecimal.valueOf(5_000))
                        .build())
                .toList();
    }
}
