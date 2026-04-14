package org.roadmap.weather.controller;

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
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;
import java.util.Optional;

@Controller
public class WeatherController {
    private final WeatherService weatherService;
    private final AuthService authService;

    public WeatherController(
            WeatherService weatherService,
            AuthService authService
    ) {
        this.weatherService = weatherService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String getWeathers(
            @RequestAttribute(name = "userId", required = false) Integer userId,
            HttpServletResponse response,
            Model model
    ) {
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            model.addAttribute("weathers", List.of());
            model.addAttribute("isUserAuthorized", false);
            return "index";
        }
        try {
            List<Weather> weathers = weatherService.getWeathersForUser(userId);
            Optional<String> login = authService.getLoginById(userId);
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
}
