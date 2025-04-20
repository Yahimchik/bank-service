package com.example.bankcards.service;

import com.example.bankcards.data.CardTestData;
import com.example.bankcards.data.TransactionTestData;
import com.example.bankcards.data.UserTestData;
import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import com.example.bankcards.entities.Transaction;
import com.example.bankcards.entities.enums.TransactionType;
import com.example.bankcards.factory.TransactionFactory;
import com.example.bankcards.mapper.TransactionMapper;
import com.example.bankcards.repository.CardLimitRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.exception.transaction.InsufficientFundsException;
import com.example.bankcards.service.exception.transaction.InvalidDepositAmountException;
import com.example.bankcards.service.exception.transaction.TransferToSameCardException;
import com.example.bankcards.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final UUID USER_ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");
    private static final UUID CARD_ID = UUID.fromString("0a05885c-2386-4e21-af1a-eb5f473249e3");
    private static final UUID TO_CARD_ID = UUID.fromString("bd721116-820a-4647-9b9e-70f3e526fbcb");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardLimitRepository cardLimitRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private TransactionFactory transactionFactory;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Card card;
    private Card toCard;
    private CardLimit cardLimit;
    private TransactionRequestDto requestDto;
    private TransactionFilterDto filter;
    private UserPrincipal user;

    @BeforeEach
    void setUp() {
        createTestData();
    }

    @Test
    void withdraw_shouldSucceed_whenEnoughBalanceAndLimit() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardLimitRepository.findByCardIdAndTransactionType(CARD_ID, TransactionType.WITHDRAWAL)).thenReturn(Optional.of(cardLimit));
        when(transactionRepository.sumAmountByCardAndTimestampAfter(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionFactory.create(requestDto, card, TransactionType.WITHDRAWAL))
                .thenReturn(new Transaction());

        transactionService.withdraw(CARD_ID, requestDto, USER_ID);

        verify(cardRepository).saveAll(List.of(card));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deposit_shouldSucceed_whenValidAmount() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardLimitRepository.findByCardIdAndTransactionType(CARD_ID, TransactionType.DEPOSIT)).thenReturn(Optional.of(cardLimit));
        when(transactionRepository.sumAmountByCardAndTimestampAfter(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionFactory.create(requestDto, card, TransactionType.DEPOSIT))
                .thenReturn(new Transaction());

        transactionService.deposit(CARD_ID, requestDto, USER_ID);

        verify(cardRepository).saveAll(List.of(card));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_shouldSucceed_whenEnoughBalanceAndLimit() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardRepository.findById(TO_CARD_ID)).thenReturn(Optional.of(toCard));
        when(cardLimitRepository.findByCardIdAndTransactionType(CARD_ID, TransactionType.TRANSFER)).thenReturn(Optional.of(cardLimit));
        when(transactionRepository.sumAmountByCardAndTimestampAfter(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transactionFactory.create(any(), any(), any()))
                .thenReturn(new Transaction());

        transactionService.transfer(CARD_ID, TO_CARD_ID, requestDto, USER_ID);

        verify(cardRepository).saveAll(List.of(card, toCard));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void withdraw_shouldThrowException_whenNotEnoughBalance() {
        card.setBalance(BigDecimal.valueOf(10));
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdraw(CARD_ID, requestDto, USER_ID));
    }

    @Test
    void deposit_shouldThrowException_whenAmountIsZeroOrNegative() {
        requestDto.setAmount(BigDecimal.ZERO);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThrows(InvalidDepositAmountException.class,
                () -> transactionService.deposit(CARD_ID, requestDto, USER_ID));
    }

    @Test
    void transfer_shouldThrowException_whenSameCardIds() {
        assertThrows(TransferToSameCardException.class,
                () -> transactionService.transfer(CARD_ID, CARD_ID, requestDto, USER_ID));
    }

    @Test
    void getAllTransactions_shouldApplyUserFilter_ifNotAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findAll(
                ArgumentMatchers.<Specification<Transaction>>any(), eq(pageable)))
                .thenReturn(Page.empty());


        Page<TransactionResponseDto> result = transactionService.getAllTransactions(filter, pageable, user);

        assertNotNull(result);
    }

    @Test
    void getAllTransactions_shouldNotApplyUserFilter_ifAdmin() {
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findAll(
                ArgumentMatchers.<Specification<Transaction>>any(), eq(pageable)))
                .thenReturn(Page.empty());

        Page<TransactionResponseDto> result = transactionService.getAllTransactions(filter, pageable, user);

        assertNotNull(result);
        assertNull(filter.getUserId(), "userId should not be set for ADMIN");
        verify(transactionRepository).findAll(ArgumentMatchers.<Specification<Transaction>>any(), eq(pageable));
    }


    private void createTestData() {
        card = CardTestData.buildCard();
        toCard = CardTestData.buildCardTo();
        cardLimit = CardTestData.buildCardLimit();
        requestDto = TransactionTestData.buildTransactionRequestDto();
        filter = TransactionTestData.buildTransactionFilterDto();
        user = UserTestData.buildUserPrincipal();
    }
}
