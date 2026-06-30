package org.roadmap.weather.dto.internal;

import java.math.BigDecimal;

public record LocationDto(
        Integer id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}