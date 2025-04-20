package com.example.bankcards.security.jwt.impl;

import com.example.bankcards.entities.User;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProviderService implements JwtTokenProvider {

    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final UserDetailsService userDetailsService;

    public JwtTokenProviderService(@Value("${spring.application.security.jwt.secret.access}") String jwtAccessSecret,
                                   @Value("${spring.application.security.jwt.secret.refresh}") String jwtRefreshSecret,
                                   @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.userDetailsService = userDetailsService;
    }

    @Override
    public String generateAccessToken(@NonNull User user) {
        Claims claims = Jwts
                .claims()
                .setSubject(user.getEmail());
        claims.put("role", user.getRoles());
        claims.put("email", user.getEmail());
        claims.put("id", user.getId());

        LocalDateTime now = LocalDateTime
                .now();
        Instant accessExpirationInstant = now
                .plusMinutes(15)
                .atZone(ZoneId
                        .systemDefault())
                .toInstant();
        Date accessExpiration = Date.from(accessExpirationInstant);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(accessExpiration)
                .signWith(jwtAccessSecret)
                .compact();
    }

    @Override
    public String generateRefreshToken(@NonNull User user) {
        LocalDateTime now = LocalDateTime.now();
        Instant refreshExpirationInstant = now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
        Date refreshExpiration = Date.from(refreshExpirationInstant);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(refreshExpiration)
                .signWith(jwtRefreshSecret)
                .compact();
    }

    @Override
    public boolean validateAccessToken(String token) {
        return validateToken(token, jwtAccessSecret);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return validateToken(token, jwtRefreshSecret);
    }

    @Override
    public Claims getAccessClaims(String token) {
        return getClaimsFromToken(token, jwtAccessSecret);
    }

    @Override
    public Claims getRefreshClaims(String token) {
        return getClaimsFromToken(token, jwtRefreshSecret);
    }

    @Override
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getLoginFromAccessToken(token));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public String getLoginFromAccessToken(String token) {
        return getAccessClaims(token).getSubject();
    }

    @Override
    public String getLoginFromRefreshToken(String token) {
        return getRefreshClaims(token).getSubject();
    }

    @Override
    public LocalDateTime getExpirationDate(String token) {
        return toLocalDateTime(getRefreshClaims(token).getExpiration());
    }

    private boolean validateToken(@NonNull String token, @NonNull Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Claims getClaimsFromToken(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
