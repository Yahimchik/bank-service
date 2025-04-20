package com.example.bankcards.data;

import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.RefreshJwtRequestDto;
import com.example.bankcards.entities.RefreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

public class RefreshTokenTestData {
    private static final UUID ID = UUID.fromString("fd84e284-29aa-4481-9c39-f29f770d967a");
    private static final String REFRESH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQG1haWwucnUiLCJleHAiOjE2ODEyMzU1OTN9.OKbS1a" +
            "8fKv1wevLT2wRzXmljWvu6lIS8IXFGUuelCgyGgJX87Fi0PcZxKNVdvg8aSd_PGX1i4JYiZ38ejBXjgg";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQG1haWwucnUiLCJyb2xlIjoiVVNFUiIsInN0YXR1cyI6Ik" +
            "FDVElWRSIsImV4cCI6MTY3ODY0OTgyMH0.ErieLaCZ0obUScwljrPYWDpocnhJEs1bOOdo1mTyMNzJgwZNL-pJPzBmD6AhZtUdhdVEook9AGXdvH3ro9jwqQ";
    private static final LocalDateTime EXPIRATION = LocalDateTime.of(2025, 4, 17, 20, 53, 13);

    public static RefreshToken buildRefreshToken() {
        return RefreshToken.builder()
                .id(ID)
                .ipAddress("192.0.2.1")
                .token(REFRESH_TOKEN)
                .expiryDate(EXPIRATION)
                .user(UserTestData.buildUser())
                .build();
    }

    public static JwtResponseDto buildJwtResponseDto() {
        return JwtResponseDto.builder()
                .accessToken(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .build();
    }

    public static RefreshJwtRequestDto buildRefreshJwtRequestDto() {
        return RefreshJwtRequestDto.builder()
                .refreshToken(REFRESH_TOKEN)
                .build();
    }
}
