package org.roadmap.weather.dto;

import java.math.BigDecimal;

public record Weather(
        Integer id,
        String name,
        BigDecimal temp,
        BigDecimal tempFeelsLike,
        BigDecimal humidity,
        String clouds
) {
}
