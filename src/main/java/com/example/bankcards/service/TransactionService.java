package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.TransactionFilterDto;
import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    void withdraw(UUID cardId, TransactionRequestDto dto, UUID userId);

    void transfer(UUID fromCardId, UUID toCardId, TransactionRequestDto dto, UUID userId);

    void deposit(UUID cardId, TransactionRequestDto dto, UUID userId);

    Page<TransactionResponseDto> getAllTransactions(TransactionFilterDto filter, Pageable pageable, UserPrincipal user);
}

