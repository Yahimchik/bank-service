package com.example.bankcards.service.impl;

import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardStatusUpdaterService {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredCardStatuses() {
        List<Card> activeCards = cardRepository.findAllByStatus(CardStatus.ACTIVE);

        LocalDate today = LocalDate.now();

        for (Card card : activeCards) {
            if (card.getExpirationDate().isBefore(today)) {
                card.setStatus(CardStatus.EXPIRED);
                log.info("Card {} has expired", card.getId());
            }
        }

        cardRepository.saveAll(activeCards);
    }
}
