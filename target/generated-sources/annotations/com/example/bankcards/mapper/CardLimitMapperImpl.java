package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardLimitResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.CardLimit;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-20T17:08:15+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class CardLimitMapperImpl implements CardLimitMapper {

    @Override
    public CardLimitResponseDto convertToCardLimitDto(CardLimit cardLimit) {
        if ( cardLimit == null ) {
            return null;
        }

        CardLimitResponseDto cardLimitResponseDto = new CardLimitResponseDto();

        cardLimitResponseDto.setCardId( cardLimitCardId( cardLimit ) );
        cardLimitResponseDto.setTransactionType( cardLimit.getTransactionType() );
        cardLimitResponseDto.setDailyLimit( cardLimit.getDailyLimit() );
        cardLimitResponseDto.setMonthlyLimit( cardLimit.getMonthlyLimit() );

        return cardLimitResponseDto;
    }

    @Override
    public CardLimit convertToCardLimit(CardLimitResponseDto cardLimitResponseDto) {
        if ( cardLimitResponseDto == null ) {
            return null;
        }

        CardLimit cardLimit = new CardLimit();

        cardLimit.setCard( cardLimitResponseDtoToCard( cardLimitResponseDto ) );
        cardLimit.setTransactionType( cardLimitResponseDto.getTransactionType() );
        cardLimit.setDailyLimit( cardLimitResponseDto.getDailyLimit() );
        cardLimit.setMonthlyLimit( cardLimitResponseDto.getMonthlyLimit() );

        return cardLimit;
    }

    private UUID cardLimitCardId(CardLimit cardLimit) {
        if ( cardLimit == null ) {
            return null;
        }
        Card card = cardLimit.getCard();
        if ( card == null ) {
            return null;
        }
        UUID id = card.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected Card cardLimitResponseDtoToCard(CardLimitResponseDto cardLimitResponseDto) {
        if ( cardLimitResponseDto == null ) {
            return null;
        }

        Card card = new Card();

        card.setId( cardLimitResponseDto.getCardId() );

        return card;
    }
}
