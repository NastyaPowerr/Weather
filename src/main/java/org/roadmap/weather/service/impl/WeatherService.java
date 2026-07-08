package org.roadmap.weather.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.client.WeatherClient;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.WeatherResponseDto;
import org.roadmap.weather.dto.view.WeatherDto;
import org.roadmap.weather.dto.view.WeatherResult;
import org.roadmap.weather.exception.client.WeatherApiException;
import org.roadmap.weather.exception.mapper.ExternalApiParseException;
import org.roadmap.weather.mapper.WeatherMapper;
import org.roadmap.weather.service.WeatherApi;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherService implements WeatherApi {
    private final LocationService locationService;
    private final WeatherMapper weatherMapper;
    private final WeatherClient weatherClient;
    private final ExecutorService weatherApiExecutor;

    @Loggable
    public WeatherResult getWeathersForUser(Integer userId) {
        List<LocationDto> locations = locationService.findByUserId(userId);
        return getWeathersForLocations(locations);
    }

    public WeatherResult getWeathersForLocations(List<LocationDto> locations) {
        List<CompletableFuture<Optional<WeatherDto>>> futures = locations.stream()
                .map(location -> CompletableFuture.supplyAsync(
                        () -> fetchWeather(location),
                        weatherApiExecutor
                ))
                .toList();

        List<WeatherDto> weathers = new ArrayList<>();
        List<String> failedWeathers = new ArrayList<>();

        for (int i = 0; i < futures.size(); i++) {
            Optional<WeatherDto> result = futures.get(i).join();
            if (result.isPresent()) {
                weathers.add(result.get());
            } else {
                failedWeathers.add(locations.get(i).name());
            }
        }
        return new WeatherResult(weathers, failedWeathers);
    }

    private Optional<WeatherDto> fetchWeather(LocationDto location) {
        try {
            WeatherResponseDto response = weatherClient.fetchWeather(location.latitude(), location.longitude());
            return Optional.of(weatherMapper.toWeather(location, response));
        } catch (WeatherApiException | ExternalApiParseException ex) {
            return Optional.empty();
        }
    }
}