package com.analysisroom.platform.auth.dto;

import java.util.List;
import java.util.Set;

public record ProfileResponse(
    Long userId,
    String username,
    String nickname,
    Long deptId,
    String deptName,
    List<RoleResponse> roles,
    Set<String> permissions
) {
}
