package org.roadmap.weather.repository;

public interface BaseRepository<T> {
    void save(T entity);
}
