package org.roadmap.weather.service;

import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class WeatherService {
    private final static Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final LocationRepository locationRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    //  org.springframework.web.client.ResourceAccessException: I/O error on GET request for "https://api.openweathermap.org/data/2.5/weather": Connection timed out: connect
    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationRepository.findByUserId(userId);
        List<Weather> weathers = new ArrayList<>();

        long start = System.currentTimeMillis();
        logger.info("Starting: external API call - searching weather for locations...");
        for (Location location : locations) {
            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s",
                    location.getLatitude(),
                    location.getLongitude(),
                    apiKey,
                    "metric"
            );
            try {
                String json = restTemplate.getForObject(url, String.class);
                JsonNode root = jsonMapper.readTree(json);

                String name = location.getName();
                BigDecimal temp = root.path("main").path("temp").asDecimal();
                BigDecimal tempFeelsLike = root.path("main").path("feels_like").asDecimal();
                BigDecimal humidity = root.path("main").path("humidity").asDecimal();
                String clouds = root.path("weather").get(0).path("description").asString();
                weathers.add(
                        new Weather(
                                location.getId(),
                                name,
                                temp,
                                tempFeelsLike,
                                humidity,
                                clouds
                        )
                );
            } catch (HttpClientErrorException | HttpServerErrorException ex) {
                logger.warn("External API call 'searching weather for locations' was not finished. {}", ex.getMessage());
                throw new GeocodingApiCallException(ex.getMessage());
            } catch (Exception ex) {
                logger.warn("External API call 'searching weather for locations' was not finished. {}", ex.getMessage());
                throw ex;
            }
        }
        long difference = System.currentTimeMillis() - start;
        logger.info("Finished: API call - searching weather for locations. Found {} in {}ms", weathers.size(), difference);
        return weathers;
    }
}
