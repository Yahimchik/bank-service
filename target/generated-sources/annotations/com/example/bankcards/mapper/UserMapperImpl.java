package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entities.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-20T17:08:15+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User convertToUser(UserRequestDto userRequestDto) {
        if ( userRequestDto == null ) {
            return null;
        }

        User user = new User();

        user.setRoles( map( userRequestDto.getRoles() ) );
        user.setEmail( userRequestDto.getEmail() );
        user.setPassword( userRequestDto.getPassword() );
        user.setFullName( userRequestDto.getFullName() );

        return user;
    }

    @Override
    public UserResponseDto convertToUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId( user.getId() );
        userResponseDto.setEmail( user.getEmail() );
        userResponseDto.setFullName( user.getFullName() );
        userResponseDto.setRoles( mapToRoleNames( user.getRoles() ) );
        userResponseDto.setDeleted( user.isDeleted() );

        return userResponseDto;
    }
}
