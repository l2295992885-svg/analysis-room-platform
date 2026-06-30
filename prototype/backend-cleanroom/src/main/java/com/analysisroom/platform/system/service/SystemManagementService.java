package com.analysisroom.platform.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.analysisroom.platform.common.exception.ForbiddenException;
import com.analysisroom.platform.common.exception.ResourceNotFoundException;
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
import com.analysisroom.platform.system.repository.SystemManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SystemManagementService {

    private static final String SUPER_ADMIN_ROLE_KEY = "SUPER_ADMIN";

    private final SystemManagementRepository repository;

    public SystemManagementService(SystemManagementRepository repository) {
        this.repository = repository;
    }

    public PageResult<SystemUserPageResponse> pageUsers(SystemUserQuery query) {
        requireSuperAdmin();
        return repository.pageUsers(query);
    }

    public SystemUserDetailResponse getUser(Long userId) {
        requireSuperAdmin();
        SystemUserDetailResponse user = repository.findUserById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return new SystemUserDetailResponse(
            user.userId(),
            user.username(),
            user.nickname(),
            user.realName(),
            user.deptId(),
            user.deptName(),
            user.email(),
            user.phone(),
            user.status(),
            repository.findRolesByUserId(userId),
            user.createdTime(),
            user.updatedTime()
        );
    }

    public PageResult<SystemRolePageResponse> pageRoles(SystemRoleQuery query) {
        requireSuperAdmin();
        return repository.pageRoles(query);
    }

    public SystemRoleDetailResponse getRole(Long roleId) {
        requireSuperAdmin();
        SystemRolePageResponse role = repository.findRoleById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("角色不存在"));
        return new SystemRoleDetailResponse(
            role.roleId(),
            role.roleCode(),
            role.roleName(),
            role.status(),
            role.sortOrder(),
            role.remark(),
            repository.findMenuIdsByRoleId(roleId),
            repository.findPermissionCodesByRoleId(roleId),
            role.createdTime(),
            role.updatedTime()
        );
    }

    public List<DeptTreeResponse> deptTree() {
        requireSuperAdmin();
        return buildDeptTree(repository.findDeptNodes());
    }

    public List<SystemMenuTreeResponse> menuTree() {
        requireSuperAdmin();
        return buildMenuTree(repository.findMenuNodes());
    }

    public List<PermissionResponse> permissions() {
        requireSuperAdmin();
        return repository.findPermissions();
    }

    private void requireSuperAdmin() {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!repository.userHasRole(userId, SUPER_ADMIN_ROLE_KEY)) {
            throw new ForbiddenException("无权访问系统管理接口");
        }
    }

    private List<DeptTreeResponse> buildDeptTree(List<DeptTreeResponse> nodes) {
        Map<Long, DeptTreeResponse> byId = new LinkedHashMap<>();
        List<DeptTreeResponse> roots = new ArrayList<>();
        for (DeptTreeResponse node : nodes) {
            byId.put(node.id(), node);
        }
        for (DeptTreeResponse node : byId.values()) {
            DeptTreeResponse parent = byId.get(node.parentId());
            if (parent == null || node.parentId() == 0) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }

    private List<SystemMenuTreeResponse> buildMenuTree(List<SystemMenuTreeResponse> nodes) {
        Map<Long, SystemMenuTreeResponse> byId = new LinkedHashMap<>();
        List<SystemMenuTreeResponse> roots = new ArrayList<>();
        for (SystemMenuTreeResponse node : nodes) {
            byId.put(node.id(), node);
        }
        for (SystemMenuTreeResponse node : byId.values()) {
            SystemMenuTreeResponse parent = byId.get(node.parentId());
            if (parent == null || node.parentId() == 0) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }
}
