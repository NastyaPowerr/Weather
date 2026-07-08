package org.roadmap.weather.service;

import jakarta.servlet.http.Cookie;

import java.util.UUID;

public interface CookieApi {
    Cookie create(UUID sessionId);

    Cookie delete();
}
