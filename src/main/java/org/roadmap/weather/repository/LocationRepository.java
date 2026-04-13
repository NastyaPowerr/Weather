package org.roadmap.weather.repository;

import org.roadmap.weather.entity.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class LocationRepository {
    private static final String SAVE = """
            INSERT INTO weather.locations(name, user_id, latitude, longitude)
            VALUES (?, ?, ?, ?)
            """;
    private final static String GET_BY_ID = """
            SELECT id, name, user_id, latitude, longitude
            FROM weather.locations
            WHERE user_id = ?
            """;
    private final static String DELETE_BY_ID = """
            DELETE
            FROM weather.locations
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public LocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Location location) {
        jdbcTemplate.update(
                SAVE,
                location.getName(),
                location.getUserId(),
                location.getLatitude(),
                location.getLongitude()
        );
    }

    public List<Location> getByUserId(Integer userId) {
        return jdbcTemplate.query(
                GET_BY_ID,
                (rs, rowNum) -> {
                    Integer id = rs.getInt("id");
                    String name = rs.getString("name");
                    BigDecimal latitude = rs.getBigDecimal("latitude");
                    BigDecimal longitude = rs.getBigDecimal("longitude");
                    return new Location(id,
                            name,
                            userId,
                            latitude,
                            longitude);
                },
                userId
        );
    }

    public void delete(Integer locationId) {
        jdbcTemplate.update(DELETE_BY_ID, locationId);
    }
}
