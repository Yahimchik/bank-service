package com.example.bankcards.dto.user;

import com.example.bankcards.validation.email.ValidEmail;
import com.example.bankcards.validation.password.ValidPassword;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDto {
    @ValidEmail
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @ValidPassword
    @Schema(description = "User password", example = "strongPassword123")
    private String password;

    @Schema(description = "Full name of the user", example = "John Smith")
    private String fullName;

    @Schema(description = "User roles", example = "[\"USER/ADMIN\"]")
    private Set<String> roles;
}
