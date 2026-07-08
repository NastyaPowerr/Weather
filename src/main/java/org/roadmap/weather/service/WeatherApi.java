package org.roadmap.weather.service;

import org.roadmap.weather.dto.internal.LocationDto;
import org.roadmap.weather.dto.view.WeatherResult;

import java.util.List;

public interface WeatherApi {
    WeatherResult getWeathersForUser(Integer userId);

    WeatherResult getWeathersForLocations(List<LocationDto> locations);
}
