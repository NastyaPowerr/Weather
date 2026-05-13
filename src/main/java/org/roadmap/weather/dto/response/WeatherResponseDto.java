package org.roadmap.weather.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record WeatherResponseDto(
        Main main,
        List<Weather> weather,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public record Main(
            BigDecimal temp,
            BigDecimal feels_like,
            BigDecimal humidity
    ) {
    }

    public record Weather(
            String description
    ) {
    }
}
