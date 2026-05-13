package org.roadmap.weather.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.WeatherDto;
import org.roadmap.weather.dto.response.WeatherResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ExternalApiParseException;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    @Mapping(target = "id", source = "location.id")
    @Mapping(target = "name", source = "location.name")
    @Mapping(target = "temp", source = "responseDto.main.temp")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    @Mapping(target = "tempFeelsLike", source = "responseDto.main.feels_like")
    @Mapping(target = "humidity", source = "responseDto.main.humidity")
    @Mapping(target = "clouds", ignore = true)
    WeatherDto toWeather(LocationDto location, WeatherResponseDto responseDto);

    @BeforeMapping
    default void validateInputs(LocationDto location, WeatherResponseDto responseDto) {
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
    default WeatherDto setWeatherClouds(@MappingTarget WeatherDto weather, WeatherResponseDto responseDto) {
        String clouds = responseDto.weather().get(0).description();
        return new WeatherDto(
                weather.id(),
                weather.name(),
                weather.latitude(),
                weather.longitude(),
                weather.temp(),
                weather.tempFeelsLike(),
                weather.humidity(),
                clouds
        );
    }
}
