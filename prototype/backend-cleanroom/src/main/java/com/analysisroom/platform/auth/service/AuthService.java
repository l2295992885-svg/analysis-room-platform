package com.analysisroom.platform.auth.service;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.analysisroom.platform.auth.config.AuthProperties;
import com.analysisroom.platform.auth.dto.LoginRequest;
import com.analysisroom.platform.auth.dto.LoginResponse;
import com.analysisroom.platform.auth.dto.MenuResponse;
import com.analysisroom.platform.auth.dto.ProfileResponse;
import com.analysisroom.platform.auth.dto.RoleResponse;
import com.analysisroom.platform.auth.model.AuthUser;
import com.analysisroom.platform.auth.model.MenuRecord;
import com.analysisroom.platform.auth.model.RoleRecord;
import com.analysisroom.platform.auth.repository.AuthRepository;
import com.analysisroom.platform.common.exception.ForbiddenException;
import com.analysisroom.platform.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final LoginLogService loginLogService;

    public AuthService(
        AuthRepository authRepository,
        PasswordEncoder passwordEncoder,
        AuthProperties authProperties,
        LoginLogService loginLogService
    ) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.loginLogService = loginLogService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        String username = request.username().trim();
        String ipAddress = resolveIpAddress(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");

        AuthUser user = authRepository.findUserByUsername(username).orElse(null);
        if (user == null) {
            writeLoginLog(null, username, ipAddress, userAgent, "FAILED", "账号不存在");
            throw new UnauthorizedException("账号不存在");
        }

        if (!STATUS_ACTIVE.equals(user.status())) {
            writeLoginLog(user.id(), username, ipAddress, userAgent, "FAILED", "用户被禁用");
            throw new ForbiddenException("用户被禁用");
        }

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            writeLoginLog(user.id(), username, ipAddress, userAgent, "FAILED", "密码错误");
            throw new UnauthorizedException("密码错误");
        }

        StpUtil.login(user.id(), new SaLoginModel().setTimeout(authProperties.getTokenExpiresIn()));
        authRepository.updateLastLogin(user.id(), ipAddress);
        writeLoginLog(user.id(), username, ipAddress, userAgent, "SUCCESS", "登录成功");

        return new LoginResponse(
            StpUtil.getTokenValue(),
            "Bearer",
            authProperties.getTokenExpiresIn(),
            user.id(),
            user.username(),
            user.displayName()
        );
    }

    public void logout() {
        StpUtil.logout();
    }

    public ProfileResponse profile() {
        AuthUser user = currentUser();
        List<RoleResponse> roles = roles(user.id());
        Set<String> permissions = authRepository.findPermissionCodesByUserId(user.id());
        return new ProfileResponse(
            user.id(),
            user.username(),
            user.displayName(),
            user.deptId(),
            user.deptName(),
            roles,
            permissions
        );
    }

    public List<MenuResponse> menus() {
        Long userId = currentUserId();
        return buildMenuTree(authRepository.findVisibleMenusByUserId(userId));
    }

    public Set<String> permissions() {
        return authRepository.findPermissionCodesByUserId(currentUserId());
    }

    private AuthUser currentUser() {
        Long userId = currentUserId();
        AuthUser user = authRepository.findUserById(userId)
            .orElseThrow(() -> new UnauthorizedException("登录用户不存在"));
        if (!STATUS_ACTIVE.equals(user.status())) {
            throw new ForbiddenException("用户被禁用");
        }
        return user;
    }

    private Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    private List<RoleResponse> roles(Long userId) {
        return authRepository.findRolesByUserId(userId).stream()
            .map(role -> new RoleResponse(role.id(), role.roleName(), role.roleKey()))
            .toList();
    }

    private List<MenuResponse> buildMenuTree(List<MenuRecord> records) {
        Map<Long, MenuResponse> byId = new LinkedHashMap<>();
        List<MenuResponse> roots = new ArrayList<>();

        for (MenuRecord record : records) {
            byId.put(record.id(), MenuResponse.leaf(
                record.id(),
                record.parentId(),
                record.menuName(),
                record.menuCode(),
                record.menuType(),
                record.path(),
                record.component(),
                record.permissionCode(),
                record.icon(),
                record.sortOrder()
            ));
        }

        for (MenuResponse menu : byId.values()) {
            MenuResponse parent = byId.get(menu.parentId());
            if (parent == null || menu.parentId() == 0) {
                roots.add(menu);
            } else {
                parent.children().add(menu);
            }
        }

        return roots;
    }

    private void writeLoginLog(
        Long userId,
        String username,
        String ipAddress,
        String userAgent,
        String status,
        String message
    ) {
        loginLogService.write(userId, username, ipAddress, userAgent, status, message);
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
