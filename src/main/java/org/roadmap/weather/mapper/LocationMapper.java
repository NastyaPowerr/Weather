package org.roadmap.weather.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.response.LocationResponseDto;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ExternalApiParseException;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "latitude", source = "lat")
    @Mapping(target = "longitude", source = "lon")
    LocationDto toDto(LocationResponseDto response);

    @BeforeMapping
    default void validateInputs(LocationResponseDto response) {
        if (response == null || response.name() == null || response.lat() == null || response.lon() == null) {
            throw new ExternalApiParseException(
                    String.format(
                            ExceptionMessages.MISSING_DATA_FROM_RESPONSE,
                            "location")
            );
        }
    }
}
