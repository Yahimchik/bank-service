
package com.example.bankcards.dto.card;

import com.example.bankcards.entities.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardRequestDto {
    @NotBlank(message = "Card number must not be blank")
    @Pattern(regexp = "\\d{16}", message = "The card number must contain only numbers (16)")
    @Schema(description = "Card number", example = "1234567890123456")
    private String cardNumberEncrypted;

    @NotNull(message = "Expiration date must not be null")
    @Future(message = "Expiration date must be in the future")
    @Schema(description = "Card expiration date", example = "2030-12-31T23:59:59")
    private LocalDateTime expirationDate;

    @NotNull(message = "Card status must not be null")
    @Schema(description = "Status of the card", example = "ACTIVE")
    private CardStatus status;

    @NotNull(message = "Balance must not be null")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 12, fraction = 2, message = "Invalid balance format (max 2 decimal places)")
    @Schema(description = "Card balance", example = "1000.00")
    private BigDecimal balance;
}
