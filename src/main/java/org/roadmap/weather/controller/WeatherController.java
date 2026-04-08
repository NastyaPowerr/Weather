package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.service.SessionService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WeatherController {
    private final WeatherService weatherService;
    private final SessionService sessionService;

    public WeatherController(WeatherService weatherService, SessionService sessionService) {
        this.weatherService = weatherService;
        this.sessionService = sessionService;
    }

    @GetMapping("/location/test")
    public List<LocationDto> testSeeCityInfo(
            @RequestParam String name,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return weatherService.findByName(name, userId);
    }

    @GetMapping("/weather/test")
    public List<Weather> testSeeWeatherInfo(HttpServletRequest request, HttpServletResponse response) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        return weatherService.getWeathersForUser(userId);
    }

    private Integer extractUserId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                if (sessionService.isSessionValid(cookie.getValue())) {
                    return sessionService.getUserIdFromSession(cookie.getValue());
                }
            }
        }

        return null;
    }
}
