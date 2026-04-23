package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.roadmap.weather.dto.LocationDto;
import org.roadmap.weather.dto.response.LocationResponseDto;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "latitude", source = "lat")
    @Mapping(target = "longitude", source = "lon")
    LocationDto toDto(LocationResponseDto response);
}
