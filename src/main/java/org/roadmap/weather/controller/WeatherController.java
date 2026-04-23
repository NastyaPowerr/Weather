package org.roadmap.weather.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

@Controller
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String getWeathers(
            @RequestAttribute(name = "userId", required = false) Integer userId,
            @RequestAttribute(name = "userLogin", required = false) String login,
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
            model.addAttribute("userLogin", login);
            model.addAttribute("weathers", weathers);
            model.addAttribute("isUserAuthorized", true);
            return "index";
        } catch (GeocodingApiCallException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "redirect:/error";
        }
    }
}
