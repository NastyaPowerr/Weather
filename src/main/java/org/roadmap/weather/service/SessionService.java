package org.roadmap.weather.service;

import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.Session;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionDto create(Integer userId) {
        UUID sessionId = UUID.randomUUID();

        long twoHoursInMillis = 2 * 60 * 60 * 1000;
        Timestamp expiredAt = new Timestamp(System.currentTimeMillis() + twoHoursInMillis);

        Session session = new Session(sessionId, userId, expiredAt);
        sessionRepository.save(session);

        return new SessionDto(
                sessionId,
                userId,
                expiredAt
        );
    }

    public boolean isSessionValid(String sessionId) {
        // valid = exists in DB and not expired
        Session session = sessionRepository.getById(sessionId);
        if (session != null) {
            if (!isExpired(session)) {
                return true;
            }
        }
        return false;
    }

    public Integer getUserIdFromSession(String sessionId) {
        Session session = sessionRepository.getById(sessionId);
        return session.getUserId();
    }

    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }

    private boolean isExpired(Session session) {
        return session.getExpiresAt().before(new Timestamp(System.currentTimeMillis()));
    }
}
