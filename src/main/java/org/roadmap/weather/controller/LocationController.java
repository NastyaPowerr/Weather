package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.service.SessionService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LocationController {
    private final WeatherService weatherService;
    private final SessionService sessionService;

    public LocationController(WeatherService weatherService, SessionService sessionService) {
        this.weatherService = weatherService;
        this.sessionService = sessionService;
    }

    @GetMapping("/search")
    public String findLocations(
            @RequestParam String name,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/auth/sign-in";
        }
        List<LocationDto> locations = weatherService.findByName(name, userId);
        model.addAttribute("locations", locations);
        model.addAttribute("locationName", name);
        return "search-results";
    }

    @PostMapping("/locations")
    public String addLocation(
            @RequestParam String name,
            @RequestParam String latitude,
            @RequestParam String longitude,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/auth/sign-in";
        }
        weatherService.addLocation(name, latitude, longitude, userId);
        return "redirect:/";
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(
            @RequestParam String locationId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Integer userId = extractUserId(request);

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/auth/sign-in";
        }
        weatherService.deleteLocation(locationId, userId);
        return "redirect:/";
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
