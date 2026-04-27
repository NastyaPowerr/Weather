package org.roadmap.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    public Location(String name, Integer userId, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
