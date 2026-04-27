package org.roadmap.weather.repository.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.roadmap.weather.entity.SessionEntity;
import org.roadmap.weather.repository.SessionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class SessionRepositoryImpl implements SessionRepository {
    private final static String DELETE_BY_ID_HQL = """
            DELETE
            FROM SessionEntity
            WHERE id=:id
            """;
    private final static String DELETE_EXPIRED_HQL = """
            DELETE
            FROM SessionEntity
            WHERE expiresAt < CURRENT_TIMESTAMP
            """;
    private final SessionFactory sessionFactory;

    public SessionRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(SessionEntity sessionEntity) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(sessionEntity);
        session.flush();
    }

    @Override
    public Optional<SessionEntity> findById(String id) {
        Session session = sessionFactory.getCurrentSession();
        SessionEntity sessionEntity = session.find(SessionEntity.class, UUID.fromString(id));
        return Optional.ofNullable(sessionEntity);
    }

    @Override
    public void deleteById(String id) {
        Session session = sessionFactory.getCurrentSession();
        session.createMutationQuery(DELETE_BY_ID_HQL)
                .setParameter("id", UUID.fromString(id))
                .executeUpdate();
    }

    @Override
    public int deleteExpiredSessions() {
        Session session = sessionFactory.getCurrentSession();
        return session.createMutationQuery(DELETE_EXPIRED_HQL)
                .executeUpdate();
    }
}
