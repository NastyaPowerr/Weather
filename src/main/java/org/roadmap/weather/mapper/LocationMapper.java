package org.roadmap.weather.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.response.LocationResponseDto;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.ExternalApiParseException;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "latitude", source = "lat")
    @Mapping(target = "longitude", source = "lon")
    @Mapping(target = "name", expression = "java(getLocalName(response))")
    LocationDto toDto(LocationResponseDto response);

    List<LocationDto> toDtoList(List<Location> locations);

    @BeforeMapping
    default void validateInputs(LocationResponseDto response) {
        if (response == null || response.name() == null || response.lat() == null || response.lon() == null) {
            throw new ExternalApiParseException(
                    String.format(
                            ExceptionMessages.MISSING_DATA_FROM_RESPONSE,
                            "location"
                    )
            );
        }
    }

    default String getLocalName(LocationResponseDto response) {
        Map<String, String> localNames = response.local_names();
        if (localNames != null && localNames.containsKey("ru")) {
            return localNames.get("ru");
        }
        return response.name();
    }
}
