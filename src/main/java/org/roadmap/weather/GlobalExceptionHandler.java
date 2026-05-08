package org.roadmap.weather;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.exception.location.LocationAlreadyExistsForUserException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        model.addAttribute("error", ExceptionMessages.USERNAME_TAKEN);
        return "sign-up";
    }

    @ExceptionHandler(InvalidUserParamsException.class)
    public String handleInvalidUserParams(InvalidUserParamsException ex, HttpServletResponse response, Model model) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        model.addAttribute("error", ex.getMessage());
        return "sign-in";
    }

    @ExceptionHandler(LocationAlreadyExistsForUserException.class)
    public String handleLocationAlreadyExists(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
        String referer = request.getHeader("Referer");
        return "redirect:" + referer;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletResponse response,
            Model model,
            HttpServletRequest request
    ) {
        String error = ex
                .getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        model.addAttribute("error", error);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String uri = request.getRequestURI();
        log.debug("User could not {}. {}", uri, error);
        if (uri.contains("sign-in")) {
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            String username = request.getParameter("username");
            model.addAttribute("username", username);
            return "sign-up";
        }
        return "error";
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public String handlePasswordDoNotMatch(
            HttpServletResponse response,
            Model model,
            HttpServletRequest request
    ) {
        String username = request.getParameter("username");
        model.addAttribute("username", username);

        model.addAttribute("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "sign-up";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletResponse response,
            Model model,
            HttpServletRequest request
    ) {
        String error = ex
                .getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("Constraint violation without controller: {}", error);
        model.addAttribute("error", "Please check your input.");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        String uri = request.getRequestURI();
        if (uri.contains("sign-in")) {
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            return "sign-up";
        }
        return "error";
    }

    @ExceptionHandler(GeocodingApiCallException.class)
    public String handleGeocodingApiCall(GeocodingApiCallException ex, HttpServletResponse response, Model model) {
        log.warn("External Api error - Geocoding Api call failed: ", ex);
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        model.addAttribute("error", "Weather service temporarily unavailable. Please try again later.");
        return "index";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex, HttpServletResponse response) {
        log.error("Unexpected error: ", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error";
    }
}
