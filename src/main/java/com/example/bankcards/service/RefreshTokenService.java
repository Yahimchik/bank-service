package com.example.bankcards.service;

import com.example.bankcards.entities.RefreshToken;
import com.example.bankcards.entities.User;

public interface RefreshTokenService {
    RefreshToken save(RefreshToken token);

    RefreshToken findByUserAndIp(User user, String ip);

    RefreshToken updateRefreshToken(RefreshToken token, String newToken);

    void deleteByUserAndIp(User user, String ip);
}
