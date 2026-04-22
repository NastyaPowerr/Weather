package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.repository.AuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class AuthService {
    private final static Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final SessionService sessionService;
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository, SessionService sessionService) {
        this.sessionService = sessionService;
        this.authRepository = authRepository;
    }

    public void register(UserDto user) {
        if (user.password() == null) {
            throw new ValidationException("Password cannot be null");
        }
        String hashPassword = getEncryptedPassword(user.password());
        User userToSave = new User(user.login(), hashPassword);
        try {
            authRepository.save(userToSave);
        } catch (InvalidUserParamsException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }

    // wrong OR non-existent login = "invalid login or password"
    // don't want to give info about whether user registered there or not
    public SessionDto authorize(UserDto userDto) {
        User user = authRepository.findByLogin(userDto.login())
                .orElseThrow(() -> new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS));
        if (isPasswordVerified(userDto.password().toCharArray(), user.getPassword())) {
            logger.info("User={} successfully authorized at {}", userDto.login(), new Timestamp(System.currentTimeMillis()));
            return sessionService.create(user.getId());
        }
        logger.debug("User={} failed to authorize at {}", userDto.login(), new Timestamp(System.currentTimeMillis()));
        throw new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS);
    }

    public Optional<String> getLoginById(Integer userId) {
        Optional<User> user = authRepository.findById(userId);
        if (user.isPresent()) {
            return Optional.of(user.get().getLogin());
        }
        return Optional.empty();
    }

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
