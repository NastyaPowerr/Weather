package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.request.UserLoginDto;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.util.CookieManagerUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/sign-up")
    public String register() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String register(@Valid UserRegisterDto user) {
        authService.register(user);
        return "redirect:/";
    }

    @GetMapping("/sign-in")
    public String login() {
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public String login(
            @Valid UserLoginDto user,
            HttpServletResponse response
    ) {
        SessionDto session = authService.authorize(user);
        String sessionId = String.valueOf(session.id());
        Cookie cookie = CookieManagerUtil.createCookie(sessionId);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
        return "redirect:/";
    }

    @PostMapping("/sign-out")
    public String logout(
            @RequestAttribute(name = "sessionId", required = false) String sessionId,
            HttpServletResponse response
    ) {
        // проверка, чтобы уменьшить кол-во вызовов к БД
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        response.addCookie(CookieManagerUtil.deleteCookie());
        return "redirect:/";
    }
}
