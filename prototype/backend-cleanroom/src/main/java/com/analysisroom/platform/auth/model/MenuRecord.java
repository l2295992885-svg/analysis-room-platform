package com.analysisroom.platform.auth.model;

public record MenuRecord(
    Long id,
    Long parentId,
    String menuName,
    String menuCode,
    String menuType,
    String path,
    String component,
    String permissionCode,
    String icon,
    Integer sortOrder
) {
}
