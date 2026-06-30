package com.analysisroom.platform.health;

import com.analysisroom.platform.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<HealthResponse> health() {
        return ApiResponse.success(new HealthResponse(
            "UP",
            "analysis-room-platform-backend",
            OffsetDateTime.now()
        ));
    }
}
