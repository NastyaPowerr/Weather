package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
