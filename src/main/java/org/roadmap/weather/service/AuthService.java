package org.roadmap.weather.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void register(UserDto user) {
        String hashPassword = getEncryptedPassword(user.password());
        User userToSave = new User(user.login(), hashPassword);
        authRepository.save(userToSave);
    }

    public String authorize(UserDto user) {
        User foundUser = authRepository.getUser(user.login());
        BCrypt.Result result = BCrypt.verifyer().verify(user.password().toCharArray(), foundUser.getPassword());
        if (result.verified) {
            return "session";
        }
        return "wrong login or password";
    }

    private static String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
}
