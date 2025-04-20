package com.example.bankcards.service;

import com.example.bankcards.dto.auth.AuthenticationDto;
import com.example.bankcards.dto.auth.JwtResponseDto;
import com.example.bankcards.dto.auth.RefreshJwtRequestDto;

public interface AuthenticationService {
    JwtResponseDto authenticate(AuthenticationDto authenticationDto);

    JwtResponseDto recreateToken(RefreshJwtRequestDto refreshJwtRequestDto);

    void logout();
}
