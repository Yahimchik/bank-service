package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardLimitRequestDto;
import com.example.bankcards.dto.card.CardLimitResponseDto;

import java.util.List;
import java.util.UUID;

public interface CardLimitService {
    List<CardLimitResponseDto> getCardLimit(UUID cardId, UUID userId, List<String> roles);

    CardLimitResponseDto setCardLimit(UUID cardId, CardLimitRequestDto cardLimitRequestDto);
}
