package com.analysisroom.platform.system.dto;

import java.util.ArrayList;
import java.util.List;

public record DeptTreeResponse(
    Long id,
    Long parentId,
    String deptName,
    String deptCode,
    Integer sortOrder,
    String status,
    List<DeptTreeResponse> children
) {

    public static DeptTreeResponse leaf(
        Long id,
        Long parentId,
        String deptName,
        String deptCode,
        Integer sortOrder,
        String status
    ) {
        return new DeptTreeResponse(id, parentId, deptName, deptCode, sortOrder, status, new ArrayList<>());
    }
}
