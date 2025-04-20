package com.example.bankcards.service;

import com.example.bankcards.data.CardTestData;
import com.example.bankcards.data.RoleTestData;
import com.example.bankcards.data.UserTestData;
import com.example.bankcards.dto.user.UserRequestDto;
import com.example.bankcards.dto.user.UserResponseDto;
import com.example.bankcards.entities.Card;
import com.example.bankcards.entities.Role;
import com.example.bankcards.entities.User;
import com.example.bankcards.entities.enums.CardStatus;
import com.example.bankcards.factory.UserFactory;
import com.example.bankcards.factory.UserUpdater;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.exception.RoleNotFoundException;
import com.example.bankcards.service.exception.user.UserNotFoundException;
import com.example.bankcards.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final UUID ID = UUID.fromString("fd84e264-29aa-4481-9d39-f29f660d827a");
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserFactory userFactory;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User updatedUser;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private UserResponseDto updatedUserResponseDto;
    private Role roleUser;
    private Role roleAdmin;
    private Card card;

    @BeforeEach
    void setup() {
        UserUpdater userUpdater = new UserUpdater(roleRepository, passwordEncoder);
        userService = new UserServiceImpl(userRepository, roleRepository, userMapper, userFactory, userUpdater, cardRepository);
        createTestData();
    }

    @Test
    void viewAllUsers_shouldReturnMappedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.convertToUserResponse(user)).thenReturn(userResponseDto);

        List<UserResponseDto> result = userService.viewAllUsers();
        assertThat(result).hasSize(1);

        verify(userRepository).findAll();
        verify(userMapper).convertToUserResponse(user);
    }

    @Test
    void getUserByEmail_whenFound_shouldReturnUser() {
        String email = userRequestDto.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getUserByEmail_whenNotFound_shouldThrowException() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    void addUser_shouldCreateAndReturnUser() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(roleUser));
        when(userFactory.createUser(userRequestDto, Set.of(roleUser))).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.convertToUserResponse(user)).thenReturn(userResponseDto);

        assertThat(userResponseDto).isEqualTo(userService.addUser(userRequestDto));

        verify(roleRepository).findByName("USER");
        verify(userFactory).createUser(userRequestDto, Set.of(roleUser));
        verify(userRepository).save(user);
        verify(userMapper).convertToUserResponse(user);
    }

    @Test
    void addUser_whenRoleNotFound_shouldThrowException() {
        userRequestDto.setRoles(Set.of("UNKNOWN"));
        when(roleRepository.findByName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> userService.addUser(userRequestDto));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() {
        userRequestDto.setEmail("new@example.com");
        userRequestDto.setRoles(Set.of(roleAdmin.getName()));

        when(passwordEncoder.encode(userRequestDto.getPassword())).thenReturn(userRequestDto.getPassword());
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.convertToUserResponse(updatedUser)).thenReturn(updatedUserResponseDto);
        when(roleRepository.findByName(roleAdmin.getName())).thenReturn(Optional.of(roleAdmin));

        UserResponseDto result = userService.updateUser(ID, userRequestDto);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals(Set.of(roleAdmin.getName()), result.getRoles());

        verify(userRepository).findById(ID);
        verify(userRepository).save(user);
        verify(userMapper).convertToUserResponse(updatedUser);
        verify(roleRepository).findByName(roleAdmin.getName());
        verify(passwordEncoder).encode(userRequestDto.getPassword());
    }

    @Test
    void updateUser_whenNotFound_shouldThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(ID, userRequestDto));
    }

    @Test
    void deleteUser_shouldMarkUserAndCardsAsDeletedAndBlocked() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(cardRepository.findAllByUserId(ID)).thenReturn(List.of(card));

        userService.deleteUser(ID);

        assertTrue(user.isDeleted());
        assertTrue(card.isDeleted());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(userRepository).save(user);
        verify(cardRepository).save(card);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(ID));
    }

    private void createTestData() {
        user = UserTestData.buildUser();
        updatedUser = UserTestData.buildUserForUpdate();
        userRequestDto = UserTestData.buildUserRequestForRegistration();
        userResponseDto = UserTestData.buildUserResponseForRegistration();
        updatedUserResponseDto = UserTestData.buildUserResponseForUpdate();
        roleUser = RoleTestData.ROLE_USER;
        roleAdmin = RoleTestData.ROLE_ADMIN;
        card = CardTestData.buildCardForDeleting();
    }
}
