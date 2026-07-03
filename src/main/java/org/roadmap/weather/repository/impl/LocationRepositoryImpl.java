package org.roadmap.weather.repository.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.DuplicateLocationException;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class LocationRepositoryImpl implements LocationRepository {
    private final static String FIND_BY_USERID_HQL = """
            FROM Location
            WHERE userId = :userId
            """;
    private final static String DELETE_BY_ID_HQL = """
            DELETE
            FROM Location
            WHERE id=:id AND userId = :userId
            """;
    private final SessionFactory sessionFactory;

    public LocationRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Location location) {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.persist(location);
        } catch (ConstraintViolationException ex) {
            throw new DuplicateLocationException(ExceptionMessages.LOCATION_CONFLICT_FOR_USER);
        }
    }

    @Override
    public List<Location> findByUserId(Integer userId) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(FIND_BY_USERID_HQL, Location.class)
                .setParameter("userId", userId)
                .list();
    }

    @Override
    public int deleteByIdAndUserId(Integer id, Integer userId) {
        Session session = sessionFactory.getCurrentSession();
        return session.createMutationQuery(DELETE_BY_ID_HQL)
                .setParameter("id", id)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
