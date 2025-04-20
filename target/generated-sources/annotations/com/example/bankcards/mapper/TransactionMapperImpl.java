package com.example.bankcards.mapper;

import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.entities.Transaction;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-20T17:08:15+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public Transaction convertToTransaction(TransactionRequestDto transactionRequestDto) {
        if ( transactionRequestDto == null ) {
            return null;
        }

        Transaction transaction = new Transaction();

        transaction.setAmount( transactionRequestDto.getAmount() );
        transaction.setDescription( transactionRequestDto.getDescription() );

        return transaction;
    }

    @Override
    public TransactionResponseDto convertToTransactionResponseDto(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponseDto transactionResponseDto = new TransactionResponseDto();

        transactionResponseDto.setType( transaction.getType() );
        transactionResponseDto.setAmount( transaction.getAmount() );
        transactionResponseDto.setDescription( transaction.getDescription() );
        transactionResponseDto.setTimestamp( transaction.getTimestamp() );

        return transactionResponseDto;
    }
}
