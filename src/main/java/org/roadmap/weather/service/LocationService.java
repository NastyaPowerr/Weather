package org.roadmap.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.dto.response.LocationResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.ExternalApiParseException;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.mapper.LocationMapper;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
@Slf4j
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public LocationService(
            LocationRepository locationRepository,
            LocationMapper locationMapper,
            RestTemplate restTemplate
    ) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
        this.restTemplate = restTemplate;
    }

    @Loggable
    @CacheEvict(cacheNames = "locations", key = "#user.id()")
    public void add(String name, String latitude, String longitude, UserDto user) {
        locationRepository.save(
                new Location(
                        name,
                        user.id(),
                        new BigDecimal(latitude),
                        new BigDecimal(longitude)
                ));
    }

    public List<LocationDto> findByName(String locationName) {
        String url = String.format(
                "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=%s&appid=%s",
                locationName,
                10,
                apiKey
        );
        try {
            long start = System.currentTimeMillis();
            log.info("Starting: external API call - searching for locations...");

            LocationResponseDto[] response = restTemplate.getForObject(url, LocationResponseDto[].class);
            List<LocationDto> locations = new ArrayList<>();

            if (response != null) {
                for (LocationResponseDto location : response) {
                    try {
                        locations.add(locationMapper.toDto(location));
                    } catch (ExternalApiParseException ex) {
                        log.warn("Couldn't map location response for name={}", locationName);
                    }
                }
            }
            long difference = System.currentTimeMillis() - start;
            log.info("Finished: external API call - searching for locations. Found {} in {}ms", locations.size(), difference);
            return locations;
        } catch (RestClientException ex) {
            log.warn("External API call 'searching for locations' was not finished. {}", ex.getMessage());
            throw new GeocodingApiCallException(ex.getMessage());
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "locations", key = "#userId")
    public void delete(String locationId, Integer userId) {
        try {
            Integer id = Integer.valueOf(locationId);
            List<Location> locations = locationRepository.findByUserId(userId);
            for (Location location : locations) {
                if (location.getId().equals(id)) {
                    locationRepository.deleteById(id);
                    log.info("User={} deleted location={}", userId, locationId);
                }
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
        }
    }

    @Cacheable(cacheNames = "locations", key = "#userId")
    public List<Location> findByUserId(Integer userId) {
        return locationRepository.findByUserId(userId);
    }
}
