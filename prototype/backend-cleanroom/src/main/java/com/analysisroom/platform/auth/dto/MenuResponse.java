package com.analysisroom.platform.auth.dto;

import java.util.ArrayList;
import java.util.List;

public record MenuResponse(
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
    List<MenuResponse> children
) {

    public static MenuResponse leaf(
        Long id,
        Long parentId,
        String name,
        String code,
        String type,
        String path,
        String component,
        String permissionCode,
        String icon,
        Integer sortOrder
    ) {
        return new MenuResponse(
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
            new ArrayList<>()
        );
    }
}
