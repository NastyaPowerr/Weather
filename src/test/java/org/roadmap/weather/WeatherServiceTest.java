package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.WeatherDto;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.exception.GeocodingApiCallException;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class WeatherServiceTest {
    private static LocationDto MOSCOW_LOCATION;
    private static LocationDto LONDON_LOCATION;
    private static WeatherResponseDto MOSCOW_RESPONSE;
    private static WeatherResponseDto LONDON_RESPONSE;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private LocationService locationService;

    @Autowired
    private WeatherService weatherService;

    private List<LocationDto> locations;

    @BeforeAll
    static void setup() {
        MOSCOW_LOCATION = new LocationDto(
                1,
                "Moscow",
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        LONDON_LOCATION = new LocationDto(
                1,
                "London",
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );
        MOSCOW_RESPONSE = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy")),
                new BigDecimal("55.7504461"),
                new BigDecimal("37.6174943")
        );
        LONDON_RESPONSE = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("16.8"),
                        new BigDecimal("20"),
                        new BigDecimal("88.5")
                ),
                List.of(new WeatherResponseDto.Weather("rain")),
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );
    }

    @Test
    void givenLocation_whenApiReturnsValidResponse_thenReturnWeathers() {
        locations = List.of(MOSCOW_LOCATION);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(MOSCOW_RESPONSE);
        List<WeatherDto> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals("Moscow", weathers.get(0).name());
        Assertions.assertEquals(new BigDecimal("6.0"), weathers.get(0).temp());
    }

    @Test
    void givenMultipleLocations_whenApiReturnsValidResponse_thenReturnWeathers() {
        locations = List.of(MOSCOW_LOCATION, LONDON_LOCATION);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(MOSCOW_RESPONSE, LONDON_RESPONSE);
        List<WeatherDto> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(2, weathers.size());

        Assertions.assertEquals(new BigDecimal("6.0"), weathers.get(0).temp());
        Assertions.assertEquals(new BigDecimal("16.8"), weathers.get(1).temp());
    }

    @Test
    void givenEmptyLocationList_whenApiReturnsValidResponse_thenReturnEmptyWeatherList() {
        locations = new ArrayList<>();
        List<WeatherDto> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(0, weathers.size());
    }

    @Test
    void givenLocations_whenApiReturnsInvalidResponse_thenSkipLocation() {
        WeatherResponseDto firstResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        null,
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy")),
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );
        WeatherResponseDto secondResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        null,
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather("cloudy")),
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );
        WeatherResponseDto thirdResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        null
                ),
                List.of(new WeatherResponseDto.Weather("cloudy")),
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );
        WeatherResponseDto fourthResponse = new WeatherResponseDto(
                new WeatherResponseDto.Main(
                        new BigDecimal("6.0"),
                        new BigDecimal("2.3"),
                        new BigDecimal("85")
                ),
                List.of(new WeatherResponseDto.Weather(null)),
                new BigDecimal("46.7323875"),
                new BigDecimal("-117.0001651")
        );

        locations = List.of(MOSCOW_LOCATION, MOSCOW_LOCATION, MOSCOW_LOCATION, MOSCOW_LOCATION, MOSCOW_LOCATION);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenReturn(firstResponse, secondResponse, thirdResponse, fourthResponse, MOSCOW_RESPONSE);
        List<WeatherDto> weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(1, weathers.size());
    }

    @Test
    void givenLocation_whenApiReturnsBadRequest_thenThrowException() {
        locations = List.of(MOSCOW_LOCATION);

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
        locations = List.of(MOSCOW_LOCATION);

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
        locations = List.of(MOSCOW_LOCATION);

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
        locations = List.of(MOSCOW_LOCATION);

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
        locations = List.of(MOSCOW_LOCATION);

        when(restTemplate
                .getForObject(anyString(), eq(WeatherResponseDto.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(
                GeocodingApiCallException.class,
                () -> weatherService.getWeathersForLocations(locations)
        );
    }
}
