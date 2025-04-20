package com.example.bankcards.service;

import com.example.bankcards.data.CardTestData;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.impl.CardStatusUpdaterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardStatusUpdaterServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardStatusUpdaterService cardStatusUpdaterService;
    private Card validCard;
    private Card expiredCard;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    @Test
    void testUpdateExpiredCardStatuses_shouldMarkExpiredCards() {
        List<Card> cards = List.of(expiredCard, validCard);

        when(cardRepository.findAllByStatus(CardStatus.ACTIVE)).thenReturn(cards);

        cardStatusUpdaterService.updateExpiredCardStatuses();

        assertEquals(CardStatus.EXPIRED, expiredCard.getStatus());
        assertEquals(CardStatus.ACTIVE, validCard.getStatus());

        verify(cardRepository).saveAll(cards);
    }

    private void createTestData() {
        validCard = CardTestData.buildCard();
        expiredCard = CardTestData.buildExpiredCard();
    }
}
