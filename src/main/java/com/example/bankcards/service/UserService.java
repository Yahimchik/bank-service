package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entities.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponseDto> viewAllUsers();

    User getUserByEmail(String email);

    UserResponseDto addUser(UserRequestDto user);

    UserResponseDto updateUser(UUID id, UserRequestDto user);

    void deleteUser(UUID userId);

    void restoreUser(UUID userId);
}
