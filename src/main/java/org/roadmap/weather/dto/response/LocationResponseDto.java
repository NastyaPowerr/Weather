package org.roadmap.weather.dto.response;

import java.math.BigDecimal;

public record LocationResponseDto(
        String name,
        BigDecimal lat,
        BigDecimal lon
) {
}
