package com.analysisroom.platform.health;

import java.time.OffsetDateTime;

public record HealthResponse(
    String status,
    String service,
    OffsetDateTime time
) {
}
