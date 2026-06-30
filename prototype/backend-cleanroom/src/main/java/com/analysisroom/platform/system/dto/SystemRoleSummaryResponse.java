package com.analysisroom.platform.system.dto;

public record SystemRoleSummaryResponse(
    Long roleId,
    String roleCode,
    String roleName
) {
}
