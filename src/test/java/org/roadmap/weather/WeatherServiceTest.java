package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roadmap.weather.client.WeatherClient;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.openweather.response.WeatherResponseDto;
import org.roadmap.weather.dto.view.WeatherResult;
import org.roadmap.weather.exception.client.OpenWeatherApiException;
import org.roadmap.weather.service.LocationService;
import org.roadmap.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class WeatherServiceTest {
    private static LocationDto MOSCOW_LOCATION;
    private static LocationDto LONDON_LOCATION;
    private static WeatherResponseDto MOSCOW_RESPONSE;
    private static WeatherResponseDto LONDON_RESPONSE;

    @MockitoBean
    private WeatherClient weatherClient;

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

        when(weatherClient.fetchWeather(MOSCOW_LOCATION.latitude(), MOSCOW_LOCATION.longitude()))
                .thenReturn(MOSCOW_RESPONSE);
        WeatherResult weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals("Moscow", weathers.weathers().get(0).name());
        Assertions.assertEquals(new BigDecimal("6.0"), weathers.weathers().get(0).temp());
    }

    @Test
    void givenMultipleLocations_whenApiReturnsValidResponse_thenReturnWeathers() {
        locations = List.of(MOSCOW_LOCATION, LONDON_LOCATION);

        when(weatherClient.fetchWeather(MOSCOW_LOCATION.latitude(), MOSCOW_LOCATION.longitude()))
                .thenReturn(MOSCOW_RESPONSE);
        when(weatherClient.fetchWeather(LONDON_LOCATION.latitude(), LONDON_LOCATION.longitude()))
                .thenReturn(LONDON_RESPONSE);
        WeatherResult weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(2, weathers.weathers().size());

        Assertions.assertEquals(new BigDecimal("6.0"), weathers.weathers().get(0).temp());
        Assertions.assertEquals(new BigDecimal("16.8"), weathers.weathers().get(1).temp());
    }

    @Test
    void givenEmptyLocationList_whenApiReturnsValidResponse_thenReturnEmptyWeatherList() {
        locations = new ArrayList<>();
        WeatherResult weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(0, weathers.weathers().size());
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

        when(weatherClient.fetchWeather(MOSCOW_LOCATION.latitude(), MOSCOW_LOCATION.longitude()))
                .thenReturn(firstResponse, secondResponse, thirdResponse, fourthResponse, MOSCOW_RESPONSE);
        WeatherResult weathers = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(1, weathers.weathers().size());
    }

    @Test
    void givenLocation_whenApiThrowsException_thenAddToFailed() {
        locations = List.of(MOSCOW_LOCATION, LONDON_LOCATION);

        when(weatherClient.fetchWeather(MOSCOW_LOCATION.latitude(), MOSCOW_LOCATION.longitude()))
                .thenReturn(MOSCOW_RESPONSE);
        when(weatherClient.fetchWeather(LONDON_LOCATION.latitude(), LONDON_LOCATION.longitude()))
                .thenThrow(new OpenWeatherApiException("Service unavailable"));

        WeatherResult result = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(1, result.weathers().size());
        Assertions.assertEquals(1, result.failedWeathers().size());
        Assertions.assertEquals("Moscow", result.weathers().get(0).name());
        Assertions.assertEquals("London", result.failedWeathers().get(0));
    }

    @Test
    void givenAllLocationsFail_whenApiThrowsException_thenAllFailed() {
        locations = List.of(MOSCOW_LOCATION, LONDON_LOCATION);

        when(weatherClient.fetchWeather(MOSCOW_LOCATION.latitude(), MOSCOW_LOCATION.longitude()))
                .thenThrow(new OpenWeatherApiException("Bad request"));
        when(weatherClient.fetchWeather(LONDON_LOCATION.latitude(), LONDON_LOCATION.longitude()))
                .thenThrow(new OpenWeatherApiException("Unauthorized"));

        WeatherResult result = weatherService.getWeathersForLocations(locations);

        Assertions.assertEquals(0, result.weathers().size());
        Assertions.assertEquals(2, result.failedWeathers().size());
        Assertions.assertEquals("Moscow", result.failedWeathers().get(0));
        Assertions.assertEquals("London", result.failedWeathers().get(1));
    }
}
