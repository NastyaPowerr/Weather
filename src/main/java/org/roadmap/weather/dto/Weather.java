package org.roadmap.weather.dto;

import java.math.BigDecimal;

public record Weather(
        String name,
        BigDecimal temp,
        BigDecimal tempFeelsLike,
        BigDecimal humidity,
        String clouds
) {
}
