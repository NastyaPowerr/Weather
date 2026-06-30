package org.roadmap.weather.dto.openweather.response;

import java.math.BigDecimal;
import java.util.Map;

public record LocationResponseDto(
        String name,
        BigDecimal lat,
        BigDecimal lon,
        Map<String, String> local_names
) {
}
