package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardFilterDto;
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
import com.example.bankcards.service.CardCryptoService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.exception.auth.AccessDeniedException;
import com.example.bankcards.service.exception.card.BlockingRequestException;
import com.example.bankcards.service.exception.card.CardAlreadyExistsException;
import com.example.bankcards.service.exception.card.CardNotActiveException;
import com.example.bankcards.service.exception.card.CardNotFoundException;
import com.example.bankcards.service.exception.user.UserNotFoundException;
import com.example.bankcards.specification.CardSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardCryptoService cardCryptoService;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardLimitRepository cardLimitRepository;
    private final CardFactory cardFactory;

    @Override
    @Transactional
    public CardResponseDto createCard(CardRequestDto request, UUID userId) {
        log.info("Creating a new card for user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        String encryptedCardNumber = cardCryptoService.encryptCardNumber(request.getCardNumberEncrypted());
        log.info("Encrypted card number: {}", encryptedCardNumber);

        ensureCardDoesNotExist(encryptedCardNumber);
        log.info("Card number uniqueness check passed");

        Card card = cardFactory.createCard(request, user, encryptedCardNumber);

        Card savedCard = cardRepository.save(card);
        List<CardLimit> defaultLimits = cardFactory.createDefaultLimitsForAllTransactionTypes(savedCard);
        cardLimitRepository.saveAll(defaultLimits);

        log.info("Card successfully created with ID: {}", savedCard.getId());
        return buildMaskedCardResponse(savedCard);
    }

    @Override
    public Page<CardResponseDto> getAllCards(CardFilterDto filter, PageRequest pageRequest) {
        log.info("Fetching all cards with filter: {}", filter);
        Pageable pageable = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
        Page<Card> cardsPage = cardRepository.findAll(CardSpecification.withFilters(filter), pageable);
        return cardsPage.map(this::buildMaskedCardResponse);
    }

    @Override
    public List<CardResponseDto> getUserCards(UUID userId) {
        log.info("Fetching all cards for user with ID: {}", userId);
        return cardRepository.findAllByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(this::buildMaskedCardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void blockCard(UUID cardId) {
        log.info("Blocking card with ID: {}", cardId);
        changeStatus(cardId, CardStatus.BLOCKED);
    }

    @Override
    @Transactional
    public void activateCard(UUID cardId) {
        log.info("Activating card with ID: {}", cardId);
        changeStatus(cardId, CardStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deleteCard(UUID cardId) {
        log.info("Deleting card with ID: {}", cardId);
        Card card = findCard(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setDeleted(true);
        log.info("Marking card as deleted: {}", card);
        cardRepository.save(card);
    }

    @Override
    public void requestCardBlocking(UUID cardId, UUID userId) {
        log.info("User {} requested blocking for card {}", userId, cardId);
        Card card = findCard(cardId);

        if (card.isRequestedForBlocking()) {
            log.warn("Request is already send for card {}", cardId);
            throw new BlockingRequestException("Request is already send for card: " + cardId);
        }

        if (!card.getUser().getId().equals(userId)) {
            log.warn("User {} is not authorized to block card {}", userId, cardId);
            throw new AccessDeniedException("User cannot request blocking for someone else's card");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card {} is not active and cannot be blocked", cardId);
            throw new CardNotActiveException("Only active cards can be requested for blocking");
        }

        card.setRequestedForBlocking(true);
        cardRepository.save(card);
        log.info("Blocking request for card {} has been submitted", cardId);
    }

    @Override
    public void rejectCardBlockRequest(UUID cardId, UserPrincipal adminUser) {
        log.info("Admin {} rejected blocking request for card {}", adminUser.getId(), cardId);
        Card card = findCard(cardId);

        if (!card.isRequestedForBlocking()) {
            log.warn("Card {} has no pending block request", cardId);
            throw new BlockingRequestException("Card is not requested for blocking");
        }

        card.setRequestedForBlocking(false);
        cardRepository.save(card);
        log.info("Blocking request for card {} has been rejected", cardId);
    }

    private void ensureCardDoesNotExist(String encryptedCardNumber) {
        if (cardRepository.existsByCardNumberEncrypted(encryptedCardNumber)) {
            log.warn("Attempt to create a card with existing encrypted number: {}", encryptedCardNumber);
            throw new CardAlreadyExistsException("Card already exists.");
        }
    }

    private CardResponseDto buildMaskedCardResponse(Card card) {
        log.info("Building masked response for card with ID: {}", card.getId());
        CardResponseDto dto = cardMapper.convertToCardResponseDto(card);
        String decrypted = cardCryptoService.decryptCardNumber(card.getCardNumberEncrypted());
        dto.setMaskedCardNumber(CardCryptoServiceImpl.maskCardNumber(decrypted));
        return dto;
    }

    private void changeStatus(UUID cardId, CardStatus status) {
        log.info("Changing status of card {} to {}", cardId, status);
        Card card = findCard(cardId);
        card.setStatus(status);
        card.setDeleted(false);
        cardRepository.save(card);
        log.info("Card {} status changed to {}", cardId, status);
    }

    private Card findCard(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.warn("Card with ID {} not found", cardId);
                    return new CardNotFoundException(cardId);
                });
    }
}
