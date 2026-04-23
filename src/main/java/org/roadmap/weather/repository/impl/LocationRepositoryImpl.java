package org.roadmap.weather.repository.impl;

import org.roadmap.weather.entity.Location;
import org.roadmap.weather.exception.ExceptionMessages;
import org.roadmap.weather.exception.location.LocationAlreadyExistsForUserException;
import org.roadmap.weather.repository.LocationRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class LocationRepositoryImpl implements LocationRepository {
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
        try {
            jdbcTemplate.update(
                    SAVE,
                    location.getName(),
                    location.getUserId(),
                    location.getLatitude(),
                    location.getLongitude()
            );
        } catch (DuplicateKeyException ex) {
            throw new LocationAlreadyExistsForUserException(ExceptionMessages.LOCATION_CONFLICT_FOR_USER);
        }
    }

    @Override
    public List<Location> findByUserId(Integer userId) {
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
        return locations;
    }

    @Override
    public void deleteById(Integer id) {
        jdbcTemplate.update(DELETE_BY_ID, id);
    }
}
