package com.example.bankcards.data;

import com.example.bankcards.dto.auth.AuthenticationDto;
import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entities.User;
import com.example.bankcards.security.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserTestData {
    private static final UUID ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");

    public static User buildUser() {
        return User.builder()
                .id(ID)
                .email("test@example.com")
                .password("test@example.com")
                .fullName("Test User")
                .deleted(false)
                .roles(Set.of(RoleTestData.ROLE_USER))
                .build();
    }

    public static User buildUserForUpdate() {
        return User.builder()
                .id(ID)
                .email("new@example.com")
                .password("test@example.com")
                .fullName("Test User")
                .roles(Set.of(RoleTestData.ROLE_ADMIN))
                .deleted(false)
                .build();
    }

    public static UserRequestDto buildUserRequestForRegistration() {
        return UserRequestDto.builder()
                .email("test@example.com")
                .password("test@example.com")
                .fullName("Test User")
                .roles(Set.of("USER"))
                .build();
    }

    public static UserResponseDto buildUserResponseForRegistration() {
        return UserResponseDto.builder()
                .id(ID)
                .email("test@example.com")
                .fullName("Test User")
                .roles(Set.of("USER"))
                .deleted(false)
                .build();
    }

    public static UserResponseDto buildUserResponseForUpdate() {
        return UserResponseDto.builder()
                .id(ID)
                .email("new@example.com")
                .fullName("Test User")
                .roles(Set.of("ADMIN"))
                .deleted(false)
                .build();
    }

    public static AuthenticationDto buildAuthDto() {
        return AuthenticationDto.builder()
                .email("test@example.com")
                .password("test@example.com")
                .build();
    }

    public static UserPrincipal buildUserPrincipal() {
        return UserPrincipal.builder()
                .id(ID)
                .email("admin@test.com")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
