package org.roadmap.weather.dto.view;

import java.math.BigDecimal;

public record LocationViewDto(
        Integer id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}