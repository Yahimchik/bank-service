package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardLimitRequestDto;
import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.mapper.CardLimitMapper;
import com.example.bankcards.repository.CardLimitRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardLimitService;
import com.example.bankcards.service.exception.auth.AccessDeniedException;
import com.example.bankcards.service.exception.card.CardLimitNotFoundException;
import com.example.bankcards.service.exception.card.CardNotActiveException;
import com.example.bankcards.service.exception.card.CardNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardLimitServiceImpl implements CardLimitService {

    private final CardRepository cardRepository;
    private final CardLimitRepository cardLimitRepository;
    private final CardLimitMapper cardLimitMapper;

    @Override
    @Transactional
    public List<CardLimitResponseDto> getCardLimit(UUID cardId, UUID userId, List<String> roles) {
        log.info("Getting card limit for cardId={} and userId={}", cardId, userId);

        Card card = getValidatedCard(cardId);
        validateCardIsActive(card);
        validateAccess(card, userId, roles);

        log.info("Card is valid and active. Checking card limit in repository...");

        List<CardLimitResponseDto> cardLimit = cardLimitRepository.findAllByCardId(cardId).stream()
                .map(cardLimitMapper::convertToCardLimitDto)
                .collect(Collectors.toList());

        if (cardLimit.isEmpty()) {
            log.warn("Card limits not found for cardId={}", cardId);
            throw new CardLimitNotFoundException(cardId);
        }

        log.info("Card limit found for cardId={}", cardId);
        return cardLimit;
    }

    @Override
    @Transactional
    public CardLimitResponseDto setCardLimit(UUID cardId, CardLimitRequestDto cardLimitRequestDto) {
        log.info("Setting card limit for cardId={} with dailyLimit={} and monthlyLimit={}",
                cardId,
                cardLimitRequestDto.getDailyLimit(),
                cardLimitRequestDto.getMonthlyLimit());

        Card card = getValidatedCard(cardId);
        validateCardIsActive(card);

        CardLimit cardLimit = cardLimitRepository.findByCardIdAndTransactionType(cardId, cardLimitRequestDto.getTransactionType())
                .orElseGet(() -> {
                    log.info("No existing card limit found for cardId={}, creating new one", cardId);
                    return CardLimit.builder()
                            .card(card)
                            .transactionType(cardLimitRequestDto.getTransactionType())
                            .build();
                });

        cardLimit.setCard(card);
        cardLimit.setDailyLimit(cardLimitRequestDto.getDailyLimit());
        cardLimit.setMonthlyLimit(cardLimitRequestDto.getMonthlyLimit());

        cardLimit = cardLimitRepository.save(cardLimit);
        log.info("Card limit saved for cardId={} with new limits: daily={}, monthly={}",
                cardId, cardLimit.getDailyLimit(), cardLimit.getMonthlyLimit());

        return cardLimitMapper.convertToCardLimitDto(cardLimit);
    }

    private void validateAccess(Card card, UUID userId, List<String> roles) {
        if (!card.getUser().getId().equals(userId) && !roles.contains("ADMIN")) {
            log.warn("Access denied: userId={} does not own cardId={} and is not ADMIN", userId, card.getId());
            throw new AccessDeniedException("Access denied for user: " + card.getUser().getEmail());
        }
        log.info("Access granted to userId={} for cardId={}", userId, card.getId());
    }

    private void validateCardIsActive(Card card) {
        if (card.getStatus() == CardStatus.EXPIRED || card.getStatus() == CardStatus.BLOCKED) {
            log.warn("Card is not active: cardId={} status={}", card.getId(), card.getStatus());
            throw new CardNotActiveException("Card is expired or blocked");
        }
        log.info("Card is active: cardId={}", card.getId());
    }

    private Card getValidatedCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card not found: cardId={}", cardId);
                    return new CardNotFoundException(cardId);
                });
    }
}
