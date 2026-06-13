package org.roadmap.weather.dto.request;

import org.hibernate.validator.constraints.Length;
import org.roadmap.weather.util.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRegisterDto(
        @NotBlank(message = ValidationConstants.MISSING_USERNAME)
        @Length(
                min = 2,
                max = 20,
                message = "Login length must be between {min} and {max} characters."
        )
        @Pattern(regexp = "^[A-Za-zА-Яа-я-.@0-9]+$", message = "Login can contain only: English and Russian letters, dots, commercial at.")
        String username,

        @NotBlank(message = ValidationConstants.MISSING_PASSWORD)
        @Length(
                min = 6,
                max = 50,
                message = "Password length must be between {min} and {max} characters."
        )
        String password,

        @NotBlank(message = "Repeat your password.")
        String repeatedPassword
) {
}