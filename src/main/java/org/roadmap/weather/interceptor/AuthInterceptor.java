package org.roadmap.weather.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.internal.SessionDto;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.service.impl.AuthService;
import org.roadmap.weather.service.impl.CookieService;
import org.roadmap.weather.service.impl.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    private final AuthService authService;
    private final CookieService cookieService;

    @Value("${cookie.name}")
    private String cookieName;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        try {
            Optional<String> sessionIdOpt = extractSessionId(request);
            if (sessionIdOpt.isPresent()) {
                UUID sessionId = UUID.fromString(sessionIdOpt.get());
                Optional<SessionDto> session = sessionService.getSession(sessionId);
                if (session.isPresent()) {
                    SessionDto currentSession = session.get();
                    UserDto user = authService.getById(currentSession.userId());
                    request.setAttribute("user", user);
                    request.setAttribute("sessionId", sessionId);
                } else {
                    Cookie deletedCookie = cookieService.delete();
                    response.addCookie(deletedCookie);
                }
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid session id: {}", ex.getMessage());
            Cookie deletedCookie = cookieService.delete();
            response.addCookie(deletedCookie);
        }
        return true;
    }

    private Optional<String> extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
