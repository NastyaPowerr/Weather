package org.roadmap.weather.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.roadmap.weather.util.ValidationConstants;

import java.math.BigDecimal;

public record LocationDto(
        Integer id,

        @NotBlank(message = "Location name is required.")
        @Length(
                min = 2,
                max = 100,
                message = "Location name must be between {min} and {max} characters."
        )
        @Pattern(regexp = "^[A-Za-zА-Яа-я-.,]+$", message = "Location can contain only: English and Russian letters, dots, dashes.")
        String name,

        @NotNull(message = "Latitude is required.")
        @DecimalMin(value = "-90.0", message = ValidationConstants.INVALID_LATITUDE_RANGE)
        @DecimalMax(value = "90.0", message = ValidationConstants.INVALID_LATITUDE_RANGE)
        BigDecimal latitude,

        @NotNull
        @DecimalMin(value = "-180.0", message = ValidationConstants.INVALID_LONGITUDE_RANGE)
        @DecimalMax(value = "180.0", message = ValidationConstants.INVALID_LONGITUDE_RANGE)
        BigDecimal longitude
) {
}
