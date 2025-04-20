package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entities.Card;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "maskedCardNumber", ignore = true)
    @Mapping(target = "expirationDate",
            expression = "java(card.getExpirationDate() != null ? card.getExpirationDate().atStartOfDay() : null)")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "requestedForBlocking", source = "requestedForBlocking")
    CardResponseDto convertToCardResponseDto(Card card);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Card convertToCard(CardRequestDto cardRequestDto);
}


