package com.example.bankcards.security.jwt;

import com.example.bankcards.entities.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

public interface JwtTokenProvider {
    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    boolean validateAccessToken(String token);

    boolean validateRefreshToken(String token);

    Claims getAccessClaims(String token);

    Claims getRefreshClaims(String token);

    Authentication getAuthentication(String token);

    String getLoginFromAccessToken(String token);

    String getLoginFromRefreshToken(String token);

    LocalDateTime getExpirationDate(String token);
}
