package com.example.bankcards.service.impl;

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
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.exception.RoleNotFoundException;
import com.example.bankcards.service.exception.user.UserAlreadyActiveException;
import com.example.bankcards.service.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserFactory userFactory;
    private final UserUpdater userUpdater;
    private final CardRepository cardRepository;

    @Override
    public List<UserResponseDto> viewAllUsers() {
        log.info("Fetching all users from the database");
        List<UserResponseDto> users = userRepository.findAll()
                .stream()
                .map(userMapper::convertToUserResponse)
                .collect(Collectors.toList());
        log.info("Found {} users", users.size());
        return users;
    }

    @Override
    public User getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email '{}' not found", email);
                    return new UserNotFoundException("User not found: " + email);
                });
    }

    @Override
    @Transactional
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        log.info("Adding new user with email: {}", userRequestDto.getEmail());

        Set<Role> roles = userRequestDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> {
                            log.warn("Role '{}' not found when creating user", roleName);
                            return new RoleNotFoundException("Role not found: " + roleName);
                        }))
                .collect(Collectors.toSet());

        log.info(String.valueOf(roles));

        User user = userFactory.createUser(userRequestDto, roles);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.convertToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UUID id, UserRequestDto userDto) {
        log.info("Updating user with id: {}", id);

        User existing = findUserById(id);

        userUpdater.updateUserFromDto(existing, userDto);
        User updated = userRepository.save(existing);

        log.info("User with id {} updated successfully", id);
        return userMapper.convertToUserResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);

        User user = findUserById(userId);

        user.setDeleted(true);

        List<Card> userCards = cardRepository.findAllByUserId(userId);
        setStatusAndDeletedForUserCards(userCards, CardStatus.BLOCKED, true);
        userRepository.save(user);

        log.info("User with id {} and their {} cards marked as deleted", userId, userCards.size());
    }

    @Override
    @Transactional
    public void restoreUser(UUID userId) {
        log.info("Restore user with id: {}", userId);

        User user = findUserById(userId);

        if (!user.isDeleted()) {
            log.warn("User with id {} is already active", userId);
            throw new UserAlreadyActiveException("User is already active " + userId);
        }

        user.setDeleted(false);

        List<Card> userCards = cardRepository.findAllByUserId(userId);
        setStatusAndDeletedForUserCards(userCards, CardStatus.ACTIVE, false);
        userRepository.save(user);

        log.info("User with id {} and their {} cards marked as active", userId, userCards.size());
    }

    private void setStatusAndDeletedForUserCards(List<Card> userCards, CardStatus blocked, boolean isDeleted) {
        for (Card card : userCards) {
            card.setStatus(blocked);
            card.setDeleted(isDeleted);
            cardRepository.save(card);
        }
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id '{}' not found", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });
    }
}
