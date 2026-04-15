package org.roadmap.weather.repository;

import org.roadmap.weather.entity.Location;

import java.util.List;

public interface LocationRepository extends BaseRepository<Location> {
    List<Location> findByUserId(Integer userId);

    void deleteById(Integer id);
}
