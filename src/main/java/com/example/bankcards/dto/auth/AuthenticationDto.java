package com.example.bankcards.dto.auth;

import com.example.bankcards.validation.email.ValidEmail;
import com.example.bankcards.validation.password.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationDto {
    @NotBlank(message = "Email must not be blank")
    @ValidEmail(message = "Email must be valid")
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @ValidPassword
    @Schema(description = "User password", example = "strongPassword123")
    private String password;
}
