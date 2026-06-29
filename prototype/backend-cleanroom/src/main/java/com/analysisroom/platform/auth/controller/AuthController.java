package com.analysisroom.platform.auth.controller;

import com.analysisroom.platform.auth.dto.LoginRequest;
import com.analysisroom.platform.auth.dto.LoginResponse;
import com.analysisroom.platform.auth.dto.LogoutResponse;
import com.analysisroom.platform.auth.dto.MenuResponse;
import com.analysisroom.platform.auth.dto.ProfileResponse;
import com.analysisroom.platform.auth.service.AuthService;
import com.analysisroom.platform.common.api.ApiResponse;
import com.analysisroom.platform.common.log.annotation.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.login(request, servletRequest));
    }

    @PostMapping("/logout")
    @OperationLog(module = "AUTH", action = "LOGOUT", description = "用户退出登录")
    public ApiResponse<LogoutResponse> logout() {
        authService.logout();
        return ApiResponse.success(new LogoutResponse(true));
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.success(authService.profile());
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> menus() {
        return ApiResponse.success(authService.menus());
    }

    @GetMapping("/permissions")
    public ApiResponse<Set<String>> permissions() {
        return ApiResponse.success(authService.permissions());
    }
}
