package org.roadmap.weather.dto.view;

import java.math.BigDecimal;

public record WeatherDto(
        Integer id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal temp,
        BigDecimal tempFeelsLike,
        BigDecimal humidity,
        String clouds
) {
}
