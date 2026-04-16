package org.roadmap.weather.repository.impl;

import org.roadmap.weather.entity.Session;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SessionRepositoryImpl implements SessionRepository {
    private final static String SAVE = """
            INSERT INTO sessions(id, user_id, expires_at)
            VALUES (?, ?, ?)
            """;
    private final static String GET_BY_ID = """
            SELECT id, user_id, expires_at
            FROM sessions
            WHERE id = ?
            """;
    private final static String DELETE_EXPIRED = """
            DELETE
            FROM sessions
            WHERE expires_at < NOW()
            """;
    private final static String DELETE = """
            DELETE
            FROM sessions
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public SessionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Session session) {
        jdbcTemplate.update(
                SAVE,
                session.getId(),
                session.getUserId(),
                session.getExpiresAt()
        );
    }

    @Override
    public Optional<Session> findById(String id) {
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        GET_BY_ID,
                        (rs, rowNum) -> {
                            UUID sessionId = UUID.fromString(rs.getString("id"));
                            Integer userId = rs.getInt("user_id");
                            Timestamp expiresAt = rs.getTimestamp("expires_at");
                            return new Session(sessionId, userId, expiresAt);
                        },
                        id)
        );
    }

    @Override
    public void deleteById(String id) {
        jdbcTemplate.update(DELETE, id);
    }

    @Override
    public void deleteExpiredSessions() {
        jdbcTemplate.update(DELETE_EXPIRED);
    }
}
