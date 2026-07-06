package org.roadmap.weather.controller;

import lombok.RequiredArgsConstructor;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.dto.view.WeatherResult;
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
        WeatherResult weatherResult = weatherService.getWeathersForUser(user.id());
        model.addAttribute("username", user.username());
        model.addAttribute("weathers", weatherResult.weathers());
        model.addAttribute("failedWeathers", weatherResult.failedWeathers());

        model.addAttribute("isUserAuthorized", true);
        return "index";
    }
}
