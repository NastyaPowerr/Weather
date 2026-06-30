package org.roadmap.weather.dto.internal;

import java.sql.Timestamp;
import java.util.UUID;

public record SessionDto(
        UUID id,
        Integer userId,
        Timestamp expiresAt
) {
}
