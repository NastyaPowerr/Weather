package org.roadmap.weather.client;

import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.dto.openweather.response.WeatherResponseDto;

import java.math.BigDecimal;

public interface WeatherClient {
    LocationResponseDto[] fetchLocations(String locationName);

    WeatherResponseDto fetchWeather(BigDecimal latitude, BigDecimal longitude);
}
