package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardFilterDto;
import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardResponseDto createCard(CardRequestDto request, UUID userId);

    Page<CardResponseDto> getAllCards(CardFilterDto filter, PageRequest pageRequest);

    List<CardResponseDto> getUserCards(UUID userId);

    void blockCard(UUID cardId);

    void activateCard(UUID cardId);

    void deleteCard(UUID cardId);

    void requestCardBlocking(UUID cardId, UUID userId);

    void rejectCardBlockRequest(UUID cardId, UserPrincipal adminUser);
}
