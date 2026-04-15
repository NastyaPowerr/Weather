package org.roadmap.weather.repository.impl;

import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthRepositoryImpl implements AuthRepository {
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

    public AuthRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(User user) {
        try {
            jdbcTemplate.update(
                    SAVE,
                    user.getLogin(),
                    user.getPassword()
            );
        } catch (DuplicateKeyException ex) {
            throw new UserAlreadyExistsException(ExceptionMessages.USERNAME_TAKEN);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    GET_USER_BY_ID,
                    (rs, rowNum) -> {
                        String login = rs.getString("login");
                        String password = rs.getString("password");
                        return new User(id, login, password);
                    },
                    id
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try {
            User user = jdbcTemplate.queryForObject(
                    GET_USER,
                    (rs, rowNum) -> {
                        Integer id = rs.getInt("id");
                        String password = rs.getString("password");
                        return new User(id, login, password);
                    },
                    login
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}
