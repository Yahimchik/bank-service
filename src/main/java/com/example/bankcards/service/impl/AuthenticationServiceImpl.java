package com.example.bankcards.service.impl;

import com.example.bankcards.dto.auth.AuthenticationDto;
import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.RefreshJwtRequestDto;
import com.example.bankcards.entities.RefreshToken;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.service.AuthenticationService;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.exception.auth.InvalidJwtTokenException;
import com.example.bankcards.service.exception.user.UserAuthenticationProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public JwtResponseDto authenticate(AuthenticationDto authenticationDto) {
        try {
            log.info("Trying to authenticate user with email: {}", authenticationDto.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationDto.getEmail(),
                            authenticationDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.getUserByEmail(authenticationDto.getEmail());
            log.info("User authenticated successfully: {}", user.getEmail());

            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);
            String ip = getUserIp();

            RefreshToken newToken = buildRefreshToken(user, refreshToken);

            refreshTokenRepository.findByUserAndIp(user, ip).ifPresent(existingToken -> {
                log.info("Existing refresh token found for user {} and IP {}. Deleting it.", user.getEmail(), ip);
                refreshTokenService.deleteByUserAndIp(user, ip);
            });

            refreshTokenService.save(newToken);
            log.info("New refresh token saved for user {} from IP {}", user.getEmail(), ip);

            return new JwtResponseDto(accessToken, refreshToken);
        } catch (AuthenticationException exception) {
            log.error("Authentication failed for email {}: {}", authenticationDto.getEmail(), exception.getMessage());
            throw new UserAuthenticationProcessingException("Authentication error " + exception.getMessage());
        }
    }

    @Override
    @Transactional
    public JwtResponseDto recreateToken(RefreshJwtRequestDto refreshJwtRequestDto) {
        String requestToken = refreshJwtRequestDto.getRefreshToken();
        log.info("Attempting to recreate token using refresh token");

        if (jwtTokenProvider.validateRefreshToken(requestToken)) {
            String email = jwtTokenProvider.getLoginFromRefreshToken(requestToken);
            String ip = getUserIp();
            User user = userService.getUserByEmail(email);
            RefreshToken refreshToken = refreshTokenService.findByUserAndIp(user, ip);
            String tokenValue = refreshToken.getToken();

            if (Objects.nonNull(tokenValue) && tokenValue.equals(requestToken)) {
                String accessToken = jwtTokenProvider.generateAccessToken(user);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
                refreshTokenService.updateRefreshToken(refreshToken, newRefreshToken);
                log.info("Successfully recreated tokens for user {}", user.getEmail());
                return new JwtResponseDto(accessToken, newRefreshToken);
            }
        }

        log.warn("Invalid or expired refresh token used for recreation");
        throw new InvalidJwtTokenException("JWT token is expired or invalid");
    }

    @Override
    @Transactional
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            String ip = getUserIp();
            User user = userService.getUserByEmail(authentication.getName());
            refreshTokenService.deleteByUserAndIp(user, ip);
            log.info("User {} logged out and refresh token deleted for IP {}", user.getEmail(), ip);
        } else {
            log.warn("Logout attempt with no authenticated user in context");
        }
    }

    private RefreshToken buildRefreshToken(User user, String refreshToken) {
        return RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .ipAddress(getUserIp())
                .expiryDate(jwtTokenProvider.getExpirationDate(refreshToken))
                .build();
    }

    private String getUserIp() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes())
                        .getRequest();
        return request.getRemoteAddr();
    }
}
