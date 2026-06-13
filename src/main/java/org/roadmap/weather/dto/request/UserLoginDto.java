package org.roadmap.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.roadmap.weather.util.ValidationConstants;

public record UserLoginDto(
        @NotBlank(message = ValidationConstants.MISSING_USERNAME)
        String username,
        @NotBlank(message = ValidationConstants.MISSING_PASSWORD)
        String password
) {
}
