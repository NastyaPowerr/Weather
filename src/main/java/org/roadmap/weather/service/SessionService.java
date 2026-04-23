package org.roadmap.weather.service;

import org.roadmap.weather.aspect.Loggable;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.entity.Session;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Loggable
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
        Optional<Session> session = sessionRepository.findById(sessionId);
        if (session.isPresent()) {
            if (!isExpired(session.get())) {
                return true;
            }
        }
        return false;
    }

    public Optional<Integer> getUserIdFromSession(String sessionId) {
        Optional<Session> session = sessionRepository.findById(sessionId);
        return session.map(Session::getUserId);
    }

    @Loggable
    public void deleteExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
    }

    public void deleteSession(String sessionId) {
        sessionRepository.deleteById(sessionId);
    }

    private boolean isExpired(Session session) {
        return session.getExpiresAt().before(new Timestamp(System.currentTimeMillis()));
    }
}
