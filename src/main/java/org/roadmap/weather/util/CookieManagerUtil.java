package org.roadmap.weather.util;


import jakarta.servlet.http.Cookie;

public final class CookieManagerUtil {
    private CookieManagerUtil() {
    }

    public static Cookie createCookie(String sessionId) {
        Cookie cookie = new Cookie("sessionId", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        return cookie;
    }
}
