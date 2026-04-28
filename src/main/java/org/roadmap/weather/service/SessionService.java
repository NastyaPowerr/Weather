package org.roadmap.weather.service;

import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.SessionEntity;
import org.roadmap.weather.mapper.SessionMapper;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Value("${session.duration}")
    private long sessionDuration;

    public SessionService(SessionRepository sessionRepository, SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.sessionMapper = sessionMapper;
    }

    @Loggable
    public SessionDto create(Integer userId) {
        UUID sessionId = UUID.randomUUID();

        Timestamp expiredAt = new Timestamp(System.currentTimeMillis() + sessionDuration);

        SessionEntity session = new SessionEntity(sessionId, userId, expiredAt);

        sessionRepository.save(session);
        return sessionMapper.toDto(session);
    }

    @Loggable
    @CacheEvict(cacheNames = "sessions", allEntries = true)
    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }

    @CacheEvict(cacheNames = "sessions", key = "#sessionId")
    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    @Cacheable(cacheNames = "sessions", key = "#sessionId")
    public Optional<SessionDto> getSession(String sessionId) {
        Optional<SessionEntity> session = sessionRepository.findById(sessionId);
        if (session.isPresent()) {
            SessionEntity foundSession = session.get();
            if (!isExpired(foundSession)) {
                return Optional.of(sessionMapper.toDto(foundSession));
            }
        }
        return Optional.empty();
    }

    private boolean isExpired(SessionEntity session) {
        return session.getExpiresAt().before(new Timestamp(System.currentTimeMillis()));
    }
}
