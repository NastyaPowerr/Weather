package org.roadmap.weather.dto.view;

import java.util.List;

public record WeatherResult(
        List<WeatherDto> weathers,
        List<String> failedWeathers
) {
}
