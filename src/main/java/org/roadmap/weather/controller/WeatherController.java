package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.exception.user.UserNotFoundException;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.SessionService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class WeatherController {
    private final WeatherService weatherService;
    private final SessionService sessionService;
    private final AuthService authService;

    public WeatherController(
            WeatherService weatherService,
            SessionService sessionService,
            AuthService authService
    ) {
        this.weatherService = weatherService;
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String getWeathers(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        Optional<Integer> userId = extractUserId(request);
        if (userId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            model.addAttribute("weathers", List.of());
            model.addAttribute("isUserAuthorized", false);
            return "index";
        }
        try {
            List<Weather> weathers = weatherService.getWeathersForUser(userId.get());
            Optional<String> login = authService.getLoginById(userId.get());
            if (login.isEmpty()) {
                throw new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND);
            }
            model.addAttribute("userLogin", login.get());
            model.addAttribute("weathers", weathers);
            model.addAttribute("isUserAuthorized", true);
            return "index";
        } catch (GeocodingApiCallException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "redirect:/error";
        }
    }

    private Optional<Integer> extractUserId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if ("sessionId".equals(cookie.getName())) {
                if (sessionService.isSessionValid(cookie.getValue())) {
                    return Optional.ofNullable(sessionService.getUserIdFromSession(cookie.getValue()));
                }
            }
        }
        return Optional.empty();
    }
}
