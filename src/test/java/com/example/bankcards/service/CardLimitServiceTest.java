package com.example.bankcards.service;

import com.example.bankcards.data.CardTestData;
import com.example.bankcards.dto.card.CardLimitRequestDto;
import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.mapper.CardLimitMapper;
import com.example.bankcards.repository.CardLimitRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.exception.auth.AccessDeniedException;
import com.example.bankcards.service.exception.card.CardLimitNotFoundException;
import com.example.bankcards.service.exception.card.CardNotActiveException;
import com.example.bankcards.service.exception.card.CardNotFoundException;
import com.example.bankcards.service.impl.CardLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardLimitServiceTest {
    private static final UUID CARD_ID = UUID.fromString("0a05885c-2386-4e21-af1a-eb5f473249e3");
    private static final UUID USER_ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardLimitRepository cardLimitRepository;

    @Mock
    private CardLimitMapper cardLimitMapper;

    @InjectMocks
    private CardLimitServiceImpl cardLimitService;
    private CardLimit cardLimit;
    private CardLimit cardLimitSaved;
    private Card card;
    private CardLimitResponseDto cardLimitResponseDto;
    private CardLimitResponseDto cardLimitResponseDtoUpdated;
    private CardLimitRequestDto cardLimitRequestDto;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    @Test
    void shouldGetCardLimit() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardLimitRepository.findAllByCardId(CARD_ID)).thenReturn(List.of(cardLimit));
        when(cardLimitMapper.convertToCardLimitDto(cardLimit)).thenReturn(cardLimitResponseDto);

        List<CardLimitResponseDto> result = cardLimitService.getCardLimit(CARD_ID, USER_ID, List.of("USER"));

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(cardLimitResponseDto);
    }

    @Test
    void shouldThrowIfCardNotFound() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardLimitService.getCardLimit(CARD_ID, USER_ID, List.of("USER")))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void shouldThrowIfCardNotActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardLimitService.getCardLimit(CARD_ID, USER_ID, List.of("USER")))
                .isInstanceOf(CardNotActiveException.class);
    }

    @Test
    void shouldThrowIfAccessDenied() {
        UUID anotherUserId = UUID.randomUUID();
        card.getUser().setId(anotherUserId);

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardLimitService.getCardLimit(CARD_ID, USER_ID, List.of("USER")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldThrowIfCardLimitNotFound() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardLimitRepository.findAllByCardId(CARD_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> cardLimitService.getCardLimit(CARD_ID, USER_ID, List.of("ADMIN")))
                .isInstanceOf(CardLimitNotFoundException.class);
    }

    @Test
    void shouldSetNewCardLimit() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardLimitRepository.findByCardIdAndTransactionType(CARD_ID, cardLimitRequestDto.getTransactionType()))
                .thenReturn(Optional.empty());

        when(cardLimitRepository.save(any())).thenReturn(cardLimitSaved);
        when(cardLimitMapper.convertToCardLimitDto(cardLimitSaved)).thenReturn(cardLimitResponseDtoUpdated);

        CardLimitResponseDto result = cardLimitService.setCardLimit(CARD_ID, cardLimitRequestDto);

        assertThat(result).isEqualTo(cardLimitResponseDtoUpdated);
    }

    private void createTestData() {
        card = CardTestData.buildCard();
        cardLimit = CardTestData.buildCardLimit();
        cardLimitSaved = CardTestData.buildCardLimitSaved();
        cardLimitResponseDto = CardTestData.buildCardLimitResponseDto();
        cardLimitResponseDtoUpdated = CardTestData.buildCardLimitResponseDtoUpdated();
        cardLimitRequestDto = CardTestData.buildCardLimitRequestDto();
    }
}
