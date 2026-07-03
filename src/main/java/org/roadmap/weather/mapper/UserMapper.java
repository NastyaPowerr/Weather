package org.roadmap.weather.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.roadmap.weather.dto.view.UserDto;
import org.roadmap.weather.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "username", source = "login")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", source = "username")
    @Mapping(target = "password", source = "hashedPassword")
    User toEntity(String username, String hashedPassword);
}
