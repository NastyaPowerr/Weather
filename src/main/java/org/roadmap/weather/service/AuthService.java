package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.request.UserLoginDto;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.mapper.UserMapper;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final SessionService sessionService;
    private final AuthRepository authRepository;
    private final UserMapper userMapper;

    public AuthService(
            AuthRepository authRepository,
            SessionService sessionService,
            UserMapper userMapper
    ) {
        this.sessionService = sessionService;
        this.authRepository = authRepository;
        this.userMapper = userMapper;
    }

    @Loggable
    public void register(UserRegisterDto userDto) {
        if (!userDto.password().equals(userDto.repeatedPassword())) {
            throw new PasswordsDoNotMatchException(ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
        }
        String hashPassword = getEncryptedPassword(userDto.password());
        User userToSave = new User(userDto.username(), hashPassword);
        try {
            authRepository.save(userToSave);
        } catch (InvalidUserParamsException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }

    @Loggable
    public SessionDto authorize(UserLoginDto userDto) {
        User user = authRepository.findByLogin(userDto.login())
                .orElseThrow(() -> new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS));
        if (isPasswordVerified(userDto.password().toCharArray(), user.getPassword())) {
            return sessionService.create(user.getId());
        }
        throw new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS);
    }

    @Cacheable(cacheNames = "users", key = "#id")
    public UserDto getById(Integer id) {
        return authRepository.findById(id)
                .map(userMapper::toDto)
                .orElse(null);
    }

    @Loggable
    public void logout(String sessionId) {
        sessionService.deleteSession(sessionId);
    }

    private String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    private boolean isPasswordVerified(char[] password, String hashedPassword) {
        return BCrypt.verifyer().verify(password, hashedPassword).verified;
    }
}
