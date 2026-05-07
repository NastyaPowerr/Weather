package org.roadmap.weather.repository.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.roadmap.weather.entity.User;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.user.UserAlreadyExistsException;
import org.roadmap.weather.repository.AuthRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class AuthRepositoryImpl implements AuthRepository {
    private static final String FIND_BY_LOGIN_HQL = """
            FROM User
            WHERE login = :login
            """;
    private final SessionFactory sessionFactory;

    public AuthRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(User user) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.persist(user);
        } catch (ConstraintViolationException ex) {
            throw new UserAlreadyExistsException(ExceptionMessages.USERNAME_TAKEN);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        Session session = sessionFactory.getCurrentSession();
        User user = session.find(User.class, id);
        return Optional.ofNullable(user);
    }

    @Transactional
    @Override
    public Optional<User> findByLogin(String login) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(FIND_BY_LOGIN_HQL, User.class)
                .setParameter("login", login)
                .uniqueResultOptional();
    }
}
