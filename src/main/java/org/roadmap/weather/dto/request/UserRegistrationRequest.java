package org.roadmap.weather.dto.request;

import org.hibernate.validator.constraints.Length;
import org.roadmap.weather.util.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegistrationRequest(
        @NotBlank(message = ValidationConstants.MISSING_LOGIN)
        @Length(
                min = ValidationConstants.MIN_LOGIN_LENGTH,
                max = ValidationConstants.MAX_LOGIN_LENGTH,
                message = ValidationConstants.INVALID_LOGIN_LENGTH
        )
        @Pattern(regexp = ValidationConstants.LOGIN_PATTERN, message = ValidationConstants.INVALID_LOGIN_PATTERN)
        String username,

        @NotBlank(message = ValidationConstants.MISSING_PASSWORD)
        @Length(
                min = ValidationConstants.MIN_PASSWORD_LENGTH,
                max = ValidationConstants.MAX_PASSWORD_LENGTH,
                message = ValidationConstants.INVALID_PASSWORD_LENGTH
        )
        String password,

        @NotBlank(message = ValidationConstants.MISSING_REPEATED_PASSWORD)
        String repeatedPassword
) {
}