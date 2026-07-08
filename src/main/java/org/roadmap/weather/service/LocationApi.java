package org.roadmap.weather.service;

import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.request.LocationRequestDto;
import org.roadmap.weather.dto.view.UserDto;

import java.util.List;

public interface LocationApi {
    void add(LocationRequestDto locationDto, UserDto user);

    List<LocationDto> findByUserId(Integer userId);

    void delete(String locationId, Integer userId);

    List<LocationDto> findByName(String locationName);

    void evictLocationCache();
}
