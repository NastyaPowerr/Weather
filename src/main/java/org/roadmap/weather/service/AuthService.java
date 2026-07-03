package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.internal.SessionDto;
import org.roadmap.weather.dto.request.UserLoginDto;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.mapper.UserMapper;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {
    private final SessionService sessionService;
    private final AuthRepository authRepository;
    private final UserMapper userMapper;

    @Loggable
    @Transactional
    public void register(@Valid UserRegisterDto userDto) {
        if (!userDto.password().equals(userDto.repeatedPassword())) {
            throw new PasswordsDoNotMatchException(ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
        }
        String hashPassword = getEncryptedPassword(userDto.password());
        String username = extractUsername(userDto.username());
        User userToSave = userMapper.toEntity(username, hashPassword);
        try {
            authRepository.save(userToSave);
        } catch (InvalidUserParamsException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }

    @Loggable
    @Transactional
    public SessionDto authorize(UserLoginDto userDto) {
        String username = extractUsername(userDto.username());
        User user = authRepository.findByLogin(username)
                .orElseThrow(() -> new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS));
        if (isPasswordVerified(userDto.password().toCharArray(), user.getPassword())) {
            return sessionService.create(user.getId());
        }
        throw new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    public UserDto getById(Integer id) {
        return authRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
    }

    @Loggable
    public void logout(UUID sessionId) {
        sessionService.deleteSession(sessionId);
    }

    private String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    private boolean isPasswordVerified(char[] password, String hashedPassword) {
        return BCrypt.verifyer().verify(password, hashedPassword).verified;
    }

    private static String extractUsername(String userDto) {
        return userDto.strip();
    }
}
