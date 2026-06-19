package org.roadmap.weather.service;

import lombok.RequiredArgsConstructor;
import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.SessionEntity;
import org.roadmap.weather.mapper.SessionMapper;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Value("${session.duration}")
    private long sessionDuration;


    @Loggable
    @Transactional
    public SessionDto create(Integer userId) {
        UUID sessionId = UUID.randomUUID();
        Timestamp expiredAt = new Timestamp(System.currentTimeMillis() + sessionDuration);
        SessionEntity session = new SessionEntity(sessionId, userId, expiredAt);

        sessionRepository.save(session);
        return sessionMapper.toDto(session);
    }

    @CacheEvict(cacheNames = "sessions", key = "#sessionId")
    @Transactional
    public void deleteSession(UUID sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    @Cacheable(cacheNames = "sessions", key = "#sessionId")
    @Transactional(readOnly = true)
    public Optional<SessionDto> getSession(UUID sessionId) {
        Optional<SessionEntity> session = sessionRepository.findById(sessionId);
        if (session.isPresent()) {
            SessionEntity foundSession = session.get();
            if (!foundSession.isExpired()) {
                return Optional.of(sessionMapper.toDto(foundSession));
            }
        }
        return Optional.empty();
    }

    @Loggable
    @CacheEvict(cacheNames = "sessions", allEntries = true)
    @Transactional
    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }
}
