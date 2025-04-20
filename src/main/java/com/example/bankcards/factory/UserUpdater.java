package com.example.bankcards.factory;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.entities.Role;
import com.example.bankcards.entities.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.service.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserUpdater {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateUserFromDto(User user, UserRequestDto dto) {
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getRoles() != null) {
            Set<Role> roles = dto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
    }
}
