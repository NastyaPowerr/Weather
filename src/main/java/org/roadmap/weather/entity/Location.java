package org.roadmap.weather.entity;

import java.math.BigDecimal;

public class Location {
    private Integer id;
    private String name;
    private Integer userId;
    private BigDecimal latitude;
    private BigDecimal longitude;

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
