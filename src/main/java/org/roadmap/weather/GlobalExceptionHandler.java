package org.roadmap.weather;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.exception.location.LocationAlreadyExistsForUserException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public String handlePasswordDoNotMatch(HttpServletResponse response, Model model) {
        model.addAttribute("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return "sign-up";
    }

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
    public String handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletResponse response, Model model, HttpServletRequest request) {
        String error = ex
                .getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();
        model.addAttribute("error", error);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String uri = request.getRequestURI();
        logger.debug("User could not {}. {}", uri, error);
        if (uri.contains("sign-in")) {
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            return "sign-up";
        }
        return "error";
    }

    @ExceptionHandler(GeocodingApiCallException.class)
    public String handleGeocodingApiCall(GeocodingApiCallException ex, HttpServletResponse response) {
        logger.warn("External Api error - Geocoding Api call failed: ", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex, HttpServletResponse response) {
        logger.error("Unexpected error: ", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return "error";
    }
}
