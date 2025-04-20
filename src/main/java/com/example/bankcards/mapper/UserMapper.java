package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entities.Role;
import com.example.bankcards.entities.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true))
public interface UserMapper {
    @Mappings({
            @Mapping(target = "roles", source = "roles")
    })
    User convertToUser(UserRequestDto userRequestDto);

    UserResponseDto convertToUserResponse(User user);

    default Set<Role> map(Set<String> roleNames) {
        if (roleNames == null) {
            return null;
        }
        return roleNames.stream()
                .map(roleName -> new Role(null, roleName))
                .collect(Collectors.toSet());
    }

    default Set<String> mapToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
