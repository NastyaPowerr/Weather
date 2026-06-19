package org.roadmap.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.WeatherDto;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.exception.GeocodingApiCallException;
import org.roadmap.weather.exception.mapper.ExternalApiParseException;
import org.roadmap.weather.mapper.WeatherMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@PropertySource("classpath:application.properties")
@Slf4j
@RequiredArgsConstructor
public class WeatherService {
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s&lang=%s";
    private static final String WEATHER_UNIT = "metric";
    private static final String WEATHER_LANGUAGE = "ru";

    private final LocationService locationService;
    private final WeatherMapper weatherMapper;
    private final RestTemplate restTemplate;
    private final ExecutorService weatherApiExecutor;

    @Value("${weather.api.key}")
    private String apiKey;

    public List<WeatherDto> getWeathersForUser(Integer userId) {
        List<LocationDto> locations = locationService.findByUserId(userId);
        return getWeathersForLocations(locations);
    }

    public List<WeatherDto> getWeathersForLocations(List<LocationDto> locations) {
        long start = System.currentTimeMillis();
        log.info("Starting: external API call - searching weather for locations...");

        List<CompletableFuture<Optional<WeatherDto>>> futures = locations.stream()
                .map(location -> CompletableFuture.supplyAsync(
                        () -> fetchWeather(location),
                        weatherApiExecutor
                ))
                .toList();

        List<WeatherDto> weathers = futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (!locations.isEmpty() && weathers.isEmpty()) {
            throw new GeocodingApiCallException("Unable to fetch weather data. Please try again later.");
        }
        long difference = System.currentTimeMillis() - start;
        log.info("Finished: API call - searching weather for locations. Found {} in {}ms", weathers.size(), difference);
        return weathers;
    }

    private Optional<WeatherDto> fetchWeather(LocationDto location) {
        try {
            String url = createUrl(location);
            WeatherResponseDto response = restTemplate.getForObject(url, WeatherResponseDto.class);
            return Optional.of(weatherMapper.toWeather(location, response));
        } catch (RestClientException | ExternalApiParseException ex) {
            log.warn(
                    "Failed to fetch weather for location={}, {}, {}. {}",
                    location.name(),
                    location.latitude(),
                    location.longitude(),
                    ex.getMessage()
            );
        }
        return Optional.empty();
    }

    private String createUrl(LocationDto location) {
        return String.format(
                WEATHER_URL,
                location.latitude(),
                location.longitude(),
                apiKey,
                WEATHER_UNIT,
                WEATHER_LANGUAGE
        );
    }
}