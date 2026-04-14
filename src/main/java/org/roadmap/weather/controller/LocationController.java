package org.roadmap.weather.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class LocationController {
    private final LocationService locationService;
    private final AuthService authService;

    public LocationController(
            LocationService locationService,
            AuthService authService
    ) {
        this.locationService = locationService;
        this.authService = authService;
    }

    @GetMapping("/search")
    public String findLocations(
            @RequestParam String name,
            @RequestAttribute(name = "userId", required = false) Integer userId,
            HttpServletResponse response,
            Model model
    ) {
        if (userId == null) {
            model.addAttribute("isUserAuthorized", false);
        } else {
            Optional<String> login = authService.getLoginById(userId);
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
            @RequestAttribute(name = "userId", required = false) Integer userId,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                model.addAttribute("error", ExceptionMessages.REQUIRE_AUTHORIZATION);
                return "redirect:/auth/sign-in";
            }
            locationService.add(name, latitude, longitude, userId);
            return "redirect:/";
        } catch (LocationAlreadyExistsForUserException ex) {
            redirectAttributes.addFlashAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
            return "redirect:/search?name=" + name;
        }
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(
            @RequestParam String locationId,
            @RequestAttribute(name = "userId", required = false) Integer userId,
            HttpServletResponse response
    ) {
        try {
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return "redirect:/auth/sign-in";
            }
            locationService.delete(locationId, userId);
            return "redirect:/";
        } catch (ValidationException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "redirect:/";
        }
    }
}
