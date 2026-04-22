package org.roadmap.weather.repository.impl;

import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.LocationAlreadyExistsForUserException;
import org.roadmap.weather.repository.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class LocationRepositoryImpl implements LocationRepository {
    private static final Logger logger = LoggerFactory.getLogger(LocationRepositoryImpl.class);
    private static final String SAVE = """
            INSERT INTO locations(name, user_id, latitude, longitude)
            VALUES (?, ?, ?, ?)
            """;
    private final static String GET_BY_USER_ID = """
            SELECT id, name, user_id, latitude, longitude
            FROM locations
            WHERE user_id = ?
            """;
    private final static String DELETE_BY_ID = """
            DELETE
            FROM locations
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public LocationRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Location location) {
        long start = System.currentTimeMillis();
        try {
            jdbcTemplate.update(
                    SAVE,
                    location.getName(),
                    location.getUserId(),
                    location.getLatitude(),
                    location.getLongitude()
            );
            long end = System.currentTimeMillis() - start;
            logger.info("user={} saved location={} in {}ms", location.getUserId(), location.getName(), end);
        } catch (DuplicateKeyException ex) {
            long difference = System.currentTimeMillis() - start;
            logger.warn("user={} failed to save location={}, {}ms", location.getUserId(), location.getName(), difference);
            throw new LocationAlreadyExistsForUserException(ExceptionMessages.LOCATION_CONFLICT_FOR_USER);
        }
    }

    @Override
    public List<Location> findByUserId(Integer userId) {
        long start = System.currentTimeMillis();
        List<Location> locations = jdbcTemplate.query(
                GET_BY_USER_ID,
                (rs, rowNum) -> {
                    Integer id = rs.getInt("id");
                    String name = rs.getString("name");
                    BigDecimal latitude = rs.getBigDecimal("latitude");
                    BigDecimal longitude = rs.getBigDecimal("longitude");
                    return new Location(id,
                            name,
                            userId,
                            latitude,
                            longitude
                    );
                },
                userId
        );
        long difference = System.currentTimeMillis() - start;
        logger.debug("found {} locations for user={} in {}ms", locations.size(), userId, difference);
        return locations;
    }

    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }
}
