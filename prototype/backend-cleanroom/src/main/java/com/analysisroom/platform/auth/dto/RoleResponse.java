package com.analysisroom.platform.auth.dto;

public record RoleResponse(
    Long roleId,
    String roleName,
    String roleKey
) {
}
