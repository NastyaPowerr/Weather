package org.roadmap.weather.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SearchDto(
        @NotBlank(message = "Search request cannot be empty")
        @Size(min = 1, max = 100, message = "Search request must be between {min} and {max} characters.")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-я-.'0-9\\s]+$",
                message = "Search request must contain only: English and Russian letters, dots, commas, numbers, spaces, dashes, apostrophes."
        )
        String name
) {
}
