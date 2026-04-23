package org.roadmap.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.request.UserRegistrationRequest;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.util.CookieManagerUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/sign-up")
    public String register() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String register(
            @Valid @ModelAttribute UserRegistrationRequest user,
            BindingResult res,
            HttpServletResponse response,
            Model model
    ) {
        if (res.hasErrors()) {
            String error = res.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("error", error);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "sign-up";
        }
        if (!user.password().equals(user.repeatedPassword())) {
            model.addAttribute("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "sign-up";
        }
        UserDto userToRegister = new UserDto(user.username(), user.password());
        authService.register(userToRegister);
        return "redirect:/";
    }

    @GetMapping("/sign-in")
    public String login() {
        return "sign-in";
    }

    @PostMapping("/sign-in")
    public String login(
            UserDto user,
            HttpServletResponse response
    ) {
        SessionDto session = authService.authorize(user);
        String sessionId = String.valueOf(session.id());
        Cookie cookie = CookieManagerUtil.createCookie(sessionId);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
        return "redirect:/";
    }

    @PostMapping("/sign-out")
    public String logout(
            @RequestAttribute(name = "sessionId", required = false) String sessionId,
            HttpServletResponse response
    ) {
        if (sessionId != null) {
            authService.logout(sessionId);
        }
        response.addCookie(CookieManagerUtil.deleteCookie());
        return "redirect:/";
    }
}
