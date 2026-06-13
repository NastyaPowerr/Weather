package org.roadmap.weather.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.exception.location.DuplicateLocationException;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.PasswordsDoNotMatchException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("sign-up");
        modelAndView.setStatus(HttpStatus.CONFLICT);
        modelAndView.addObject("error", ExceptionMessages.USERNAME_TAKEN);

        addParam(modelAndView, request, "username", "username");

        return modelAndView;
    }

    @ExceptionHandler(InvalidUserParamsException.class)
    public ModelAndView handleInvalidUserParamsException(InvalidUserParamsException ex, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("sign-in");
        modelAndView.setStatus(HttpStatus.UNAUTHORIZED);
        modelAndView.addObject("error", ex.getMessage());

        addParam(modelAndView, request, "login", "login");
        return modelAndView;
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public ModelAndView handlePasswordDoNotMatchException(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("sign-up");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);

        addParam(modelAndView, request, "username", "username");
        return modelAndView;
    }

    @ExceptionHandler(DuplicateLocationException.class)
    public ModelAndView handleDuplicateLocationException(
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/search")) {
            redirectAttributes.addFlashAttribute("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
            return new ModelAndView("redirect:" + referer);
        }

        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.setStatus(HttpStatus.CONFLICT);
        modelAndView.addObject("error", ExceptionMessages.USER_ALREADY_HAS_LOCATION);
        return modelAndView;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String viewName = getViewName(request);
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);

        modelAndView.addObject("errors", errors);
        String error = String.join(", ", errors);
        modelAndView.addObject("error", error);

        String uri = request.getRequestURI();
        log.debug("User could not {}. {}", uri, errors);

        if (uri.contains("sign-in")) {
            addParam(modelAndView, request, "login", "login");
        }
        if (uri.contains("sign-up")) {
            addParam(modelAndView, request, "username", "username");
        }
        if (uri.contains("search")) {
            addAuthAttributes(request, modelAndView);
        }
        return modelAndView;
    }

    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleValidationException(ValidationException ex) {
        log.warn("Validation error: ", ex);

        ModelAndView modelAndView = new ModelAndView("redirect:/");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        return modelAndView;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Constraint violation without controller: ", ex);

        String viewName = getViewName(request);
        ModelAndView modelAndView = new ModelAndView(viewName);

        modelAndView.addObject("error", "Please check your input.");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        String uri = request.getRequestURI();
        if (uri.contains("sign-in")) {
            addParam(modelAndView, request, "login", "login");
        }
        if (uri.contains("sign-up")) {
            addParam(modelAndView, request, "username", "username");
        }
        return modelAndView;
    }

    @ExceptionHandler(GeocodingApiCallException.class)
    public ModelAndView handleGeocodingApiCallException(GeocodingApiCallException ex, HttpServletRequest request) {
        log.warn("External Api error - Geocoding Api call failed: ", ex);

        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("error", "Weather service temporarily unavailable. Please try again later.");
        modelAndView.addObject("weathers", List.of());

        addAuthAttributes(request, modelAndView);
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpectedException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        return modelAndView;
    }

    private String getViewName(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("sign-in")) {
            return "sign-in";
        }
        if (uri.contains("sign-up")) {
            return "sign-up";
        }
        if (uri.contains("locations")) {
            return "index";
        }
        if (uri.contains("search")) {
            return "search-results";
        }
        return "error";
    }

    private void addAuthAttributes(HttpServletRequest request, ModelAndView modelAndView) {
        UserDto user = (UserDto) request.getAttribute("user");
        if (user == null) {
            modelAndView.addObject("isUserAuthorized", false);
        } else {
            modelAndView.addObject("isUserAuthorized", true);
            modelAndView.addObject("userLogin", user.login());
        }
    }

    private void addParam(
            ModelAndView modelAndView,
            HttpServletRequest request,
            String paramName,
            String attributeName
    ) {
        String value = request.getParameter(paramName);
        if (value != null) {
            modelAndView.addObject(attributeName, value);
        }
    }
}
