package org.roadmap.weather.service;


import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CookieService {
    @Value("${cookie.duration}")
    private int cookieAge;

    @Value("${cookie.name}")
    private String cookieName;

    private CookieService() {
    }

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
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
