package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.location.GeocodingApiCallException;
import org.roadmap.weather.mapper.WeatherMapper;
import org.roadmap.weather.service.LocationService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class WeatherServiceTest {
    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private LocationService locationService;

    @Autowired
    private WeatherService weatherService;

    @Test
    void givenLocation_whenApiReturnsValidResponse_thenReturnWeathers() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );

        WeatherResponseDto response = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy"))
        );

        List<Location> locations = List.of(location);
        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(response);
        List<Weather> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals("Moscow", weathers.get(0).name());
        Assertions.assertEquals(new BigDecimal("6.0"), weathers.get(0).temp());
    }

    @Test
    void givenMultipleLocations_whenApiReturnsValidResponse_thenReturnWeathers() {
        Location firstLocation = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );

        Location secondLocation = new Location(
                "London",
                1,
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );

        WeatherResponseDto firstResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy"))
        );

        WeatherResponseDto secondResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("16.8"),
                        new BigDecimal("20"),
                        new BigDecimal("88.5")
                ),
                List.of(new WeatherResponseDto.Weather("rain"))
        );

        List<Location> locations = List.of(firstLocation, secondLocation);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(firstResponse, secondResponse);
        List<Weather> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(2, weathers.size());

        Assertions.assertEquals(new BigDecimal("6.0"), weathers.get(0).temp());
        Assertions.assertEquals(new BigDecimal("16.8"), weathers.get(1).temp());
    }

    @Test
    void givenEmptyLocationList_whenApiReturnsValidResponse_thenReturnEmptyWeatherList() {
        List<Location> locations = List.of();
        List<Weather> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(0, weathers.size());
    }

    @Test
    void givenLocations_whenApiReturnsInvalidResponse_thenSkipLocation() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );

        WeatherResponseDto firstResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        null,
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy"))
        );

        WeatherResponseDto secondResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        null,
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy"))
        );
        WeatherResponseDto thirdResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        null
                ),
                List.of(new WeatherResponseDto.Weather("cloudy"))
        );
        WeatherResponseDto fourthResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather(null))
        );

        WeatherResponseDto validResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("rainy"))
        );

        List<Location> locations = List.of(location, location, location, location, location);
        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(firstResponse, secondResponse, thirdResponse, fourthResponse, validResponse);
        List<Weather> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(1, weathers.size());
    }

    @Test
    void givenLocation_whenApiReturnsBadRequest_thenThrowException() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        List<Location> locations = List.of(location);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }

    @Test
    void givenLocation_whenApiReturnsUnauthorized_thenThrowException() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        List<Location> locations = List.of(location);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }

    @Test
    void givenLocation_whenApiReturnsNotFound_thenThrowException() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        List<Location> locations = List.of(location);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }

    @Test
    void givenLocation_whenApiReturnsTooManyRequests_thenThrowException() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        List<Location> locations = List.of(location);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }

    @Test
    void givenLocation_whenApiReturnsUnexpectedError_thenThrowException() {
        Location location = new Location(
                "Moscow",
                1,
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        List<Location> locations = List.of(location);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }
}
