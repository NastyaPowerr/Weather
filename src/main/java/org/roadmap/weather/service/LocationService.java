package org.roadmap.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.dto.request.LocationRequestDto;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.GeocodingApiCallException;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.mapper.ExternalApiParseException;
import org.roadmap.weather.mapper.LocationMapper;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private static final String WEATHER_URL = "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=%s&appid=%s&lang=%s";
    private static final String WEATHER_LIMIT = "10";
    private static final String WEATHER_LANGUAGE = "ru";

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    @Loggable
    @CacheEvict(cacheNames = "locations", key = "#user.id()")
    @Transactional
    public void add(LocationRequestDto locationDto, UserDto user) {
        locationRepository.save(locationMapper.toEntity(locationDto, user.id()));
    }

    @Cacheable(cacheNames = "locations", key = "#userId")
    @Transactional(readOnly = true)
    public List<LocationDto> findByUserId(Integer userId) {
        return locationMapper.toDtoList(locationRepository.findByUserId(userId));
    }

    @CacheEvict(cacheNames = "locations", key = "#userId")
    @Transactional
    public void delete(String locationId, Integer userId) {
        try {
            Integer id = Integer.valueOf(locationId);
            int deletedLocations = locationRepository.deleteByIdAndUserId(id, userId);
            if (deletedLocations == 0) {
                throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
            }
            log.info("User={} deleted location={}", userId, locationId);
        } catch (NumberFormatException ex) {
            throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
        }
    }

    @Cacheable(cacheNames = "openWeatherMapLocations", key = "#locationName")
    public List<LocationDto> findByName(String locationName) {
        String url = createUrl(locationName);
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
            log.info(
                    "Finished: external API call - searching for locations. Found {} in {}ms",
                    locations.size(),
                    difference
            );
            return locations;
        } catch (RestClientException ex) {
            log.warn("External API call 'searching for locations' was not finished. {}", ex.getMessage());
            throw new GeocodingApiCallException(ex.getMessage());
        }
    }

    @Scheduled(cron = "0 0 10 * * *")
    @CacheEvict(cacheNames = "openWeatherMapLocations", allEntries = true)
    public void evictLocationCache() {
        log.info("Evicted openWeatherApi locations cache.");
    }

    private String createUrl(String locationName) {
        return String.format(
                WEATHER_URL,
                locationName,
                WEATHER_LIMIT,
                apiKey,
                WEATHER_LANGUAGE
        );
    }
}
