package com.analysisroom.platform.system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SystemRoleDetailResponse(
    Long roleId,
    String roleCode,
    String roleName,
    String status,
    Integer sortOrder,
    String remark,
    List<Long> menuIds,
    List<String> permissionCodes,
    LocalDateTime createdTime,
    LocalDateTime updatedTime
) {
}
