package org.roadmap.weather.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.dto.openweather.response.WeatherResponseDto;
import org.roadmap.weather.exception.client.OpenWeatherApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
@Slf4j
public class OpenWeatherClient implements WeatherClient {
    private static final String LOCATION_URL = "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=%s&appid=%s&lang=%s";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s&lang=%s";
    private static final String WEATHER_LIMIT = "10";
    private static final String WEATHER_LANGUAGE = "ru";
    private static final String WEATHER_UNIT = "metric";

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public LocationResponseDto[] fetchLocations(String locationName) {
        String url = createLocationUrl(locationName);
        try {
            long start = System.currentTimeMillis();
            log.info("Starting: external API call - searching for locations...");

            LocationResponseDto[] locations = restTemplate.getForObject(url, LocationResponseDto[].class);
            long difference = System.currentTimeMillis() - start;
            log.info(
                    "Finished: external API call - searching for locations. Found {} in {}ms",
                    locations == null ? 0 : locations.length,
                    difference
            );
            return locations;
        } catch (RestClientException ex) {
            log.warn("External API call 'searching for locations' was not finished.", ex);
            throw new OpenWeatherApiException(ex.getMessage());
        }
    }

    public WeatherResponseDto fetchWeather(BigDecimal latitude, BigDecimal longitude) {
        String url = createWeatherUrl(latitude, longitude);
        try {
            return restTemplate.getForObject(url, WeatherResponseDto.class);
        } catch (RestClientException ex) {
            log.warn(
                    "Failed to fetch weather for location={}, {}.",
                    latitude,
                    longitude,
                    ex
            );
            throw new OpenWeatherApiException("Failed to fetch weather data.");
        }
    }

    private String createLocationUrl(String locationName) {
        return String.format(
                LOCATION_URL,
                locationName,
                WEATHER_LIMIT,
                apiKey,
                WEATHER_LANGUAGE
        );
    }

    private String createWeatherUrl(BigDecimal latitude, BigDecimal longitude) {
        return String.format(
                WEATHER_URL,
                latitude,
                longitude,
                apiKey,
                WEATHER_UNIT,
                WEATHER_LANGUAGE
        );
    }
}
