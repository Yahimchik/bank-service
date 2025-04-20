package com.example.bankcards.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequestDto {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Invalid amount format (maximum 2 decimal places)")
    @Schema(description = "Transaction amount", example = "1000.00")
    private BigDecimal amount;

    @NotBlank(message = "Transaction description must not be blank")
    @Schema(description = "Transaction description", example = "transaction")
    private String description;
}
