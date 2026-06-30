package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.roadmap.weather.dto.internal.SessionDto;
import org.roadmap.weather.entity.SessionEntity;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toDto(SessionEntity session);
}
