package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.service.SessionService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WeatherController {
    private final WeatherService weatherService;
    private final SessionService sessionService;

    public WeatherController(WeatherService weatherService, SessionService sessionService) {
        this.weatherService = weatherService;
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String getWeathers(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            model.addAttribute("weathers", List.of());
            return "index";
        }
        List<Weather> weathers = weatherService.getWeathersForUser(userId);
        model.addAttribute("weathers", weathers);
        return "index";
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
