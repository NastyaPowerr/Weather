package org.roadmap.weather;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    private final AuthService authService;

    public AuthInterceptor(SessionService sessionService, AuthService authService) {
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Optional<String> sessionIdOpt = extractSessionId(request);
        if (sessionIdOpt.isPresent()) {
            UUID sessionId = UUID.fromString(sessionIdOpt.get());
            Optional<SessionDto> session = sessionService.getSession(sessionId);
            if (session.isPresent()) {
                SessionDto currentSession = session.get();
                UserDto user = authService.getById(currentSession.userId());
                request.setAttribute("user", user);
                request.setAttribute("sessionId", sessionId);
            }
        }
        return true;
    }

    private Optional<String> extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
