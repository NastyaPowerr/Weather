package org.roadmap.weather.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.exception.location.DuplicateLocationException;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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
    public String handleInvalidUserParams(
            InvalidUserParamsException ex,
            HttpServletResponse response,
            HttpServletRequest request,
            Model model
    ) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        model.addAttribute("error", ex.getMessage());

        addParam(request, model, "login", "login");
        return "sign-in";
    }

    @ExceptionHandler(DuplicateLocationException.class)
    public String handleLocationAlreadyExists(
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model,
            HttpServletResponse response
    ) {
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/search")) {
            redirectAttributes.addFlashAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
            return "redirect:" + referer;
        }
        model.addAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
        return "index";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException ex, HttpServletResponse response) {
        log.warn("Validation error: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "redirect:/";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletResponse response,
            Model model,
            HttpServletRequest request
    ) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        model.addAttribute("errors", errors);

        String uri = request.getRequestURI();
        log.debug("User could not {}. {}", uri, errors);

        if (uri.contains("sign-in")) {
            addParam(request, model, "login", "login");
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            addParam(request, model, "username", "username");
            return "sign-up";
        }
        String error = String.join(", ", errors);
        model.addAttribute("error", error);
        if (uri.contains("locations")) {
            return "index";
        }
        if (uri.contains("search")) {
            addAuthAttributes(request, model);
            return "search-results";
        }
        return "error";
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public String handlePasswordDoNotMatch(
            HttpServletResponse response,
            Model model,
            HttpServletRequest request
    ) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        addParam(request, model, "username", "username");
        model.addAttribute("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
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
            addParam(request, model, "login", "login");
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            return "sign-up";
        }
        return "error";
    }

    @ExceptionHandler(GeocodingApiCallException.class)
    public String handleGeocodingApiCall(
            GeocodingApiCallException ex,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("External Api error - Geocoding Api call failed: ", ex);
        model.addAttribute("error", "Weather service temporarily unavailable. Please try again later.");
        model.addAttribute("weathers", List.of());

        addAuthAttributes(request, model);
        return "index";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex, HttpServletResponse response) {
        log.error("Unexpected error: ", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error";
    }

    private static void addAuthAttributes(HttpServletRequest request, Model model) {
        UserDto user = (UserDto) request.getAttribute("user");
        if (user == null) {
            model.addAttribute("isUserAuthorized", false);
        } else {
            model.addAttribute("isUserAuthorized", true);
            model.addAttribute("userLogin", user.login());
        }
    }

    private void addParam(HttpServletRequest request, Model model, String param, String attributeName) {
        String attribute = request.getParameter(param);
        model.addAttribute(attributeName, attribute);
    }
}
