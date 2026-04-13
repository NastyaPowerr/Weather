package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.exception.location.LocationAlreadyExistsForUserException;
import org.roadmap.weather.exception.user.UserNotFoundException;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.LocationService;
import org.roadmap.weather.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

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
            HttpServletResponse response,
            Model model
    ) {
        Optional<Integer> userId = extractUserId(request);
        if (userId.isEmpty()) {
            model.addAttribute("isUserAuthorized", false);
        } else {
            Optional<String> login = authService.getLoginById(userId.get());
            if (login.isEmpty()) {
                throw new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND);
            }
            model.addAttribute("userLogin", login.get());
            model.addAttribute("isUserAuthorized", true);
        }
        try {
            List<LocationDto> locations = locationService.findByName(name);
            model.addAttribute("locations", locations);
            model.addAttribute("locationName", name);
            return "search-results";
        } catch (GeocodingApiCallException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "redirect:/error";
        }
    }

    @PostMapping("/locations")
    public String addLocation(
            @RequestParam String name,
            @RequestParam String latitude,
            @RequestParam String longitude,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<Integer> userId = extractUserId(request);
            if (userId.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                model.addAttribute("error", ExceptionMessages.REQUIRE_AUTHORIZATION);
                return "redirect:/auth/sign-in";
            }
            locationService.add(name, latitude, longitude, userId.get());
            return "redirect:/";
        } catch (LocationAlreadyExistsForUserException ex) {
            redirectAttributes.addFlashAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
            return "redirect:/search?name=" + name;
        }
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(
            @RequestParam String locationId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Optional<Integer> userId = extractUserId(request);

            if (userId.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "redirect:/auth/sign-in";
            }
            locationService.delete(locationId, userId.get());
            return "redirect:/";
        } catch (ValidationException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "redirect:/";
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
