package com.analysisroom.platform.system.repository;

import com.analysisroom.platform.common.exception.BadRequestException;
import com.analysisroom.platform.common.pagination.PageResult;
import com.analysisroom.platform.system.dto.DeptTreeResponse;
import com.analysisroom.platform.system.dto.PermissionResponse;
import com.analysisroom.platform.system.dto.SystemMenuTreeResponse;
import com.analysisroom.platform.system.dto.SystemRoleDetailResponse;
import com.analysisroom.platform.system.dto.SystemRolePageResponse;
import com.analysisroom.platform.system.dto.SystemRoleQuery;
import com.analysisroom.platform.system.dto.SystemRoleSummaryResponse;
import com.analysisroom.platform.system.dto.SystemUserDetailResponse;
import com.analysisroom.platform.system.dto.SystemUserPageResponse;
import com.analysisroom.platform.system.dto.SystemUserQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SystemManagementRepository {

    private static final Map<String, String> USER_SORT_COLUMNS = Map.of(
        "userId", "u.id",
        "username", "u.username",
        "nickname", "u.display_name",
        "deptId", "u.dept_id",
        "status", "u.status",
        "createdTime", "u.created_time",
        "updatedTime", "u.updated_time"
    );

    private static final Map<String, String> ROLE_SORT_COLUMNS = Map.of(
        "roleId", "r.id",
        "roleCode", "r.role_key",
        "roleName", "r.role_name",
        "status", "r.status",
        "sortOrder", "r.sort_order",
        "createdTime", "r.created_time",
        "updatedTime", "r.updated_time"
    );

    private final JdbcTemplate jdbcTemplate;

    public SystemManagementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean userHasRole(Long userId, String roleKey) {
        String sql = """
            SELECT COUNT(*)
            FROM sys_user u
            JOIN sys_user_role ur ON ur.user_id = u.id AND ur.deleted = 0
            JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE u.id = ?
              AND u.deleted = 0
              AND u.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
              AND r.role_key = ?
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, roleKey);
        return count != null && count > 0;
    }

    public PageResult<SystemUserPageResponse> pageUsers(SystemUserQuery query) {
        List<Object> params = new ArrayList<>();
        String fromWhere = buildUserFromWhere(query, params);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + fromWhere, Long.class, params.toArray());
        if (total == null || total == 0) {
            return PageResult.empty(query.getPageNo(), query.getPageSize());
        }

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(query.getPageSize());
        pageParams.add(query.offset());

        String sql = """
            SELECT u.id, u.username, u.display_name, u.dept_id, d.dept_name,
                   u.status, u.created_time, u.updated_time
            """
            + fromWhere
            + " ORDER BY " + resolveOrderBy(query.getSortBy(), query.getSortOrder(), USER_SORT_COLUMNS, "u.id ASC")
            + " LIMIT ? OFFSET ?";
        List<SystemUserPageResponse> records = jdbcTemplate.query(sql, this::mapUserPage, pageParams.toArray());
        return PageResult.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public Optional<SystemUserDetailResponse> findUserById(Long userId) {
        String sql = """
            SELECT u.id, u.username, u.display_name, u.dept_id, d.dept_name,
                   u.email, u.phone, u.status, u.created_time, u.updated_time
            FROM sys_user u
            LEFT JOIN sys_dept d ON d.id = u.dept_id AND d.deleted = 0
            WHERE u.id = ? AND u.deleted = 0
            """;
        return jdbcTemplate.query(sql, this::mapUserDetail, userId).stream().findFirst();
    }

    public List<SystemRoleSummaryResponse> findRolesByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT r.id, r.role_key, r.role_name, r.sort_order
            FROM sys_role r
            JOIN sys_user_role ur ON ur.role_id = r.id AND ur.deleted = 0
            WHERE ur.user_id = ?
              AND r.deleted = 0
              AND r.status = 'ACTIVE'
            ORDER BY r.sort_order, r.id
            """;
        return jdbcTemplate.query(sql, this::mapRoleSummary, userId);
    }

    public PageResult<SystemRolePageResponse> pageRoles(SystemRoleQuery query) {
        List<Object> params = new ArrayList<>();
        String fromWhere = buildRoleFromWhere(query, params);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + fromWhere, Long.class, params.toArray());
        if (total == null || total == 0) {
            return PageResult.empty(query.getPageNo(), query.getPageSize());
        }

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(query.getPageSize());
        pageParams.add(query.offset());

        String sql = """
            SELECT r.id, r.role_key, r.role_name, r.status, r.sort_order,
                   r.remark, r.created_time, r.updated_time
            """
            + fromWhere
            + " ORDER BY " + resolveOrderBy(query.getSortBy(), query.getSortOrder(), ROLE_SORT_COLUMNS, "r.sort_order ASC, r.id ASC")
            + " LIMIT ? OFFSET ?";
        List<SystemRolePageResponse> records = jdbcTemplate.query(sql, this::mapRolePage, pageParams.toArray());
        return PageResult.of(records, total, query.getPageNo(), query.getPageSize());
    }

    public Optional<SystemRolePageResponse> findRoleById(Long roleId) {
        String sql = """
            SELECT r.id, r.role_key, r.role_name, r.status, r.sort_order,
                   r.remark, r.created_time, r.updated_time
            FROM sys_role r
            WHERE r.id = ? AND r.deleted = 0
            """;
        return jdbcTemplate.query(sql, this::mapRolePage, roleId).stream().findFirst();
    }

    public List<Long> findMenuIdsByRoleId(Long roleId) {
        String sql = """
            SELECT m.id
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
            WHERE rm.role_id = ?
              AND m.deleted = 0
            ORDER BY m.parent_id, m.sort_order, m.id
            """;
        return jdbcTemplate.queryForList(sql, Long.class, roleId);
    }

    public List<String> findPermissionCodesByRoleId(Long roleId) {
        String sql = """
            SELECT DISTINCT m.permission_code
            FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id AND rm.deleted = 0
            WHERE rm.role_id = ?
              AND m.deleted = 0
              AND m.status = 'ACTIVE'
              AND m.permission_code IS NOT NULL
              AND m.permission_code <> ''
            ORDER BY m.permission_code
            """;
        return jdbcTemplate.queryForList(sql, String.class, roleId);
    }

    public List<DeptTreeResponse> findDeptNodes() {
        String sql = """
            SELECT id, parent_id, dept_name, dept_code, sort_order, status
            FROM sys_dept
            WHERE deleted = 0
            ORDER BY parent_id, sort_order, id
            """;
        return jdbcTemplate.query(sql, this::mapDeptNode);
    }

    public List<SystemMenuTreeResponse> findMenuNodes() {
        String sql = """
            SELECT id, parent_id, menu_name, menu_code, menu_type, path, component,
                   permission_code, icon, sort_order, status
            FROM sys_menu
            WHERE deleted = 0
            ORDER BY parent_id, sort_order, id
            """;
        return jdbcTemplate.query(sql, this::mapMenuNode);
    }

    public List<PermissionResponse> findPermissions() {
        String sql = """
            SELECT id, parent_id, menu_name, menu_type, permission_code, sort_order
            FROM sys_menu
            WHERE deleted = 0
              AND status = 'ACTIVE'
              AND permission_code IS NOT NULL
              AND permission_code <> ''
            ORDER BY parent_id, sort_order, id
            """;
        return jdbcTemplate.query(sql, this::mapPermission);
    }

    private String buildUserFromWhere(SystemUserQuery query, List<Object> params) {
        StringBuilder sql = new StringBuilder("""
            FROM sys_user u
            LEFT JOIN sys_dept d ON d.id = u.dept_id AND d.deleted = 0
            WHERE u.deleted = 0
            """);
        appendLike(sql, params, "u.username", query.getUsername());
        appendLike(sql, params, "u.display_name", query.getNickname());
        if (query.getDeptId() != null) {
            sql.append(" AND u.dept_id = ?");
            params.add(query.getDeptId());
        }
        appendEqual(sql, params, "u.status", query.getStatus());
        return sql.toString();
    }

    private String buildRoleFromWhere(SystemRoleQuery query, List<Object> params) {
        StringBuilder sql = new StringBuilder("""
            FROM sys_role r
            WHERE r.deleted = 0
            """);
        appendLike(sql, params, "r.role_key", query.getRoleCode());
        appendLike(sql, params, "r.role_name", query.getRoleName());
        appendEqual(sql, params, "r.status", query.getStatus());
        return sql.toString();
    }

    private void appendLike(StringBuilder sql, List<Object> params, String column, String value) {
        if (StringUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            params.add("%" + value.trim() + "%");
        }
    }

    private void appendEqual(StringBuilder sql, List<Object> params, String column, String value) {
        if (StringUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            params.add(value.trim());
        }
    }

    private String resolveOrderBy(
        String sortBy,
        String sortOrder,
        Map<String, String> whitelist,
        String defaultOrderBy
    ) {
        validateSortOrder(sortOrder);
        if (!StringUtils.hasText(sortBy)) {
            return defaultOrderBy;
        }
        String column = whitelist.get(sortBy.trim());
        if (column == null) {
            throw new BadRequestException("Unsupported sortBy: " + sortBy);
        }
        return column + " " + normalizeSortOrder(sortOrder);
    }

    private void validateSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return;
        }
        normalizeSortOrder(sortOrder);
    }

    private String normalizeSortOrder(String sortOrder) {
        if (!StringUtils.hasText(sortOrder)) {
            return "DESC";
        }
        if ("asc".equalsIgnoreCase(sortOrder)) {
            return "ASC";
        }
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return "DESC";
        }
        throw new BadRequestException("sortOrder must be asc or desc");
    }

    private SystemUserPageResponse mapUserPage(ResultSet rs, int rowNum) throws SQLException {
        return new SystemUserPageResponse(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("display_name"),
            getNullableLong(rs, "dept_id"),
            rs.getString("dept_name"),
            rs.getString("status"),
            getLocalDateTime(rs, "created_time"),
            getLocalDateTime(rs, "updated_time")
        );
    }

    private SystemUserDetailResponse mapUserDetail(ResultSet rs, int rowNum) throws SQLException {
        String displayName = rs.getString("display_name");
        return new SystemUserDetailResponse(
            rs.getLong("id"),
            rs.getString("username"),
            displayName,
            null,
            getNullableLong(rs, "dept_id"),
            rs.getString("dept_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("status"),
            List.of(),
            getLocalDateTime(rs, "created_time"),
            getLocalDateTime(rs, "updated_time")
        );
    }

    private SystemRoleSummaryResponse mapRoleSummary(ResultSet rs, int rowNum) throws SQLException {
        return new SystemRoleSummaryResponse(
            rs.getLong("id"),
            rs.getString("role_key"),
            rs.getString("role_name")
        );
    }

    private SystemRolePageResponse mapRolePage(ResultSet rs, int rowNum) throws SQLException {
        return new SystemRolePageResponse(
            rs.getLong("id"),
            rs.getString("role_key"),
            rs.getString("role_name"),
            rs.getString("status"),
            rs.getInt("sort_order"),
            rs.getString("remark"),
            getLocalDateTime(rs, "created_time"),
            getLocalDateTime(rs, "updated_time")
        );
    }

    private DeptTreeResponse mapDeptNode(ResultSet rs, int rowNum) throws SQLException {
        return DeptTreeResponse.leaf(
            rs.getLong("id"),
            rs.getLong("parent_id"),
            rs.getString("dept_name"),
            rs.getString("dept_code"),
            rs.getInt("sort_order"),
            rs.getString("status")
        );
    }

    private SystemMenuTreeResponse mapMenuNode(ResultSet rs, int rowNum) throws SQLException {
        return SystemMenuTreeResponse.leaf(
            rs.getLong("id"),
            rs.getLong("parent_id"),
            rs.getString("menu_name"),
            rs.getString("menu_code"),
            rs.getString("menu_type"),
            rs.getString("path"),
            rs.getString("component"),
            rs.getString("permission_code"),
            rs.getString("icon"),
            rs.getInt("sort_order"),
            rs.getString("status")
        );
    }

    private PermissionResponse mapPermission(ResultSet rs, int rowNum) throws SQLException {
        String menuName = rs.getString("menu_name");
        return new PermissionResponse(
            rs.getString("permission_code"),
            menuName,
            rs.getLong("id"),
            menuName,
            rs.getString("menu_type"),
            rs.getLong("parent_id"),
            rs.getInt("sort_order")
        );
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
