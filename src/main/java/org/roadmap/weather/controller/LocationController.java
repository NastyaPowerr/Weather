package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.LocationService;
import org.roadmap.weather.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LocationController {
    private final LocationService locationService;
    private final SessionService sessionService;
    private final AuthService authService;

    public LocationController(
            LocationService locationService,
            SessionService sessionService,
            AuthService authService
    ) {
        this.locationService = locationService;
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @GetMapping("/search")
    public String findLocations(
            @RequestParam String name,
            HttpServletRequest request,
            Model model
    ) {
        Integer userId = extractUserId(request);
        if (userId == null) {
            model.addAttribute("isUserLoginned", false);
        } else {
            String login = authService.getLoginById(userId);
            model.addAttribute("userLogin", login);
            model.addAttribute("isUserLoginned", true);
        }
        List<LocationDto> locations = locationService.findByName(name);
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
        locationService.add(name, latitude, longitude, userId);
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
        locationService.delete(locationId, userId);
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
