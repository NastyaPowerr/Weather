package org.roadmap.weather;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.UserDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
public class AuthTest {
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
                    String password = rs.getString("password");
                    return new User(id, "loginA", password);
                },
                "loginA"
        );
        Assertions.assertEquals(user.login(), savedUser.getLogin());
    }

    @Test
    void givenExistedLogin_whenRegister_thenShouldThrowAnException() {
        UserDto firstUser = new UserDto("loginA", "password");
        UserDto secondUser = new UserDto("loginA", "password");

        authService.register(firstUser);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> authService.register(secondUser));
    }
}
