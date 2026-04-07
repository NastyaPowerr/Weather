package org.roadmap.weather.controller;

import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public void register(@RequestBody UserDto user) {
        authService.register(user);
    }

    @PostMapping("/auth/login")
    public String authorize(@RequestBody UserDto user) {
        return authService.authorize(user);
    }
}
