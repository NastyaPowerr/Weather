package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "login", target = "username")
    UserDto toDto(User user);
}
