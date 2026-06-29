package com.analysisroom.platform.system.controller;

import com.analysisroom.platform.common.api.ApiResponse;
import com.analysisroom.platform.common.pagination.PageResult;
import com.analysisroom.platform.system.dto.DeptTreeResponse;
import com.analysisroom.platform.system.dto.PermissionResponse;
import com.analysisroom.platform.system.dto.SystemMenuTreeResponse;
import com.analysisroom.platform.system.dto.SystemRoleDetailResponse;
import com.analysisroom.platform.system.dto.SystemRolePageResponse;
import com.analysisroom.platform.system.dto.SystemRoleQuery;
import com.analysisroom.platform.system.dto.SystemUserDetailResponse;
import com.analysisroom.platform.system.dto.SystemUserPageResponse;
import com.analysisroom.platform.system.dto.SystemUserQuery;
import com.analysisroom.platform.system.service.SystemManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/system")
public class SystemManagementController {

    private final SystemManagementService systemManagementService;

    public SystemManagementController(SystemManagementService systemManagementService) {
        this.systemManagementService = systemManagementService;
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<SystemUserPageResponse>> users(@Valid SystemUserQuery query) {
        return ApiResponse.success(systemManagementService.pageUsers(query));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<SystemUserDetailResponse> user(@PathVariable @Positive Long userId) {
        return ApiResponse.success(systemManagementService.getUser(userId));
    }

    @GetMapping("/roles")
    public ApiResponse<PageResult<SystemRolePageResponse>> roles(@Valid SystemRoleQuery query) {
        return ApiResponse.success(systemManagementService.pageRoles(query));
    }

    @GetMapping("/roles/{roleId}")
    public ApiResponse<SystemRoleDetailResponse> role(@PathVariable @Positive Long roleId) {
        return ApiResponse.success(systemManagementService.getRole(roleId));
    }

    @GetMapping("/depts/tree")
    public ApiResponse<List<DeptTreeResponse>> deptsTree() {
        return ApiResponse.success(systemManagementService.deptTree());
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<SystemMenuTreeResponse>> menusTree() {
        return ApiResponse.success(systemManagementService.menuTree());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.success(systemManagementService.permissions());
    }
}
