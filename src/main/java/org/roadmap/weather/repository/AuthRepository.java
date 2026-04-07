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
    private static final String GET_USER = """
            SELECT id, login, password
            FROM weather.users
            WHERE login = ?
            """;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User userToSave) {
        jdbcTemplate.update(
                SAVE,
                userToSave.getLogin(),
                userToSave.getPassword()
        );
    }

    public User getUser(String login) {
        return jdbcTemplate.queryForObject(
                GET_USER,
                (rs, rowNum) -> {
                    Integer id = rs.getInt("id");
                    String password = rs.getString("password");
                    return new User(id, login, password);
                },
                login
        );
    }
}
