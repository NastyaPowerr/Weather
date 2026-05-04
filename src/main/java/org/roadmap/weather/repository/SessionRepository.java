package org.roadmap.weather.repository;

import org.roadmap.weather.entity.SessionEntity;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends BaseRepository<SessionEntity> {
    Optional<SessionEntity> findById(UUID id);

    void deleteById(UUID id);

    int deleteExpiredSessions();
}
