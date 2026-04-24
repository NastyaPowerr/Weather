package org.roadmap.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "expires_at", nullable = false)
    private Timestamp expiresAt;

    public Session() {
    }

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
