package org.roadmap.weather.service;


import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CookieService {
    @Value("${cookie.duration}")
    private int cookieAge;

    @Value("${cookie.name}")
    private String cookieName;

    public Cookie create(UUID sessionId) {
        Cookie cookie = new Cookie(cookieName, String.valueOf(sessionId));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieAge);
        return cookie;
    }

    public Cookie delete() {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
