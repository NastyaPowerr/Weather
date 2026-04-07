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
        // check if user exists in DB
        // шифруем пароль
        // маппим из ДТО в энтити
        // сохраняем
        String hashPassword = getEncryptedPassword(user.password());
        User userToSave = new User(user.login(), hashPassword);
        authRepository.save(userToSave);
    }

    private static String getEncryptedPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
}
