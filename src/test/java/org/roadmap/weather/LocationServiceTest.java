package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roadmap.weather.client.WeatherClient;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.exception.client.OpenWeatherApiException;
import org.roadmap.weather.mapper.LocationMapper;
import org.roadmap.weather.repository.LocationRepository;
import org.roadmap.weather.service.LocationApi;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private LocationApi locationApi;

    @Spy
    private LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);

    private final Map<String, String> localNames = new HashMap<>();

    @Test
    void givenFindByName_whenApiReturnsValidResponse_thenReturnLocations() {
        LocationResponseDto response = new LocationResponseDto(
                "Moscow",
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943"),
                localNames

        );

        when(weatherClient.fetchLocations("Moscow"))
                .thenReturn(new LocationResponseDto[]{response});

        List<LocationDto> locations = locationApi.findByName("Moscow");
        LocationDto location = locations.get(0);

        Assertions.assertEquals("Moscow", location.name());
        Assertions.assertEquals(new BigDecimal("55.7504461"), location.latitude());
        Assertions.assertEquals(new BigDecimal("37.6174943"), location.longitude());
    }

    @Test
    void givenFindByName_whenApiReturnsMultipleLocations_thenReturnAll() {
        LocationResponseDto firstMoscow = new LocationResponseDto(
                "Moscow",
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943"),
                localNames
        );

        LocationResponseDto secondMoscow = new LocationResponseDto(
                "Moscow",
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651"),
                localNames
        );

        when(weatherClient.fetchLocations("Moscow"))
                .thenReturn(new LocationResponseDto[]{firstMoscow, secondMoscow});

        List<LocationDto> locations = locationApi.findByName("Moscow");
        LocationDto firstFoundMoscow = locations.get(0);
        LocationDto secondFoundMoscow = locations.get(1);

        Assertions.assertEquals(2, locations.size());

        Assertions.assertEquals("Moscow", firstFoundMoscow.name());
        Assertions.assertEquals(new BigDecimal("55.7504461"), firstFoundMoscow.latitude());
        Assertions.assertEquals(new BigDecimal("37.6174943"), firstFoundMoscow.longitude());

        Assertions.assertEquals("Moscow", secondFoundMoscow.name());
        Assertions.assertEquals(new BigDecimal("46.7323875"), secondFoundMoscow.latitude());
        Assertions.assertEquals(new BigDecimal("-117.0001651"), secondFoundMoscow.longitude());
    }


    @Test
    void givenFindByName_whenApiReturnsEmptyArray_thenReturnEmptyList() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenReturn(new LocationResponseDto[0]);

        List<LocationDto> locations = locationApi.findByName("Moscow");
        Assertions.assertTrue(locations.isEmpty());
    }

    @Test
    void givenFindByName_whenApiReturnsNull_thenReturnEmptyList() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenReturn(null);

        List<LocationDto> locations = locationApi.findByName("Moscow");
        Assertions.assertTrue(locations.isEmpty());
    }

    @Test
    void givenFindByName_whenApiReturnsNotFullResponse_thenNotAddThisLocation() {
        LocationResponseDto responseWithEmptyName = new LocationResponseDto(
                null,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943"),
                localNames
        );

        LocationResponseDto responseWithEmptyLatitude = new LocationResponseDto(
                "Moscow",
                null,
                new BigDecimal("37.6174943"),
                localNames
        );

        LocationResponseDto responseWithEmptyLongitude = new LocationResponseDto(
                "Moscow",
                new BigDecimal("55.7504461"),
                null,
                localNames
        );

        when(weatherClient.fetchLocations("Moscow"))
                .thenReturn(new LocationResponseDto[]{
                        responseWithEmptyName,
                        responseWithEmptyLatitude,
                        responseWithEmptyLongitude
                });

        List<LocationDto> locations = locationApi.findByName("Moscow");
        Assertions.assertEquals(0, locations.size());
    }

    @Test
    void givenFindByName_whenApiReturnsBadRequest_thenThrowException() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenThrow(new OpenWeatherApiException("Bad Request"));

        Assertions.assertThrows(OpenWeatherApiException.class, () -> locationApi.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsUnauthorized_thenThrowException() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenThrow(new OpenWeatherApiException("Unauthorized"));

        Assertions.assertThrows(OpenWeatherApiException.class, () -> locationApi.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsNotFound_thenThrowException() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenThrow(new OpenWeatherApiException("Not found"));

        Assertions.assertThrows(OpenWeatherApiException.class, () -> locationApi.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsTooManyRequests_thenThrowException() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenThrow(new OpenWeatherApiException("Too many requests"));

        Assertions.assertThrows(OpenWeatherApiException.class, () -> locationApi.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsUnexceptedError_thenThrowException() {
        when(weatherClient.fetchLocations("Moscow"))
                .thenThrow(new OpenWeatherApiException("Internal server error"));

        Assertions.assertThrows(OpenWeatherApiException.class, () -> locationApi.findByName("Moscow"));
    }
}
