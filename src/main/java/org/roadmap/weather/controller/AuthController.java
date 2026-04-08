package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping("/auth/register")
    public void register(@RequestBody UserDto user) {
        authService.register(user);
    }

    @PostMapping("/auth/login")
    public void authorize(@RequestBody UserDto user, HttpServletResponse response) {
        Optional<SessionDto> session = authService.authorize(user);
        if (session.isPresent()) {
            Cookie cookie = createCookie(String.valueOf(session.get().id()));
            response.addCookie(cookie);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @GetMapping("/auth/test/cookie")
    public String checkCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "no cookie :(";
        }

        for (Cookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                if (sessionService.isSessionValid(cookie.getValue())) {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                    return "good cookie!: " + cookie.getValue();
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return "wrong cookie";
    }

    private static Cookie createCookie(String sessionId) {
        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        return cookie;
    }
}
