package com.analysisroom.platform.system.dto;

public record PermissionResponse(
    String permissionCode,
    String permissionName,
    Long menuId,
    String menuName,
    String menuType,
    Long parentId,
    Integer sortOrder
) {
}
