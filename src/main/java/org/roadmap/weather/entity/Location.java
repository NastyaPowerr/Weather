package org.roadmap.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    private BigDecimal latitude;
    private BigDecimal longitude;

    public Location() {
    }

    public Location(String name, Integer userId, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(Integer id, String name, Integer userId, BigDecimal latitude, BigDecimal longitude) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getUserId() {
        return userId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
