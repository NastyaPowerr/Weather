package org.roadmap.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.roadmap.weather.util.ValidationConstants;

public record UserLoginDto(
        @NotBlank(message = ValidationConstants.MISSING_LOGIN)
        String login,
        @NotBlank(message = ValidationConstants.MISSING_PASSWORD)
        String password
) {
}
