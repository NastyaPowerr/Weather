package org.roadmap.weather.repository;

import org.roadmap.weather.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final String SAVE = """
            INSERT INTO weather.users(login, password)
            VALUES (?, ?)
            """;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User userToSave) {
        jdbcTemplate.update(SAVE,
                userToSave.getLogin(),
                userToSave.getPassword()
        );
    }
}
