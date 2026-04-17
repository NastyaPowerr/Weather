package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ValidationException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Method;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE weather_test.users CASCADE");
    }

    @Test
    void givenUser_whenRegister_thenShouldBeSavedInDatabase() {
        UserDto user = new UserDto("loginA", "password");

        authService.register(user);

        User savedUser = jdbcTemplate.queryForObject(
                """
                        SELECT id, login, password
                        FROM weather_test.users
                        WHERE login = ?
                        """,
                (rs, rowNum) -> {
                    Integer id = rs.getInt("id");
                    String login = rs.getString("login");
                    String password = rs.getString("password");
                    return new User(id, login, password);
                },
                user.login()
        );
        Assertions.assertEquals(user.login(), savedUser.getLogin());
    }

    @Test
    void givenNullLoginUser_whenRegister_thenShouldThrowException() {
        UserDto user = new UserDto(null, "password");
        Assertions.assertThrows(ValidationException.class, () -> authService.register(user));
    }

    @Test
    void givenNullPasswordUser_whenRegister_thenShouldThrowException() {
        UserDto user = new UserDto("login", null);
        Assertions.assertThrows(ValidationException.class, () -> authService.register(user));
    }

    @Test
    void givenExistedLogin_whenRegister_thenShouldThrowAnException() {
        UserDto firstUser = new UserDto("loginA", "password");
        UserDto secondUser = new UserDto("loginA", "password");

        authService.register(firstUser);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> authService.register(secondUser));
    }

    // осознанно пошла через рефлексию
    @Test
    void givenUser_whenRegister_passwordShouldBeHashed() {
        UserDto user = new UserDto("loginA", "password");

        authService.register(user);

        User savedUser = jdbcTemplate.queryForObject(
                """
                        SELECT id, login, password
                        FROM weather_test.users
                        WHERE login = ?
                        """,
                (rs, rowNum) -> {
                    Integer id = rs.getInt("id");
                    String login = rs.getString("login");
                    String password = rs.getString("password");
                    return new User(id, login, password);
                },
                user.login()
        );
        Assertions.assertNotEquals(user.password(), savedUser.getPassword());

        try {
            Method isPasswordVerified = AuthService.class.getDeclaredMethod(
                    "isPasswordVerified",
                    char[].class,
                    String.class
            );
            isPasswordVerified.setAccessible(true);
            boolean verified = (boolean) isPasswordVerified.invoke
                    (authService,
                            user.password().toCharArray(),
                            savedUser.getPassword()
                    );
            Assertions.assertTrue(verified);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}
