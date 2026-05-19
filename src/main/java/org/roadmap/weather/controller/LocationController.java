package org.roadmap.weather.controller;

import jakarta.validation.Valid;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.request.SearchDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.service.LocationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public String findLocations(
            @Valid @ModelAttribute SearchDto searchRequest,
            @RequestAttribute(name = "user", required = false) UserDto user,
            Model model
    ) {
        if (user == null) {
            model.addAttribute("isUserAuthorized", false);
        } else {
            model.addAttribute("userLogin", user.login());
            model.addAttribute("isUserAuthorized", true);
        }
        List<LocationDto> locations = locationService.findByName(searchRequest.name());
        model.addAttribute("locations", locations);
        model.addAttribute("locationName", searchRequest.name());
        return "search-results";
    }

    @PostMapping("/locations")
    public String addLocation(
            @Valid @ModelAttribute LocationDto location,
            @RequestAttribute(name = "user", required = false) UserDto user,
            Model model
    ) {
        if (user == null) {
            model.addAttribute("error", ExceptionMessages.REQUIRE_AUTHORIZATION);
            return "index";
        }
        locationService.add(location, user);
        return "redirect:/";
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(
            @RequestParam String locationId,
            @RequestAttribute(name = "user", required = false) UserDto user,
            Model model
    ) {
        try {
            if (user == null) {
                model.addAttribute("error", ExceptionMessages.REQUIRE_AUTHORIZATION);
                return "index";
            }
            locationService.delete(locationId, user.id());
            return "redirect:/";
        } catch (ValidationException ex) {
            return "redirect:/";
        }
    }
}
