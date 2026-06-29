package com.analysisroom.platform.system.dto;

import java.util.ArrayList;
import java.util.List;

public record SystemMenuTreeResponse(
    Long id,
    Long parentId,
    String name,
    String code,
    String type,
    String path,
    String component,
    String permissionCode,
    String icon,
    Integer sortOrder,
    String status,
    List<SystemMenuTreeResponse> children
) {

    public static SystemMenuTreeResponse leaf(
        Long id,
        Long parentId,
        String name,
        String code,
        String type,
        String path,
        String component,
        String permissionCode,
        String icon,
        Integer sortOrder,
        String status
    ) {
        return new SystemMenuTreeResponse(
            id,
            parentId,
            name,
            code,
            type,
            path,
            component,
            permissionCode,
            icon,
            sortOrder,
            status,
            new ArrayList<>()
        );
    }
}
