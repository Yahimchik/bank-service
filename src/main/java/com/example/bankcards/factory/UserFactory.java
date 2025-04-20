package com.example.bankcards.factory;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.entities.Role;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRequestDto request, Set<Role> roles) {
        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(roles)
                .build();
    }
}
