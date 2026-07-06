package org.roadmap.weather.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.view.UserDto;
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

        addSavedUsernameField(modelAndView, request);
        addAuthAttributes(request, modelAndView);
        return modelAndView;
    }

    @ExceptionHandler(InvalidUserParamsException.class)
    public ModelAndView handleInvalidUserParamsException(InvalidUserParamsException ex, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("sign-in");
        modelAndView.setStatus(HttpStatus.UNAUTHORIZED);
        modelAndView.addObject("error", ex.getMessage());

        addSavedUsernameField(modelAndView, request);
        addAuthAttributes(request, modelAndView);
        return modelAndView;
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public ModelAndView handlePasswordDoNotMatchException(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("sign-up");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("error", ExceptionMessages.PASSWORDS_DO_NOT_MATCH);

        addSavedUsernameField(modelAndView, request);
        addAuthAttributes(request, modelAndView);
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

        addAuthAttributes(request, modelAndView);
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

        boolean hasTypeMismatch = ex.getBindingResult().getAllErrors().stream()
                .anyMatch(error -> error.getCode() != null && error.getCode().contains("typeMismatch"));

        String viewName = getViewName(request);
        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);

        String errorMessage = "Invalid input.";

        if (!errors.isEmpty() && !hasTypeMismatch) {
            errorMessage = errors.get(0);
        }
        modelAndView.addObject("error", errorMessage);

        String uri = request.getRequestURI();
        log.debug("User could not {}. {}", uri, errors, ex);

        Object weathers = request.getAttribute("weathers");
        if (weathers instanceof List<?>) {
            modelAndView.addObject("weathers", weathers);
            modelAndView.addObject("failedWeathers", List.of());
        } else {
            modelAndView.addObject("weathers", List.of());
            modelAndView.addObject("failedWeathers", List.of());
        }
        addSavedUsernameField(modelAndView, request);
        addAuthAttributes(request, modelAndView);
        return modelAndView;
    }

    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleValidationException(ValidationException ex, RedirectAttributes redirectAttributes) {
        log.warn("Validation error: ", ex);

        ModelAndView modelAndView = new ModelAndView("redirect:/");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
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
        addAuthAttributes(request, modelAndView);
        return modelAndView;
    }

    @ExceptionHandler(GeocodingApiCallException.class)
    public ModelAndView handleGeocodingApiCallException(GeocodingApiCallException ex, HttpServletRequest request) {
        log.warn("External Api error - Geocoding Api call failed: ", ex);

        String uri = request.getRequestURI();
        if (uri.contains("/search")) {
            ModelAndView modelAndView = new ModelAndView("search-results");
            modelAndView.addObject("error", "Weather service temporarily unavailable. Please try again later.");
            modelAndView.addObject("weathers", List.of());

            String locationName = request.getParameter("name");
            if (locationName != null) {
                modelAndView.addObject("locationName", locationName);
            }
            addAuthAttributes(request, modelAndView);
            return modelAndView;
        }
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
            modelAndView.addObject("username", user.username());
        }
    }

    private void addSavedUsernameField(
            ModelAndView modelAndView,
            HttpServletRequest request
    ) {
        String value = request.getParameter("username");
        if (value != null) {
            modelAndView.addObject("username", value);
        }
    }
}
