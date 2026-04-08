package org.roadmap.weather.entity;

import java.sql.Timestamp;
import java.util.UUID;

public class Session {
    private UUID id;
    private Integer userId;
    private Timestamp expiresAt;

    public Session(UUID id, Integer userId, Timestamp expiresAt) {
        this.id = id;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }
}
