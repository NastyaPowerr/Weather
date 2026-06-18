package org.roadmap.weather;

import jakarta.validation.ConstraintViolationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.roadmap.weather.config.TestConfig;
import org.roadmap.weather.dto.SessionDto;
import org.roadmap.weather.dto.request.UserLoginDto;
import org.roadmap.weather.dto.request.UserRegisterDto;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.user.InvalidUserParamsException;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.service.AuthService;
import org.roadmap.weather.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@TestPropertySource("application-test.properties")
@Transactional
@Rollback
public class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionFactory sessionFactory;

    @Value("${session.duration}")
    private long sessionDuration;

    @Test
    void givenUser_whenRegister_thenShouldBeSavedInDatabase() {
        UserRegisterDto user = new UserRegisterDto("username", "password", "password");

        authService.register(user);
        Session session = sessionFactory.getCurrentSession();
        Optional<User> savedUser = session.createQuery(
                        """
                                FROM User
                                WHERE login = :username
                                """,
                        User.class
                )
                .setParameter("username", user.username())
                .uniqueResultOptional();
        Assertions.assertEquals(user.username(), savedUser.get().getLogin());
    }

    @Test
    void givenNullLoginUser_whenRegister_thenShouldThrowException() {
        UserRegisterDto user = new UserRegisterDto(null, "password", "password");
        Assertions.assertThrows(ConstraintViolationException.class, () -> authService.register(user));
    }

    @Test
    void givenNullPasswordUser_whenRegister_thenShouldThrowException() {
        UserRegisterDto user = new UserRegisterDto("username", null, null);
        Assertions.assertThrows(ConstraintViolationException.class, () -> authService.register(user));
    }

    @Test
    void givenExistedLogin_whenRegister_thenShouldThrowAnException() {
        UserRegisterDto firstUser = new UserRegisterDto("username", "password", "password");
        UserRegisterDto secondUser = new UserRegisterDto("username", "password", "password");

        authService.register(firstUser);
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> authService.register(secondUser));
    }

    // осознанно пошла через рефлексию для учения рефлексии :)
    @Test
    void givenUser_whenRegister_thenPasswordShouldBeHashed() {
        UserRegisterDto user = new UserRegisterDto("username", "password", "password");

        authService.register(user);

        Session session = sessionFactory.getCurrentSession();
        Optional<User> savedUser = session.createQuery(
                        """
                                FROM User
                                WHERE login = :username
                                """,
                        User.class
                )
                .setParameter("username", user.username())
                .uniqueResultOptional();
        Assertions.assertNotEquals(user.password(), savedUser.get().getPassword());

        try {
            Method isPasswordVerified = AuthService.class.getDeclaredMethod(
                    "isPasswordVerified",
                    char[].class,
                    String.class
            );
            isPasswordVerified.setAccessible(true);
            boolean verified = (boolean) isPasswordVerified.invoke(
                    authService,
                    user.password().toCharArray(),
                    savedUser.get().getPassword()
            );
            Assertions.assertTrue(verified);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    @Test
    void givenUser_whenAuthorize_thenShouldGiveSessionAndSaveInDatabase() {
        UserRegisterDto registerUser = new UserRegisterDto("username", "password", "password");
        authService.register(registerUser);

        UserLoginDto loginUser = new UserLoginDto("username", "password");
        SessionDto session = authService.authorize(loginUser);

        Optional<SessionDto> savedSession = sessionService.getSession(session.id());
        Assertions.assertNotNull(session.id());
        Assertions.assertTrue(savedSession.isPresent());
    }

    @Test
    void givenUser_whenAuthorizeWithWrongPassword_thenShouldThrowException() {
        UserRegisterDto registerUser = new UserRegisterDto("username", "password", "password");
        authService.register(registerUser);

        UserLoginDto loginUser = new UserLoginDto("username", "wrong_password");
        Assertions.assertThrows(InvalidUserParamsException.class, () -> authService.authorize(loginUser));
    }

    @Test
    void givenNotSavedUser_whenAuthorize_thenShouldThrowException() {
        UserLoginDto loginUser = new UserLoginDto("username", "password");
        Assertions.assertThrows(InvalidUserParamsException.class, () -> authService.authorize(loginUser));
    }

    @Test
    void givenSession_whenLogout_thenShouldDeleteSession() {
        UserRegisterDto registerUser = new UserRegisterDto("username", "password", "password");
        authService.register(registerUser);

        UserLoginDto loginUser = new UserLoginDto("username", "password");
        SessionDto session = authService.authorize(loginUser);
        Optional<SessionDto> savedSession = sessionService.getSession(session.id());

        Assertions.assertNotNull(session.id());
        Assertions.assertTrue(savedSession.isPresent());

        authService.logout(session.id());
        sessionFactory.getCurrentSession().clear();
        Optional<SessionDto> deletedSession = sessionService.getSession(session.id());

        Assertions.assertFalse(deletedSession.isPresent());
    }

    @Test
    void givenSession_whenSessionExpires_thenSessionIsNotGiven() throws InterruptedException {
        UserRegisterDto registerUser = new UserRegisterDto("username", "password", "password");
        authService.register(registerUser);

        UserLoginDto loginUser = new UserLoginDto("username", "password");
        SessionDto session = authService.authorize(loginUser);

        Optional<SessionDto> savedSession = sessionService.getSession(session.id());
        Assertions.assertNotNull(session.id());
        Assertions.assertTrue(savedSession.isPresent());

        System.out.println(sessionDuration);
        Thread.sleep(sessionDuration + 1);
        Optional<SessionDto> expiredSession = sessionService.getSession(session.id());

        Assertions.assertNotNull(session.id());
        Assertions.assertFalse(expiredSession.isPresent());
    }
}
