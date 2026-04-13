package org.roadmap.weather.service;

import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class LocationService {
    @Value("${weather.api.key}")
    private String apiKey;
    private final LocationRepository locationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void add(String name, String latitude, String longitude, Integer userId) {
        locationRepository.save(new Location(
                name,
                userId,
                new BigDecimal(latitude),
                new BigDecimal(longitude)
        ));
    }

    public List<LocationDto> findByName(String locationName) {
        String url = String.format("https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=%s&appid=%s", locationName, 10, apiKey);
        try {
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = jsonMapper.readTree(json);
            List<LocationDto> locations = new ArrayList<>();

            for (JsonNode node : root) {
                String name = node.path("name").asString();
                BigDecimal latitude = node.path("lat").asDecimal();
                BigDecimal longitude = node.path("lon").asDecimal();

                LocationDto location = new LocationDto(
                        name,
                        latitude,
                        longitude
                );
                locations.add(location);
            }
            return locations;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new GeocodingApiCallException(ex.getMessage());
        }
    }

    @Transactional
    public void delete(String locationId, Integer userId) {
        try {
            Integer id = Integer.valueOf(locationId);
            List<Location> locations = locationRepository.getByUserId(userId);
            for (Location location : locations) {
                if (location.getId().equals(id)) {
                    locationRepository.delete(id);
                }
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
        }
    }
}
