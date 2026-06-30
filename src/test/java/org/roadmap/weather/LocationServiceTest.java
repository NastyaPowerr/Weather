package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.LocationResponseDto;
import org.roadmap.weather.exception.GeocodingApiCallException;
import org.roadmap.weather.mapper.LocationMapper;
import org.roadmap.weather.repository.LocationRepository;
import org.roadmap.weather.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

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

        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenReturn(new LocationResponseDto[]{response});

        List<LocationDto> locations = locationService.findByName("Moscow");
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

        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenReturn(new LocationResponseDto[]{firstMoscow, secondMoscow});

        List<LocationDto> locations = locationService.findByName("Moscow");
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
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenReturn(new LocationResponseDto[0]);

        List<LocationDto> locations = locationService.findByName("Moscow");
        Assertions.assertTrue(locations.isEmpty());
    }

    @Test
    void givenFindByName_whenApiReturnsNull_thenReturnEmptyList() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenReturn(null);

        List<LocationDto> locations = locationService.findByName("Moscow");
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

        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenReturn(new LocationResponseDto[]{
                        responseWithEmptyName,
                        responseWithEmptyLatitude,
                        responseWithEmptyLongitude
                });

        List<LocationDto> locations = locationService.findByName("Moscow");
        Assertions.assertEquals(0, locations.size());
    }

    @Test
    void givenFindByName_whenApiReturnsBadRequest_thenThrowException() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        Assertions.assertThrows(GeocodingApiCallException.class, () -> locationService.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsUnauthorized_thenThrowException() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        Assertions.assertThrows(GeocodingApiCallException.class, () -> locationService.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsNotFound_thenThrowException() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Assertions.assertThrows(GeocodingApiCallException.class, () -> locationService.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsTooManyRequests_thenThrowException() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        Assertions.assertThrows(GeocodingApiCallException.class, () -> locationService.findByName("Moscow"));
    }

    @Test
    void givenFindByName_whenApiReturnsUnexceptedError_thenThrowException() {
        when(restTemplate.getForObject(anyString(), eq(LocationResponseDto[].class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(GeocodingApiCallException.class, () -> locationService.findByName("Moscow"));
    }
}
