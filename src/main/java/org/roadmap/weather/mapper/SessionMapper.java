package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.Session;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    SessionDto toDto(Session session);

}
