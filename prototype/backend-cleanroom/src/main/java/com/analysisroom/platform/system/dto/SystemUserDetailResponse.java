package com.analysisroom.platform.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SystemUserDetailResponse(
    Long userId,
    String username,
    String nickname,
    String realName,
    Long deptId,
    String deptName,
    String email,
    String phone,
    String status,
    List<SystemRoleSummaryResponse> roles,
    LocalDateTime createdTime,
    LocalDateTime updatedTime
) {
}
