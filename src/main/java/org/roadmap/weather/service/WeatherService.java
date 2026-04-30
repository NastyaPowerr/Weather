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

import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
@Slf4j
public class WeatherService {
    private final LocationService locationService;
    private final WeatherMapper weatherMapper;
    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(
            LocationService locationService,
            WeatherMapper weatherMapper,
            RestTemplate restTemplate
    ) {
        this.locationService = locationService;
        this.weatherMapper = weatherMapper;
        this.restTemplate = restTemplate;
    }

    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationService.findByUserId(userId);
        return getWeathersForLocations(locations);
    }

    private List<Weather> getWeathersForLocations(List<Location> locations) {
        List<Weather> weathers = new ArrayList<>();

        long start = System.currentTimeMillis();
        log.info("Starting: external API call - searching weather for locations...");

        for (Location location : locations) {
            String url = constructUrl(location);
            try {
                WeatherResponseDto response = restTemplate.getForObject(url, WeatherResponseDto.class);
                try {
                    weathers.add(weatherMapper.toWeather(location, response));
                } catch (ExternalApiParseException ex) {
                    log.warn("Couldn't map weather response for location={}", location.getId());
                }
            } catch (RestClientException ex) {
                log.warn("External API call 'searching weather for locations' was not finished. {}", ex.getMessage());
                throw new GeocodingApiCallException(ex.getMessage());
            }
        }
        long difference = System.currentTimeMillis() - start;
        log.info("Finished: API call - searching weather for locations. Found {} in {}ms", weathers.size(), difference);
        return weathers;
    }

    private String constructUrl(Location location) {
        return String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s",
                location.getLatitude(),
                location.getLongitude(),
                apiKey,
                "metric"
        );
    }
}
