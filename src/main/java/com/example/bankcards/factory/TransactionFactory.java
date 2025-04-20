package com.example.bankcards.factory;

import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.Transaction;
import com.example.bankcards.entities.enums.TransactionType;
import com.example.bankcards.service.CardCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionFactory {

    private final CardCryptoService cardCryptoService;

    public Transaction create(TransactionRequestDto dto, Card card, TransactionType type) {
        String description = dto.getDescription();

        if (description == null || description.trim().isEmpty() || description.equals("transaction")) {
            String cardNumberMasked = cardCryptoService.maskCardNumberDecrypted(card.getCardNumberEncrypted());

            switch (type) {
                case DEPOSIT ->
                        description = "Card replenishment " + cardNumberMasked + " for the amount " + dto.getAmount();
                case WITHDRAWAL ->
                        description = "Withdrawal of funds from the card " + cardNumberMasked + " for the amount " + dto.getAmount();
                case TRANSFER ->
                        description = "Transfer of funds from the card " + cardNumberMasked + " for the amount " + dto.getAmount();
            }
        }

        return Transaction.builder()
                .card(card)
                .type(type)
                .amount(dto.getAmount())
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
