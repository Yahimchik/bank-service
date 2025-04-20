package com.example.bankcards.service.impl;

import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.Transaction;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.entities.enums.TransactionType;
import com.example.bankcards.factory.TransactionFactory;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardLimitRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardCryptoService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.service.exception.card.CardLimitNotFoundException;
import com.example.bankcards.service.exception.card.CardNotActiveException;
import com.example.bankcards.service.exception.card.CardNotFoundException;
import com.example.bankcards.service.exception.transaction.*;
import com.example.bankcards.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CardLimitRepository cardLimitRepository;
    private final TransactionMapper transactionMapper;
    private final CardCryptoService cardCryptoService;
    private final TransactionFactory transactionFactory;

    @Override
    @Transactional
    public void withdraw(UUID cardId, TransactionRequestDto dto, UUID userId) {
        log.info("Attempting withdrawal of {} from card {} by user {}", dto.getAmount(), cardId, userId);

        Card card = getValidatedCard(cardId, userId);

        if (card.getBalance().compareTo(dto.getAmount()) < 0) {
            log.warn("Insufficient funds for card {}. Available: {}, Requested: {}", cardId, card.getBalance(), dto.getAmount());
            throw new InsufficientFundsException("Insufficient funds");
        }

        validateLimit(card, dto.getAmount(), TransactionType.WITHDRAWAL);

        card.setBalance(card.getBalance().subtract(dto.getAmount()));
        saveCards(card);

        transactionRepository.save(transactionFactory.create(dto, card, TransactionType.WITHDRAWAL));

        log.info("Withdrawal successful: {} withdrawn from card {}", dto.getAmount(), cardId);
    }

    @Override
    @Transactional
    public void transfer(UUID fromCardId, UUID toCardId, TransactionRequestDto dto, UUID userId) {
        log.info("Attempting transfer of {} from card {} to card {} by user {}", dto.getAmount(), fromCardId, toCardId, userId);

        if (fromCardId.equals(toCardId)) {
            log.warn("Attempted transfer to the same card {}", fromCardId);
            throw new TransferToSameCardException("Cannot transfer to the same card");
        }

        Card fromCard = getValidatedCard(fromCardId, userId);
        Card toCard = getValidatedCard(toCardId, userId);

        if (fromCard.getBalance().compareTo(dto.getAmount()) < 0) {
            log.warn("Insufficient funds on sender card {}. Available: {}, Requested: {}", fromCardId, fromCard.getBalance(), dto.getAmount());
            throw new InsufficientFundsException("Insufficient funds on sender card");
        }

        validateLimit(fromCard, dto.getAmount(), TransactionType.TRANSFER);

        fromCard.setBalance(fromCard.getBalance().subtract(dto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(dto.getAmount()));
        saveCards(fromCard, toCard);

        transactionRepository.save(transactionFactory.create(dto, fromCard, TransactionType.TRANSFER));
        transactionRepository.save(transactionFactory.create(dto, toCard, TransactionType.DEPOSIT));

        log.info("Transfer successful: {} transferred from card {} to card {}", dto.getAmount(), fromCardId, toCardId);
    }

    @Override
    @Transactional
    public void deposit(UUID cardId, TransactionRequestDto dto, UUID userId) {
        log.info("Attempting deposit of {} to card {} by user {}", dto.getAmount(), cardId, userId);

        Card card = getValidatedCard(cardId, userId);

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid deposit amount {} for card {}", dto.getAmount(), cardId);
            throw new InvalidDepositAmountException("Deposit amount must be positive");
        }

        validateLimit(card, dto.getAmount(), TransactionType.DEPOSIT);

        card.setBalance(card.getBalance().add(dto.getAmount()));
        saveCards(card);

        Transaction transaction = transactionFactory.create(dto, card, TransactionType.DEPOSIT);
        transactionRepository.save(transaction);

        log.info("Deposit successful: {} added to card {}", dto.getAmount(), cardId);
    }

    @Override
    public Page<TransactionResponseDto> getAllTransactions(TransactionFilterDto filter, Pageable pageable, UserPrincipal user) {
        if (!user.getRoles().contains("ADMIN")) {
            if (filter.getCardId() != null) {
                getValidatedCard(filter.getCardId(), user.getId());
            }
            filter.setUserId(user.getId());
            log.info("User {} is not ADMIN. Filtering transactions by userId", user.getId());
        } else {
            log.info("Admin {} requested transactions with filter {}", user.getId(), filter);
        }

        return transactionRepository.findAll(
                TransactionSpecification.withFilters(filter),
                pageable
        ).map(transactionMapper::convertToTransactionResponseDto);
    }

    private Card getValidatedCard(UUID cardId, UUID userId) {
        log.info("Validating access to card {} for user {}", cardId, userId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card {} not found", cardId);
                    return new CardNotFoundException(cardId);
                });

        if (!card.getUser().getId().equals(userId)) {
            log.warn("User {} tried to access card {} owned by another user", userId, cardId);
            throw new CardOwnershipException("Access denied to card");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            String masked = showCardNumber(card);
            log.warn("Card {} is not active (status = {}). Access denied", masked, card.getStatus());
            throw new CardNotActiveException("Card is blocked or expired " + masked);
        }

        return card;
    }

    private void validateLimit(Card card, BigDecimal amount, TransactionType type) {
        log.info("Validating limits for card {} and amount {}", card.getId(), amount);

        CardLimit cardLimit = cardLimitRepository.findByCardIdAndTransactionType(card.getId(), type)
                .orElseThrow(() -> {
                    log.error("Card limit not found for card {}", card.getId());
                    return new CardLimitNotFoundException(card.getId());
                });

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<TransactionType> types = List.of(type);

        BigDecimal dailySpent = transactionRepository.sumAmountByCardAndTimestampAfter(card, startOfDay, types);
        BigDecimal monthlySpent = transactionRepository.sumAmountByCardAndTimestampAfter(card, startOfMonth, types);

        log.info("Card {} daily spent: {}, monthly spent: {}", card.getId(), dailySpent, monthlySpent);

        if (card.getStatus() != CardStatus.EXPIRED) {
            if (dailySpent.add(amount).compareTo(cardLimit.getDailyLimit()) > 0) {
                log.warn("Daily limit exceeded for card {}: attempted {}, limit {}", card.getId(), amount, cardLimit.getDailyLimit());
                throw new DailyLimitExceededException("Daily limit exceeded");
            }
            if (monthlySpent.add(amount).compareTo(cardLimit.getMonthlyLimit()) > 0) {
                log.warn("Monthly limit exceeded for card {}: attempted {}, limit {}", card.getId(), amount, cardLimit.getMonthlyLimit());
                throw new MonthlyLimitExceededException("Monthly limit exceeded");
            }
        }
    }

    private String showCardNumber(Card card) {
        return cardCryptoService.maskCardNumberDecrypted(card.getCardNumberEncrypted());
    }

    private void saveCards(Card... cards) {
        cardRepository.saveAll(List.of(cards));
        log.info("Saved {} card(s) to repository", cards.length);
    }
}
