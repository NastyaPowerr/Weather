package org.roadmap.weather.service;

import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class WeatherService {
    @Value("${weather.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final LocationRepository locationRepository;

    public WeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationDto> findByName(String locationName, Integer userId) {
        String url = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=%s&appid=%s", locationName, 10, apiKey);

        String json = restTemplate.getForObject(url, String.class);
        JsonNode root = jsonMapper.readTree(json);
        List<LocationDto> locations = new ArrayList<>();

        for (JsonNode node : root) {
            String name = node.path("name").asString();
            BigDecimal latitude = node.path("lat").asDecimal();
            BigDecimal longitude = node.path("lon").asDecimal();

            LocationDto location = new LocationDto(
                    name,
                    userId,
                    latitude,
                    longitude
            );
            locations.add(location);
        }

        return locations;
    }

    public List<Weather> getWeathersForUser(Integer userId) {
        List<Location> locations = locationRepository.getByUserId(userId);
        List<Weather> weathers = new ArrayList<>();

        for (Location location : locations) {
            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=%s",
                    location.getLatitude(),
                    location.getLongitude(),
                    apiKey,
                    "metric"
            );
            System.out.println(url);
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
        }
        return weathers;
    }

    public void addLocation(String name, String latitude, String longitude, Integer userId) {
        locationRepository.save(new Location(
                name,
                userId,
                new BigDecimal(latitude),
                new BigDecimal(longitude)
        ));
    }

    public void deleteLocation(String locationId, Integer userId) {
        // check if user really owns this location
        locationRepository.delete(Integer.valueOf(locationId));
    }
}
