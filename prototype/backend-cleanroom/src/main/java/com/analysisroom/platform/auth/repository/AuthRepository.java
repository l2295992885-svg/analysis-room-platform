package com.analysisroom.platform.auth.repository;

import com.analysisroom.platform.auth.model.AuthUser;
import com.analysisroom.platform.auth.model.LoginLog;
import com.analysisroom.platform.auth.model.MenuRecord;
import com.analysisroom.platform.auth.model.RoleRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AuthUser> findUserByUsername(String username) {
        String sql = """
            SELECT u.id, u.dept_id, d.dept_name, u.username, u.display_name, u.password_hash, u.status
            FROM sys_user u
            LEFT JOIN sys_dept d ON d.id = u.dept_id AND d.deleted = 0
            WHERE u.username = ? AND u.deleted = 0
            """;
        return jdbcTemplate.query(sql, this::mapUser, username).stream().findFirst();
    }

    public Optional<AuthUser> findUserById(Long userId) {
        String sql = """
            SELECT u.id, u.dept_id, d.dept_name, u.username, u.display_name, u.password_hash, u.status
            FROM sys_user u
            LEFT JOIN sys_dept d ON d.id = u.dept_id AND d.deleted = 0
            WHERE u.id = ? AND u.deleted = 0
            """;
        return jdbcTemplate.query(sql, this::mapUser, userId).stream().findFirst();
    }

    public List<RoleRecord> findRolesByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT r.id, r.role_name, r.role_key, r.sort_order
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id AND ur.deleted = 0
            WHERE ur.user_id = ?
              AND r.deleted = 0
              AND r.status = 'ACTIVE'
            ORDER BY r.sort_order, r.id
            """;
        return jdbcTemplate.query(sql, this::mapRole, userId);
    }

    public Set<String> findPermissionCodesByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT m.permission_code
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
            JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.deleted = 0
            JOIN sys_role r ON r.id = rm.role_id AND r.deleted = 0 AND r.status = 'ACTIVE'
            WHERE ur.user_id = ?
              AND m.deleted = 0
              AND m.status = 'ACTIVE'
              AND m.permission_code IS NOT NULL
              AND m.permission_code <> ''
            ORDER BY m.permission_code
            """;
        return new TreeSet<>(jdbcTemplate.queryForList(sql, String.class, userId));
    }

    public List<MenuRecord> findVisibleMenusByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT m.id, m.parent_id, m.menu_name, m.menu_code, m.menu_type,
                   m.path, m.component, m.permission_code, m.icon, m.sort_order
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
            JOIN sys_user_role ur ON ur.role_id = rm.role_id AND ur.deleted = 0
            JOIN sys_role r ON r.id = rm.role_id AND r.deleted = 0 AND r.status = 'ACTIVE'
            WHERE ur.user_id = ?
              AND m.deleted = 0
              AND m.status = 'ACTIVE'
              AND m.visible = 1
              AND m.menu_type <> 'BUTTON'
            ORDER BY m.parent_id, m.sort_order, m.id
            """;
        return jdbcTemplate.query(sql, this::mapMenu, userId);
    }

    public void updateLastLogin(Long userId, String ipAddress) {
        String sql = """
            UPDATE sys_user
            SET last_login_ip = ?, last_login_time = CURRENT_TIMESTAMP, updated_time = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, ipAddress, userId);
    }

    public void insertLoginLog(LoginLog log) {
        String sql = """
            INSERT INTO sys_login_log (
                user_id, username, ip_address, browser, os, login_status, message, trace_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(
            sql,
            log.userId(),
            log.username(),
            log.ipAddress(),
            log.browser(),
            log.os(),
            log.status(),
            log.message(),
            log.traceId()
        );
    }

    private AuthUser mapUser(ResultSet rs, int rowNum) throws SQLException {
        return new AuthUser(
            rs.getLong("id"),
            getNullableLong(rs, "dept_id"),
            rs.getString("dept_name"),
            rs.getString("username"),
            rs.getString("display_name"),
            rs.getString("password_hash"),
            rs.getString("status")
        );
    }

    private RoleRecord mapRole(ResultSet rs, int rowNum) throws SQLException {
        return new RoleRecord(
            rs.getLong("id"),
            rs.getString("role_name"),
            rs.getString("role_key")
        );
    }

    private MenuRecord mapMenu(ResultSet rs, int rowNum) throws SQLException {
        return new MenuRecord(
            rs.getLong("id"),
            rs.getLong("parent_id"),
            rs.getString("menu_name"),
            rs.getString("menu_code"),
            rs.getString("menu_type"),
            rs.getString("path"),
            rs.getString("component"),
            rs.getString("permission_code"),
            rs.getString("icon"),
            rs.getInt("sort_order")
        );
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}
