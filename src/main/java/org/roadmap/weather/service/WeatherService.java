package org.roadmap.weather.service;

import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class WeatherService {
    private final static Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final LocationRepository locationRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        List<Weather> weathers = new ArrayList<>();

        long start = System.currentTimeMillis();
        logger.info("Starting: external API call - searching weather for locations...");

        for (Location location : locations) {
            String url = constructUrl(location);
            try {
                WeatherResponseDto response = restTemplate.getForObject(url, WeatherResponseDto.class);
                if (response == null || response.main() == null || response.weather() == null) {
                    logger.warn("Couldn't map weather response for location={}", location.getId());
                    continue;
                }
                weathers.add(
                        new Weather(
                                location.getId(),
                                location.getName(),
                                response.main().temp(),
                                response.main().feels_like(),
                                response.main().humidity(),
                                response.weather().get(0).description()
                        )
                );
            } catch (RestClientException ex) {
                logger.warn("External API call 'searching weather for locations' was not finished. {}", ex.getMessage());
                throw new GeocodingApiCallException(ex.getMessage());
            }
        }
        long difference = System.currentTimeMillis() - start;
        logger.info("Finished: API call - searching weather for locations. Found {} in {}ms", weathers.size(), difference);
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
