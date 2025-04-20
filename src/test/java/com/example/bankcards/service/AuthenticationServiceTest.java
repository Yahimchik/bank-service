package com.example.bankcards.service;

import com.example.bankcards.data.RefreshTokenTestData;
import com.example.bankcards.data.UserTestData;
import com.example.bankcards.dto.auth.AuthenticationDto;
import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.RefreshJwtRequestDto;
import com.example.bankcards.entities.RefreshToken;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final UUID id = UUID.fromString("fd84e284-29aa-4481-9c39-f29f770d967a");
    private static final String IP = "192.0.2.1";
    private static final LocalDateTime EXPIRATION = LocalDateTime.of(2025, 4, 17, 20, 53, 13);
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserService userService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;
    private User user;
    private AuthenticationDto authenticationDto;
    private JwtResponseDto jwtResponseDto;
    private RefreshToken refreshToken;
    private RefreshJwtRequestDto refreshJwtRequestDto;

    @BeforeEach
    void setup() {
        createTestData();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(IP);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Should authenticate user when get correct email and password")
    void authenticateTest() {
        when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn(jwtResponseDto.getAccessToken());
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn(jwtResponseDto.getRefreshToken());
        when(jwtTokenProvider.getExpirationDate(jwtResponseDto.getRefreshToken())).thenReturn(EXPIRATION);
        when(refreshTokenRepository.findByUserAndIp(user, IP)).thenReturn(Optional.empty());
        when(refreshTokenService.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(id);
            return token;
        });

        assertThat(jwtResponseDto).isEqualTo(authenticationService.authenticate(authenticationDto));

        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(authenticationDto.getEmail(), authenticationDto.getPassword()));
        verify(userService, times(1)).getUserByEmail(user.getEmail());
        verify(jwtTokenProvider, times(1)).generateAccessToken(user);
        verify(jwtTokenProvider, times(1)).generateRefreshToken(user);
        verify(jwtTokenProvider, times(1)).getExpirationDate(jwtResponseDto.getRefreshToken());
        verify(refreshTokenRepository, times(1)).findByUserAndIp(user, IP);
        verify(refreshTokenService, times(1)).save(refreshToken);
    }

    @Test
    @DisplayName("Should generate a new access and a new refresh tokens when refresh token is valid")
    void recreateToken() {
        when(jwtTokenProvider.validateRefreshToken(refreshJwtRequestDto.getRefreshToken())).thenReturn(true);
        when(jwtTokenProvider.getLoginFromRefreshToken(refreshJwtRequestDto.getRefreshToken())).thenReturn(user.getEmail());
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(refreshTokenService.findByUserAndIp(user, IP)).thenReturn(refreshToken);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn(jwtResponseDto.getAccessToken());
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn(jwtResponseDto.getRefreshToken());
        when(refreshTokenService.updateRefreshToken(refreshToken, jwtResponseDto.getRefreshToken())).thenReturn(refreshToken);

        assertThat(jwtResponseDto).isEqualTo(authenticationService.recreateToken(refreshJwtRequestDto));

        verify(jwtTokenProvider, times(1)).validateRefreshToken(refreshJwtRequestDto.getRefreshToken());
        verify(jwtTokenProvider, times(1)).getLoginFromRefreshToken(refreshJwtRequestDto.getRefreshToken());
        verify(userService, times(1)).getUserByEmail(user.getEmail());
        verify(refreshTokenService, times(1)).findByUserAndIp(user, IP);
        verify(jwtTokenProvider, times(1)).generateAccessToken(user);
        verify(jwtTokenProvider, times(1)).generateRefreshToken(user);
        verify(refreshTokenService, times(1)).updateRefreshToken(refreshToken, jwtResponseDto.getRefreshToken());
    }

    @Test
    @DisplayName("Should logout user and delete refresh token")
    void logout() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        doNothing().when(refreshTokenService).deleteByUserAndIp(user, IP);

        authenticationService.logout();

        verify(userService, times(1)).getUserByEmail(user.getEmail());
        verify(refreshTokenService, times(1)).deleteByUserAndIp(user, IP);
    }

    private void createTestData() {
        user = UserTestData.buildUser();
        authenticationDto = UserTestData.buildAuthDto();
        jwtResponseDto = RefreshTokenTestData.buildJwtResponseDto();
        refreshToken = RefreshTokenTestData.buildRefreshToken();
        refreshJwtRequestDto = RefreshTokenTestData.buildRefreshJwtRequestDto();
        authentication = new UsernamePasswordAuthenticationToken(authenticationDto.getEmail(),
                authenticationDto.getPassword());
    }
}