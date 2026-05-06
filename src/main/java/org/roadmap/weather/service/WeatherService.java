package org.roadmap.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExternalApiParseException;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
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
public class WeatherService {
    private final LocationService locationService;
    private final WeatherMapper weatherMapper;
    private final RestTemplate restTemplate;
    private final ExecutorService weatherApiExecutor;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(
            LocationService locationService,
            WeatherMapper weatherMapper,
            RestTemplate restTemplate,
            ExecutorService executorService
    ) {
        this.locationService = locationService;
        this.weatherMapper = weatherMapper;
        this.restTemplate = restTemplate;
        this.weatherApiExecutor = executorService;
    }

    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationService.findByUserId(userId);
        return getWeathersForLocations(locations);
    }

    public List<Weather> getWeathersForLocations(List<Location> locations) {
        long start = System.currentTimeMillis();
        log.info("Starting: external API call - searching weather for locations...");

        List<CompletableFuture<Optional<Weather>>> futures = locations.stream()
                .map(location -> CompletableFuture.supplyAsync(
                        () -> fetchWeather(location),
                        weatherApiExecutor
                ))
                .toList();

        List<Weather> weathers = futures.stream()
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

    private Optional<Weather> fetchWeather(Location location) {
        try {
            String url = createUrl(location);
            WeatherResponseDto response = restTemplate.getForObject(url, WeatherResponseDto.class);
            return Optional.of(weatherMapper.toWeather(location, response));
        } catch (RestClientException | ExternalApiParseException ex) {
            log.warn("Failed to fetch weather for location={}. {}", location.getId(), ex.getMessage());
        }
        return Optional.empty();
    }

    private String createUrl(Location location) {
        return String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s",
                location.getLatitude(),
                location.getLongitude(),
                apiKey,
                "metric"
        );
    }
}
