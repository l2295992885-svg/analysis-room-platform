CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    ancestors VARCHAR(500) NOT NULL DEFAULT '0',
    dept_code VARCHAR(64) NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    dept_type VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
    sort_order INT NOT NULL DEFAULT 0,
    leader_name VARCHAR(64) NULL,
    phone VARCHAR(50) NULL,
    email VARCHAR(100) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dept_code (dept_code),
    KEY idx_sys_dept_parent (parent_id, deleted),
    KEY idx_sys_dept_type_status (dept_type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dept_id BIGINT NULL,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(64) NOT NULL,
    employee_no VARCHAR(64) NULL,
    user_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    email VARCHAR(100) NULL,
    phone VARCHAR(50) NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    last_login_ip VARCHAR(128) NULL,
    last_login_time DATETIME NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_dept_status (dept_id, status, deleted),
    KEY idx_sys_user_employee_no (employee_no, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_name VARCHAR(64) NOT NULL,
    role_key VARCHAR(64) NOT NULL,
    role_type VARCHAR(32) NOT NULL DEFAULT 'BUSINESS',
    data_scope VARCHAR(32) NOT NULL DEFAULT 'SELF',
    sort_order INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_key (role_key),
    KEY idx_sys_role_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    menu_type VARCHAR(32) NOT NULL,
    path VARCHAR(255) NULL,
    component VARCHAR(255) NULL,
    permission_code VARCHAR(255) NULL,
    icon VARCHAR(100) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_code (menu_code),
    KEY idx_sys_menu_parent_type (parent_id, menu_type, status, deleted),
    KEY idx_sys_menu_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role_active (user_id, role_id, deleted),
    KEY idx_sys_user_role_user (user_id, deleted),
    KEY idx_sys_user_role_role (role_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu_active (role_id, menu_id, deleted),
    KEY idx_sys_role_menu_role (role_id, deleted),
    KEY idx_sys_role_menu_menu (menu_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_dept (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    created_by BIGINT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_dept_active (role_id, dept_id, deleted),
    KEY idx_sys_role_dept_role (role_id, deleted),
    KEY idx_sys_role_dept_dept (dept_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    username VARCHAR(64) NULL,
    ip_address VARCHAR(128) NULL,
    login_location VARCHAR(255) NULL,
    browser VARCHAR(100) NULL,
    os VARCHAR(100) NULL,
    login_status VARCHAR(32) NOT NULL,
    message VARCHAR(500) NULL,
    trace_id VARCHAR(128) NULL,
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_sys_login_log_username_time (username, login_time),
    KEY idx_sys_login_log_status_time (login_status, login_time),
    KEY idx_sys_login_log_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    module_title VARCHAR(100) NULL,
    business_type VARCHAR(64) NULL,
    method_name VARCHAR(255) NULL,
    request_method VARCHAR(20) NULL,
    operator_type VARCHAR(64) NULL,
    operator_id BIGINT NULL,
    operator_name VARCHAR(64) NULL,
    dept_id BIGINT NULL,
    dept_name VARCHAR(100) NULL,
    request_url VARCHAR(255) NULL,
    request_ip VARCHAR(128) NULL,
    request_params TEXT NULL,
    response_body TEXT NULL,
    operation_status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT NULL,
    trace_id VARCHAR(128) NULL,
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cost_time_ms BIGINT NOT NULL DEFAULT 0,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_sys_operation_log_operator_time (operator_id, operation_time),
    KEY idx_sys_operation_log_business_time (business_type, operation_time),
    KEY idx_sys_operation_log_status_time (operation_status, operation_time),
    KEY idx_sys_operation_log_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_dept (
    id, parent_id, ancestors, dept_code, dept_name, dept_type, sort_order,
    status, created_by, remark
) VALUES (
    1, 0, '0', 'ROOT_ANALYSIS_ROOM', '综合分析室', 'ANALYSIS_ROOM', 1,
    'ACTIVE', 1, '初始化系统管理部门'
);

INSERT INTO sys_role (
    id, role_name, role_key, role_type, data_scope, sort_order,
    status, created_by, remark
) VALUES (
    1, '超级管理员', 'SUPER_ADMIN', 'SYSTEM', 'ALL', 1,
    'ACTIVE', 1, '开发环境初始化角色，生产环境需重新确认授权'
);

INSERT INTO sys_user (
    id, dept_id, username, display_name, employee_no, user_type, password_hash,
    status, created_by, remark
) VALUES (
    1, 1, 'admin', '系统管理员', 'ADMIN001', 'SYSTEM',
    '$2a$10$000000000000000000000u00000000000000000000000000000000000',
    'ACTIVE', 1, '开发环境占位密码哈希，登录功能实现前必须替换'
);

INSERT INTO sys_menu (
    id, parent_id, menu_name, menu_code, menu_type, path, component,
    permission_code, icon, sort_order, visible, status, created_by, remark
) VALUES
    (1, 0, '首页', 'dashboard', 'MENU', '/dashboard', 'dashboard/index', 'dashboard:view', 'House', 1, 1, 'ACTIVE', 1, '极简功能首页'),
    (2, 0, '系统管理', 'system', 'DIRECTORY', '/system', NULL, NULL, 'Settings', 90, 1, 'ACTIVE', 1, '系统权限基础配置入口'),
    (3, 2, '用户管理', 'system-user', 'MENU', '/system/users', 'system/user/index', 'system:user:list', 'User', 1, 1, 'ACTIVE', 1, '用户管理页面'),
    (4, 2, '角色管理', 'system-role', 'MENU', '/system/roles', 'system/role/index', 'system:role:list', 'Shield', 2, 1, 'ACTIVE', 1, '角色管理页面'),
    (5, 2, '部门管理', 'system-dept', 'MENU', '/system/depts', 'system/dept/index', 'system:dept:list', 'Building2', 3, 1, 'ACTIVE', 1, '部门管理页面'),
    (6, 2, '菜单管理', 'system-menu', 'MENU', '/system/menus', 'system/menu/index', 'system:menu:list', 'Menu', 4, 1, 'ACTIVE', 1, '菜单权限管理页面'),
    (101, 3, '用户查询', 'system-user-query', 'BUTTON', NULL, NULL, 'system:user:query', NULL, 1, 0, 'ACTIVE', 1, '用户详情权限'),
    (102, 3, '用户新增', 'system-user-add', 'BUTTON', NULL, NULL, 'system:user:add', NULL, 2, 0, 'ACTIVE', 1, '用户新增权限'),
    (103, 3, '用户编辑', 'system-user-edit', 'BUTTON', NULL, NULL, 'system:user:edit', NULL, 3, 0, 'ACTIVE', 1, '用户编辑权限'),
    (104, 3, '重置密码', 'system-user-reset-password', 'BUTTON', NULL, NULL, 'system:user:reset-password', NULL, 4, 0, 'ACTIVE', 1, '用户重置密码权限'),
    (201, 4, '角色新增', 'system-role-add', 'BUTTON', NULL, NULL, 'system:role:add', NULL, 1, 0, 'ACTIVE', 1, '角色新增权限'),
    (202, 4, '角色编辑', 'system-role-edit', 'BUTTON', NULL, NULL, 'system:role:edit', NULL, 2, 0, 'ACTIVE', 1, '角色编辑权限'),
    (203, 4, '分配菜单', 'system-role-grant-menu', 'BUTTON', NULL, NULL, 'system:role:grant-menu', NULL, 3, 0, 'ACTIVE', 1, '角色菜单授权权限'),
    (204, 4, '数据权限', 'system-role-data-scope', 'BUTTON', NULL, NULL, 'system:role:data-scope', NULL, 4, 0, 'ACTIVE', 1, '角色数据权限配置权限'),
    (301, 5, '部门新增', 'system-dept-add', 'BUTTON', NULL, NULL, 'system:dept:add', NULL, 1, 0, 'ACTIVE', 1, '部门新增权限'),
    (302, 5, '部门编辑', 'system-dept-edit', 'BUTTON', NULL, NULL, 'system:dept:edit', NULL, 2, 0, 'ACTIVE', 1, '部门编辑权限'),
    (401, 6, '菜单新增', 'system-menu-add', 'BUTTON', NULL, NULL, 'system:menu:add', NULL, 1, 0, 'ACTIVE', 1, '菜单新增权限'),
    (402, 6, '菜单编辑', 'system-menu-edit', 'BUTTON', NULL, NULL, 'system:menu:edit', NULL, 2, 0, 'ACTIVE', 1, '菜单编辑权限');

INSERT INTO sys_user_role (user_id, role_id, created_by, remark)
VALUES (1, 1, 1, '初始化系统管理员用户角色');

INSERT INTO sys_role_dept (role_id, dept_id, created_by, remark)
VALUES (1, 1, 1, '初始化超级管理员数据范围');

INSERT INTO sys_role_menu (role_id, menu_id, created_by, remark)
SELECT 1, id, 1, '初始化超级管理员菜单权限'
FROM sys_menu
WHERE deleted = 0;

