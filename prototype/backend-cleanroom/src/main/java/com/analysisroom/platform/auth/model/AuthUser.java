package com.analysisroom.platform.auth.model;

public record AuthUser(
    Long id,
    Long deptId,
    String deptName,
    String username,
    String displayName,
    String passwordHash,
    String status
) {
}
