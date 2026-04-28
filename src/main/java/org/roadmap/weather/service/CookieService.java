package org.roadmap.weather.service;


import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieService {
    @Value("${cookie.duration}")
    private int cookieAge;

    private CookieService() {
    }

    public Cookie create(String sessionId) {
        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieAge);
        return cookie;
    }

    // delete = give cookie with age = 0
    public Cookie delete() {
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
