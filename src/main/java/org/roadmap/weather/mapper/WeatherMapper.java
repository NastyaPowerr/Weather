package org.roadmap.weather.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.roadmap.weather.dto.Weather;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ExternalApiParseException;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    @Mapping(target = "id", source = "location.id")
    @Mapping(target = "name", source = "location.name")
    @Mapping(target = "temp", source = "responseDto.main.temp")
    @Mapping(target = "tempFeelsLike", source = "responseDto.main.feels_like")
    @Mapping(target = "humidity", source = "responseDto.main.humidity")
    @Mapping(target = "clouds", ignore = true)
    Weather toWeather(Location location, WeatherResponseDto responseDto);

    @BeforeMapping
    default void validateInputs(Location location, WeatherResponseDto responseDto) {
        if (responseDto == null ||
                responseDto.main() == null ||
                responseDto.weather() == null ||
                responseDto.main().temp() == null ||
                responseDto.main().feels_like() == null ||
                responseDto.main().humidity() == null ||
                responseDto.weather().get(0).description() == null
        ) {
            throw new ExternalApiParseException(
                    String.format(
                            ExceptionMessages.MISSING_DATA_FROM_RESPONSE,
                            "weather"
                    )
            );
        }
    }

    @AfterMapping
    default Weather setWeatherClouds(@MappingTarget Weather weather, WeatherResponseDto responseDto) {
        String clouds = responseDto.weather().get(0).description();
        return new Weather(
                weather.id(),
                weather.name(),
                weather.temp(),
                weather.tempFeelsLike(),
                weather.humidity(),
                clouds
        );
    }
}
