package com.analysisroom.platform.common.security;

import com.analysisroom.platform.auth.model.RoleRecord;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class PermissionService {

    private static final String SUPER_ADMIN_ROLE_KEY = "SUPER_ADMIN";

    private final CurrentUserProvider currentUserProvider;
    private final AuthRepository authRepository;

    public PermissionService(CurrentUserProvider currentUserProvider, AuthRepository authRepository) {
        this.currentUserProvider = currentUserProvider;
        this.authRepository = authRepository;
    }

    public void requirePermission(String permissionCode, boolean requireSuperAdmin) {
        Long userId = currentUserProvider.requireLoginUserId();
        if (hasSuperAdminRole(userId)) {
            return;
        }
        if (requireSuperAdmin) {
            throw new ForbiddenException("需要超级管理员权限");
        }
        if (!StringUtils.hasText(permissionCode)) {
            return;
        }
        Set<String> permissions = authRepository.findPermissionCodesByUserId(userId);
        if (!permissions.contains(permissionCode)) {
            throw new ForbiddenException("无权执行该操作");
        }
    }

    public boolean hasSuperAdminRole(Long userId) {
        return authRepository.findRolesByUserId(userId).stream()
            .map(RoleRecord::roleKey)
            .anyMatch(SUPER_ADMIN_ROLE_KEY::equals);
    }
}
