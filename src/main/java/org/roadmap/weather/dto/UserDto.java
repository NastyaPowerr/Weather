package org.roadmap.weather.dto;

import jakarta.validation.constraints.NotBlank;
import org.roadmap.weather.util.ValidationConstants;

public record UserDto(
        @NotBlank(message = ValidationConstants.MISSING_LOGIN)
        String login,
        @NotBlank(message = ValidationConstants.MISSING_PASSWORD)
        String password
) {
}
