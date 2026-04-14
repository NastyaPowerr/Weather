package org.roadmap.weather.repository;

import org.roadmap.weather.entity.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.UUID;

@Repository
public class SessionRepository {
    private final static String SAVE = """
            INSERT INTO weather.sessions(id, user_id, expires_at)
            VALUES (?, ?, ?)
            """;
    private final static String GET_BY_ID = """
            SELECT id, user_id, expires_at
            FROM weather.sessions
            WHERE id = ?
            """;
    private final static String DELETE_EXPIRED = """
            DELETE
            FROM weather.sessions
            WHERE expires_at < NOW()
            """;
    private final static String DELETE = """
            DELETE
            FROM weather.sessions
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Session session) {
        jdbcTemplate.update(
                SAVE,
                session.getId(),
                session.getUserId(),
                session.getExpiresAt()
        );
    }

    public Session getById(String sessionId) {
        return jdbcTemplate.queryForObject(
                GET_BY_ID,
                (rs, rowNum) -> {
                    UUID id = UUID.fromString(rs.getString("id"));
                    Integer userId = rs.getInt("user_id");
                    Timestamp expiresAt = rs.getTimestamp("expires_at");
                    return new Session(id, userId, expiresAt);
                },
                sessionId
        );
    }

    public void deleteExpiredSessions() {
        jdbcTemplate.update(DELETE_EXPIRED);
    }

    public void delete(String sessionId) {
        jdbcTemplate.update(DELETE, sessionId);
    }
}
