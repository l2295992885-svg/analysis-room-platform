package com.analysisroom.platform.system.dto;

import java.time.LocalDateTime;

public record SystemRolePageResponse(
    Long roleId,
    String roleCode,
    String roleName,
    String status,
    Integer sortOrder,
    String remark,
    LocalDateTime createdTime,
    LocalDateTime updatedTime
) {
}
