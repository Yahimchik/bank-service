package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardRequestDto;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.UUID;
import javax.annotation.processing.Generated;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-20T17:08:15+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class CardMapperImpl implements CardMapper {

    private final DatatypeFactory datatypeFactory;

    public CardMapperImpl() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch ( DatatypeConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public CardResponseDto convertToCardResponseDto(Card card) {
        if ( card == null ) {
            return null;
        }

        CardResponseDto cardResponseDto = new CardResponseDto();

        cardResponseDto.setUserId( cardUserId( card ) );
        cardResponseDto.setDeleted( card.isDeleted() );
        cardResponseDto.setRequestedForBlocking( card.isRequestedForBlocking() );
        cardResponseDto.setId( card.getId() );
        cardResponseDto.setStatus( card.getStatus() );
        cardResponseDto.setBalance( card.getBalance() );
        cardResponseDto.setCreatedAt( card.getCreatedAt() );

        cardResponseDto.setExpirationDate( card.getExpirationDate() != null ? card.getExpirationDate().atStartOfDay() : null );

        return cardResponseDto;
    }

    @Override
    public Card convertToCard(CardRequestDto cardRequestDto) {
        if ( cardRequestDto == null ) {
            return null;
        }

        Card card = new Card();

        card.setCardNumberEncrypted( cardRequestDto.getCardNumberEncrypted() );
        card.setExpirationDate( xmlGregorianCalendarToLocalDate( localDateTimeToXmlGregorianCalendar( cardRequestDto.getExpirationDate() ) ) );
        card.setStatus( cardRequestDto.getStatus() );
        card.setBalance( cardRequestDto.getBalance() );

        return card;
    }

    private XMLGregorianCalendar localDateTimeToXmlGregorianCalendar( LocalDateTime localDateTime ) {
        if ( localDateTime == null ) {
            return null;
        }

        return datatypeFactory.newXMLGregorianCalendar(
            localDateTime.getYear(),
            localDateTime.getMonthValue(),
            localDateTime.getDayOfMonth(),
            localDateTime.getHour(),
            localDateTime.getMinute(),
            localDateTime.getSecond(),
            localDateTime.get( ChronoField.MILLI_OF_SECOND ),
            DatatypeConstants.FIELD_UNDEFINED );
    }

    private static LocalDate xmlGregorianCalendarToLocalDate( XMLGregorianCalendar xcal ) {
        if ( xcal == null ) {
            return null;
        }

        return LocalDate.of( xcal.getYear(), xcal.getMonth(), xcal.getDay() );
    }

    private UUID cardUserId(Card card) {
        if ( card == null ) {
            return null;
        }
        User user = card.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
