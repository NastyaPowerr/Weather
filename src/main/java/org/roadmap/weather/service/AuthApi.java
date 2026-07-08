package org.roadmap.weather.service;

import org.roadmap.weather.dto.internal.SessionDto;
import org.roadmap.weather.dto.request.UserLoginDto;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.dto.view.UserDto;

import java.util.UUID;

public interface AuthApi {
    void register(UserRegisterDto userDto);

    SessionDto authorize(UserLoginDto userDto);

    UserDto getById(Integer id);

    void logout(UUID sessionId);
}
