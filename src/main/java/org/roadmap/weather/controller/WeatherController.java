package org.roadmap.weather.controller;

import lombok.RequiredArgsConstructor;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.WeatherDto;
import org.roadmap.weather.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/")
    public String getWeathers(
            @RequestAttribute(name = "user", required = false) UserDto user,
            Model model
    ) {
        if (user == null) {
            model.addAttribute("weathers", List.of());
            model.addAttribute("isUserAuthorized", false);
            return "index";
        }
        List<WeatherDto> weathers = weatherService.getWeathersForUser(user.id());
        model.addAttribute("username", user.username());
        model.addAttribute("weathers", weathers);
        model.addAttribute("isUserAuthorized", true);
        return "index";
    }
}
