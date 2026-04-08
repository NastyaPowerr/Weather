package org.roadmap.weather.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record SessionDto(
        UUID id,
        Integer userId,
        Timestamp expiresAt
) {
}
