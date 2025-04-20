package com.example.bankcards.service;

import com.example.bankcards.data.RefreshTokenTestData;
import com.example.bankcards.data.UserTestData;
import com.example.bankcards.entities.RefreshToken;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.service.exception.auth.TokenNotFoundException;
import com.example.bankcards.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    private static final LocalDateTime EXPIRATION = LocalDateTime.of(2025, 4, 17, 20, 53, 13);
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;
    private RefreshToken refreshToken;
    private User user;

    @BeforeEach
    void setup() {
        createTestData();
    }

    @Test
    @DisplayName("Should save new refresh token")
    void saveTest() {
        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

        assertThat(refreshToken).isEqualTo(refreshTokenService.save(refreshToken));

        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    @DisplayName("Should return refresh token by user and ip address")
    void findByUserAndIpTest() {
        String ipAddress = "192.0.2.1";
        when(refreshTokenRepository.findByUserAndIp(user, ipAddress)).thenReturn(Optional.of(refreshToken));

        assertThat(refreshToken).isEqualTo(refreshTokenService.findByUserAndIp(user, ipAddress));

        verify(refreshTokenRepository, times(1)).findByUserAndIp(user, ipAddress);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when refresh token is not by user and ip address")
    void findByUserAndIpWhenNotFoundTest() {
        String ipAddress = "192.0.2.1";
        when(refreshTokenRepository.findByUserAndIp(user, ipAddress)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findByUserAndIp(user, ipAddress))
                .isInstanceOf(TokenNotFoundException.class)
                .hasMessage("Refresh token for user " + user.getEmail() + " not found");

        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Should update refresh token")
    void updateTokenTest() {
        String newToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQG1haWwucnUiLCJleHAiOjE2ODEyMzU1OTN9.OKbS1a" +
                "8fKv1wevLT2wRzXmljWvu6lIS8IXFGUuelCgyGgJX87Fi0PcZxKNVdvg8aSd_PGX1i4JYiZ38ejBXjgg";

        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);
        when(jwtTokenProvider.getExpirationDate(refreshToken.getToken())).thenReturn(EXPIRATION);

        assertThat(refreshToken).isEqualTo(refreshTokenService.updateRefreshToken(refreshToken, newToken));

        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    @DisplayName("Should delete refresh token by user and ip address")
    void deleteByUserAndIpTest() {
        String ipAddress = "192.0.2.1";
        doNothing().when(refreshTokenRepository).deleteByUserAndIp(user, ipAddress);

        refreshTokenService.deleteByUserAndIp(user, ipAddress);

        verify(refreshTokenRepository, times(1)).deleteByUserAndIp(user, ipAddress);
    }

    private void createTestData() {
        refreshToken = RefreshTokenTestData.buildRefreshToken();
        user = UserTestData.buildUser();
    }
}
