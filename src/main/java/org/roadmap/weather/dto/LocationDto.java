package org.roadmap.weather.dto;

import java.math.BigDecimal;

public record LocationDto(
        String name,
        Integer userId,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
