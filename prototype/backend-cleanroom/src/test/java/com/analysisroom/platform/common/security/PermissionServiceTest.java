package com.analysisroom.platform.common.security;

import com.analysisroom.platform.auth.model.RoleRecord;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.exception.ForbiddenException;
import com.analysisroom.platform.common.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void requirePermissionThrowsUnauthorizedWhenNotLoggedIn() {
        when(currentUserProvider.requireLoginUserId()).thenThrow(new UnauthorizedException("未登录或登录已过期"));

        assertThrows(
            UnauthorizedException.class,
            () -> permissionService.requirePermission("system:user:add", false)
        );
    }

    @Test
    void requirePermissionThrowsForbiddenWhenPermissionMissing() {
        when(currentUserProvider.requireLoginUserId()).thenReturn(2L);
        when(authRepository.findRolesByUserId(2L)).thenReturn(List.of());
        when(authRepository.findPermissionCodesByUserId(2L)).thenReturn(Set.of("system:user:list"));

        assertThrows(
            ForbiddenException.class,
            () -> permissionService.requirePermission("system:user:add", false)
        );
    }

    @Test
    void requirePermissionAllowsSuperAdmin() {
        when(currentUserProvider.requireLoginUserId()).thenReturn(1L);
        when(authRepository.findRolesByUserId(1L)).thenReturn(List.of(
            new RoleRecord(1L, "超级管理员", "SUPER_ADMIN")
        ));

        assertDoesNotThrow(() -> permissionService.requirePermission("system:user:add", false));
    }

    @Test
    void requirePermissionAllowsMatchedPermissionCode() {
        when(currentUserProvider.requireLoginUserId()).thenReturn(3L);
        when(authRepository.findRolesByUserId(3L)).thenReturn(List.of(
            new RoleRecord(2L, "测试角色", "TEST_ROLE")
        ));
        when(authRepository.findPermissionCodesByUserId(3L)).thenReturn(Set.of("system:user:add"));

        assertDoesNotThrow(() -> permissionService.requirePermission("system:user:add", false));
    }

    @Test
    void requirePermissionRequiresSuperAdminWhenFlagEnabled() {
        when(currentUserProvider.requireLoginUserId()).thenReturn(3L);
        when(authRepository.findRolesByUserId(3L)).thenReturn(List.of(
            new RoleRecord(2L, "测试角色", "TEST_ROLE")
        ));

        assertThrows(
            ForbiddenException.class,
            () -> permissionService.requirePermission("system:user:add", true)
        );
    }
}
