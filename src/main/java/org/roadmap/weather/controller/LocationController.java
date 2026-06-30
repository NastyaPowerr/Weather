package org.roadmap.weather.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.request.SearchDto;
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
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/search")
    public String findLocations(
            @Valid @ModelAttribute SearchDto searchRequest,
            @RequestAttribute(name = "user", required = false) UserDto user,
            Model model
    ) {
        if (user == null) {
            model.addAttribute("isUserAuthorized", false);
        } else {
            model.addAttribute("username", user.username());
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
            @RequestAttribute("user") UserDto user
    ) {
        locationService.add(location, user);
        return "redirect:/";
    }

    @PostMapping("/locations/delete")
    public String deleteLocation(
            @RequestParam String locationId,
            @RequestAttribute("user") UserDto user
    ) {
        locationService.delete(locationId, user.id());
        return "redirect:/";

    }
}
