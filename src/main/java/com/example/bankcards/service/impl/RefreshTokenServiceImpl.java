package com.example.bankcards.service.impl;

import com.example.bankcards.entities.RefreshToken;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.exception.auth.TokenNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        log.info("Saving refresh token for user: {} with IP: {}", token.getUser().getEmail(), token.getIpAddress());
        return refreshTokenRepository.save(token);
    }

    @Override
    public RefreshToken findByUserAndIp(User user, String ip) {
        log.info("Searching for refresh token for user: {} with IP: {}", user.getEmail(), ip);
        return refreshTokenRepository.findByUserAndIp(user, ip)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found for user: {} with IP: {}", user.getEmail(), ip);
                    return new TokenNotFoundException("Refresh token for user " + user.getEmail() + " not found");
                });
    }

    @Override
    @Transactional
    public RefreshToken updateRefreshToken(RefreshToken token, String newToken) {
        log.info("Updating refresh token for user: {} with IP: {}", token.getUser().getEmail(), token.getIpAddress());
        token.setToken(newToken);
        token.setExpiryDate(jwtTokenProvider.getExpirationDate(newToken));
        return refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void deleteByUserAndIp(User user, String ip) {
        log.info("Deleting refresh token for user: {} with IP: {}", user.getEmail(), ip);
        refreshTokenRepository.deleteByUserAndIp(user, ip);
    }
}
