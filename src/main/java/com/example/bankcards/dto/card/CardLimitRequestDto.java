package com.example.bankcards.dto.card;

import com.example.bankcards.entities.enums.TransactionType;
import com.example.bankcards.validation.amount.PositiveAmount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardLimitRequestDto {

    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @PositiveAmount(message = "Amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Invalid amount format (maximum 2 decimal places)")
    @Schema(description = "Daily transaction limit", example = "5000.00")
    private BigDecimal dailyLimit;

    @NotNull(message = "Monthly limit must not be null")
    @PositiveAmount(message = "Amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Invalid format for monthly limit (max 2 decimal places)")
    @Schema(description = "Monthly transaction limit", example = "20000.00")
    private BigDecimal monthlyLimit;
}
