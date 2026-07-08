package org.roadmap.weather.service;

import org.roadmap.weather.dto.internal.SessionDto;

import java.util.Optional;
import java.util.UUID;

public interface SessionApi {
    SessionDto create(Integer userId);

    void deleteSession(UUID sessionId);

    public Optional<SessionDto> getSession(UUID sessionId);

    void deleteExpiredSessions();
}
