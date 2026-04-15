package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final SessionService sessionService;
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository, SessionService sessionService) {
        this.sessionService = sessionService;
        this.authRepository = authRepository;
    }

    public void register(UserDto user) {
        String hashPassword = getEncryptedPassword(user.password());
        User userToSave = new User(user.login(), hashPassword);
        authRepository.save(userToSave);
    }

    // wrong OR non-existent login = "invalid login or password"
    // don't want to give info about whether user registered there or not
    public SessionDto authorize(UserDto userDto) {
        User user = authRepository.findByLogin(userDto.login())
                .orElseThrow(() -> new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS));

        BCrypt.Result result = BCrypt.verifyer().verify(userDto.password().toCharArray(), user.getPassword());
        if (!result.verified) {
            throw new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS);
        }
        return sessionService.create(user.getId());

    }

    private static String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
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
}
