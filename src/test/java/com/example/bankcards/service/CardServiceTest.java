package com.example.bankcards.service;

import com.example.bankcards.data.CardTestData;
import com.example.bankcards.data.UserTestData;
import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.User;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.factory.CardFactory;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardLimitRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.exception.card.BlockingRequestException;
import com.example.bankcards.service.exception.card.CardNotActiveException;
import com.example.bankcards.service.exception.user.UserNotFoundException;
import com.example.bankcards.service.impl.CardServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    private static final UUID USER_ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");
    private static final UUID CARD_ID = UUID.fromString("0a05885c-2386-4e21-af1a-eb5f473249e3");

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardCryptoService cardCryptoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardLimitRepository cardLimitRepository;

    @Mock
    private CardFactory cardFactory;

    @InjectMocks
    private CardServiceImpl cardService;
    private User user;
    private Card card;
    private Card savedCard;
    private CardLimit cardLimit;
    private CardRequestDto cardRequestDto;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    @Test
    void testCreateCard_success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cardCryptoService.encryptCardNumber(cardRequestDto.getCardNumberEncrypted()))
                .thenReturn(savedCard.getCardNumberEncrypted());
        when(cardRepository.existsByCardNumberEncrypted(savedCard.getCardNumberEncrypted()))
                .thenReturn(false);
        when(cardFactory.createCard(cardRequestDto, user, savedCard.getCardNumberEncrypted()))
                .thenReturn(card);
        when(cardFactory.createDefaultLimitsForAllTransactionTypes(savedCard))
                .thenReturn(List.of(cardLimit));
        when(cardRepository.save(card)).thenReturn(savedCard);
        when(cardMapper.convertToCardResponseDto(savedCard)).thenReturn(cardResponseDto);
        when(cardCryptoService.decryptCardNumber(savedCard.getCardNumberEncrypted()))
                .thenReturn(cardRequestDto.getCardNumberEncrypted());

        CardResponseDto result = cardService.createCard(cardRequestDto, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(cardResponseDto);

        verify(userRepository).findById(USER_ID);
        verify(cardCryptoService).encryptCardNumber(cardRequestDto.getCardNumberEncrypted());
        verify(cardRepository).existsByCardNumberEncrypted(savedCard.getCardNumberEncrypted());
        verify(cardFactory).createCard(cardRequestDto, user, savedCard.getCardNumberEncrypted());
        verify(cardFactory).createDefaultLimitsForAllTransactionTypes(savedCard);
        verify(cardRepository).save(card);
        verify(cardLimitRepository).saveAll(List.of(cardLimit));
        verify(cardMapper).convertToCardResponseDto(savedCard);
        verify(cardCryptoService).decryptCardNumber(savedCard.getCardNumberEncrypted());
    }

    @Test
    void testCreateCard_userNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(new CardRequestDto(), USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void testBlockCard_cardFound_statusChanged() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        cardService.blockCard(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    void testActivateCard_success() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        cardService.activateCard(CARD_ID);

        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(card);
    }

    @Test
    void testDeleteCard_setsDeletedFlag() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        cardService.deleteCard(CARD_ID);

        assertThat(card.isDeleted()).isTrue();
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    void testRequestCardBlocking_accessDenied() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        UUID otherUser = UUID.randomUUID();

        assertThatThrownBy(() -> cardService.requestCardBlocking(CARD_ID, otherUser))
                .isInstanceOf(com.example.bankcards.service.exception.auth.AccessDeniedException.class);
    }

    @Test
    void testRequestCardBlocking_cardNotActive() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.requestCardBlocking(CARD_ID, USER_ID))
                .isInstanceOf(CardNotActiveException.class);
    }

    @Test
    void testRejectCardBlockRequest_notRequested() {
        card.setRequestedForBlocking(false);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.rejectCardBlockRequest(CARD_ID, UserPrincipal.builder()
                .id(USER_ID)
                .build()))
                .isInstanceOf(BlockingRequestException.class);
    }

    @Test
    void testRequestCardBlocking_success() {
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        cardService.requestCardBlocking(CARD_ID, USER_ID);

        assertThat(card.isRequestedForBlocking()).isTrue();
        verify(cardRepository).save(card);
    }

    @Test
    void testRejectCardBlockRequest_success() {
        card.setRequestedForBlocking(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        cardService.rejectCardBlockRequest(CARD_ID, UserPrincipal.builder()
                .id(USER_ID)
                .build());

        assertThat(card.isRequestedForBlocking()).isFalse();
        verify(cardRepository).save(card);
    }

    private void createTestData() {
        user = UserTestData.buildUser();
        card = CardTestData.buildCard();
        savedCard = CardTestData.buildCardSaved();
        cardLimit = CardTestData.buildCardLimit();
        cardRequestDto = CardTestData.buildCardRequestDto();
        cardResponseDto = CardTestData.buildCardResponseDto();
    }

}
