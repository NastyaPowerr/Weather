package org.roadmap.weather.repository.impl;

import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.repository.AuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthRepositoryImpl implements AuthRepository {
    private static final Logger logger = LoggerFactory.getLogger(AuthRepositoryImpl.class);
    private static final String SAVE = """
            INSERT INTO users(login, password)
            VALUES (?, ?)
            """;
    private static final String GET_USER = """
            SELECT id, login, password
            FROM users
            WHERE login = ?
            """;
    private static final String GET_USER_BY_ID = """
            SELECT login, password
            FROM users
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
            logger.info("Save new user: login={} password={}", user.getLogin(), user.getPassword());
        } catch (DuplicateKeyException ex) {
            logger.debug("Failed to save user: user with that username exists");
            throw new UserAlreadyExistsException(ExceptionMessages.USERNAME_TAKEN);
        } catch (DataIntegrityViolationException ex) {
            logger.debug("Failed to save user: wrong login or password");
            throw new InvalidUserParamsException(ExceptionMessages.USER_PARAMS_ARE_NULL);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        try {
            long start = System.currentTimeMillis();
            User user = jdbcTemplate.queryForObject(
                    GET_USER_BY_ID,
                    (rs, rowNum) -> {
                        String login = rs.getString("login");
                        String password = rs.getString("password");
                        return new User(id, login, password);
                    },
                    id
            );
            long difference = System.currentTimeMillis() - start;
            logger.debug("found user={} in {}ms", id, difference);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("did not found user={}", id);
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
