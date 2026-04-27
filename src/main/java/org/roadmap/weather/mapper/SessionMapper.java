package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.SessionEntity;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toDto(SessionEntity session);

}
