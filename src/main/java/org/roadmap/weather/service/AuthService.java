package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
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

    public Optional<SessionDto> authorize(UserDto user) {
        User foundUser = authRepository.getUser(user.login());
        BCrypt.Result result = BCrypt.verifyer().verify(user.password().toCharArray(), foundUser.getPassword());
        if (result.verified) {
            return Optional.of(sessionService.create(foundUser.getId()));
        }
        return Optional.empty();
    }

    private static String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
}
