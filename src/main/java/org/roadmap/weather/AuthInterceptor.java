package org.roadmap.weather;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

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
        Optional<String> sessionId = extractSessionId(request);
        if (sessionId.isPresent()) {
            if (sessionService.isSessionValid(sessionId.get())) {
                Optional<SessionDto> session = sessionService.getSession(sessionId.get());
                if (session.isPresent()) {
                    SessionDto currentSession = session.get();
                    Optional<String> username = authService.getLoginById(currentSession.userId());
                    if (username.isPresent()) {
                        request.setAttribute("userLogin", username.get());
                    }
                    request.setAttribute("userId", currentSession.userId());
                    request.setAttribute("sessionId", currentSession.id());
                } else {
                    return false;
                }
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
