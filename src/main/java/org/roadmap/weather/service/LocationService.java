package org.roadmap.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.client.OpenWeatherClient;
import org.roadmap.weather.client.WeatherClient;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.dto.request.LocationRequestDto;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.mapper.ExternalApiParseException;
import org.roadmap.weather.mapper.LocationMapper;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final WeatherClient weatherClient;

    @Loggable
    @CacheEvict(cacheNames = "locations", key = "#user.id()")
    @Transactional
    public void add(LocationRequestDto locationDto, UserDto user) {
        locationRepository.save(locationMapper.toEntity(locationDto, user.id()));
    }

    @Loggable
    @Cacheable(cacheNames = "locations", key = "#userId")
    @Transactional(readOnly = true)
    public List<LocationDto> findByUserId(Integer userId) {
        return locationMapper.toDtoList(locationRepository.findByUserId(userId));
    }

    @Loggable
    @CacheEvict(cacheNames = "locations", key = "#userId")
    @Transactional
    public void delete(String locationId, Integer userId) {
        try {
            Integer id = Integer.valueOf(locationId);
            int deletedLocations = locationRepository.deleteByIdAndUserId(id, userId);
            if (deletedLocations == 0) {
                throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException(ExceptionMessages.LOCATION_NOT_FOUND_FOR_USER);
        }
    }

    @Loggable
    @Cacheable(cacheNames = "openWeatherMapLocations", key = "#locationName")
    public List<LocationDto> findByName(String locationName) {
        LocationResponseDto[] responseLocations = weatherClient.fetchLocations(locationName);
        List<LocationDto> locations = new ArrayList<>();
        if (responseLocations != null) {
            for (LocationResponseDto location : responseLocations) {
                try {
                    locations.add(locationMapper.toDto(location));
                } catch (ExternalApiParseException ex) {
                    log.warn("Couldn't map location response for name={}", locationName);
                }
            }
        }
        return locations;
    }

    @Scheduled(cron = "0 0 10 * * *")
    @CacheEvict(cacheNames = "openWeatherMapLocations", allEntries = true)
    @Loggable
    public void evictLocationCache() {
    }
}
