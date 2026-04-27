package org.roadmap.weather.repository;

import org.roadmap.weather.entity.SessionEntity;

import java.util.Optional;

public interface SessionRepository extends BaseRepository<SessionEntity> {
    Optional<SessionEntity> findById(String id);

    void deleteById(String id);

    int deleteExpiredSessions();
}
