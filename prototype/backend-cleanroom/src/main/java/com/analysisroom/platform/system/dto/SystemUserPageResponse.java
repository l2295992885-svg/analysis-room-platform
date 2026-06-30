package com.analysisroom.platform.system.dto;

import java.time.LocalDateTime;

public record SystemUserPageResponse(
    Long userId,
    String username,
    String nickname,
    Long deptId,
    String deptName,
    String status,
    LocalDateTime createdTime,
    LocalDateTime updatedTime
) {
}
