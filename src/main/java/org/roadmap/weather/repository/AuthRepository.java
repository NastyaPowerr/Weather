package org.roadmap.weather.repository;

import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private static final String SAVE = """
            INSERT INTO weather.users(login, password)
            VALUES (?, ?)
            """;
    private static final String GET_USER = """
            SELECT id, login, password
            FROM weather.users
            WHERE login = ?
            """;
    private static final String GET_USER_BY_ID = """
            SELECT login, password
            FROM weather.users
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User userToSave) {
        try {
            jdbcTemplate.update(
                    SAVE,
                    userToSave.getLogin(),
                    userToSave.getPassword()
            );
        } catch (DuplicateKeyException ex) {
            throw new UserAlreadyExistsException(ExceptionMessages.USERNAME_TAKEN);
        }
    }

    public User getUser(String login) {
        try {
            return jdbcTemplate.queryForObject(
                    GET_USER,
                    (rs, rowNum) -> {
                        Integer id = rs.getInt("id");
                        String password = rs.getString("password");
                        return new User(id, login, password);
                    },
                    login
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new InvalidUserParamsException(ExceptionMessages.INVALID_USER_PARAMS);
        }
    }

    public User getUserById(Integer userId) {
        return jdbcTemplate.queryForObject(
                GET_USER_BY_ID,
                (rs, rowNum) -> {
                    String login = rs.getString("login");
                    String password = rs.getString("password");
                    return new User(userId, login, password);
                },
                userId
        );
    }
}
