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

    // delete = give cookie with age = 0
    public static Cookie deleteCookie() {
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
