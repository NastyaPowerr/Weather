package org.roadmap.weather.dto;

import java.math.BigDecimal;

public record LocationDto(
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
