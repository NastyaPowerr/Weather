package org.roadmap.weather;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.service.SessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
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
                Integer userId = sessionService.getUserIdFromSession(sessionId.get());
                request.setAttribute("userId", userId);
                request.setAttribute("sessionId", sessionId.get());
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
