package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.entities.CardLimit;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CardLimitMapper {

    @Mapping(source = "card.id", target = "cardId")
    CardLimitResponseDto convertToCardLimitDto(CardLimit cardLimit);

    @Mapping(source = "cardId", target = "card.id")
    CardLimit convertToCardLimit(CardLimitResponseDto cardLimitResponseDto);
}
