package com.example.bankcards.mapper;

import com.example.bankcards.dto.transaction.TransactionRequestDto;
import com.example.bankcards.dto.transaction.TransactionResponseDto;
import com.example.bankcards.entities.Transaction;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface TransactionMapper {
    Transaction convertToTransaction(TransactionRequestDto transactionRequestDto);

    TransactionResponseDto convertToTransactionResponseDto(Transaction transaction);
}
