package org.roadmap.weather.repository;

import org.roadmap.weather.entity.Session;

import java.util.Optional;

public interface SessionRepository extends BaseRepository<Session> {
    Optional<Session> findById(String id);

    void deleteById(String id);

    int deleteExpiredSessions();
}
