-- 001_system_tables.sql
-- 系统权限基础表草案，第一版参考若依用户、角色、部门、菜单、按钮、数据权限思路。
-- 数据库：MySQL 8.x

CREATE TABLE IF NOT EXISTS sys_dept (
  dept_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
  ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
  dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
  dept_type VARCHAR(30) DEFAULT NULL COMMENT '部门类型：analysis_room/workshop/team/group',
  order_num INT DEFAULT 0 COMMENT '显示顺序',
  leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  phone VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='部门/组织表';

CREATE TABLE IF NOT EXISTS sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  dept_id BIGINT DEFAULT NULL COMMENT '所属部门ID',
  user_name VARCHAR(64) NOT NULL COMMENT '登录账号',
  nick_name VARCHAR(64) NOT NULL COMMENT '用户姓名',
  employee_no VARCHAR(64) DEFAULT NULL COMMENT '工号',
  user_type VARCHAR(20) DEFAULT 'system' COMMENT '用户类型',
  email VARCHAR(100) DEFAULT '',
  phone VARCHAR(50) DEFAULT '',
  sex CHAR(1) DEFAULT '2' COMMENT '性别',
  avatar VARCHAR(255) DEFAULT '',
  password VARCHAR(255) NOT NULL COMMENT '密码哈希',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  login_ip VARCHAR(128) DEFAULT '',
  login_date DATETIME DEFAULT NULL,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_user_user_name (user_name),
  KEY idx_sys_user_dept_id (dept_id),
  KEY idx_sys_user_employee_no (employee_no)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
  role_key VARCHAR(64) NOT NULL COMMENT '角色权限字符串',
  role_sort INT DEFAULT 0 COMMENT '显示顺序',
  data_scope CHAR(1) DEFAULT '1' COMMENT '数据范围：1全部 2自定义 3本部门 4本部门及以下 5仅本人',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_role_key (role_key)
) COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_menu (
  menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
  menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  order_num INT DEFAULT 0 COMMENT '显示顺序',
  path VARCHAR(255) DEFAULT '' COMMENT '路由地址',
  component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  query_param VARCHAR(255) DEFAULT NULL COMMENT '路由参数',
  is_frame CHAR(1) DEFAULT '1' COMMENT '是否外链',
  is_cache CHAR(1) DEFAULT '0' COMMENT '是否缓存',
  menu_type CHAR(1) NOT NULL COMMENT '菜单类型：M目录 C菜单 F按钮',
  visible CHAR(1) DEFAULT '0' COMMENT '显示状态：0显示 1隐藏',
  status CHAR(1) DEFAULT '0' COMMENT '菜单状态：0正常 1停用',
  perms VARCHAR(255) DEFAULT NULL COMMENT '权限标识',
  icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_sys_menu_parent_id (parent_id)
) COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
) COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_role_dept (
  role_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, dept_id)
) COMMENT='角色部门数据权限关联表';

CREATE TABLE IF NOT EXISTS sys_login_log (
  info_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(64) DEFAULT '',
  ipaddr VARCHAR(128) DEFAULT '',
  login_location VARCHAR(255) DEFAULT '',
  browser VARCHAR(100) DEFAULT '',
  os VARCHAR(100) DEFAULT '',
  status CHAR(1) DEFAULT '0',
  msg VARCHAR(255) DEFAULT '',
  login_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS sys_oper_log (
  oper_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) DEFAULT '' COMMENT '模块标题',
  business_type INT DEFAULT 0 COMMENT '业务类型',
  method VARCHAR(255) DEFAULT '' COMMENT '方法名称',
  request_method VARCHAR(20) DEFAULT '' COMMENT '请求方式',
  operator_type INT DEFAULT 0 COMMENT '操作类别',
  oper_name VARCHAR(64) DEFAULT '' COMMENT '操作人员',
  dept_name VARCHAR(100) DEFAULT '' COMMENT '部门名称',
  oper_url VARCHAR(255) DEFAULT '' COMMENT '请求URL',
  oper_ip VARCHAR(128) DEFAULT '' COMMENT '主机地址',
  oper_param TEXT COMMENT '请求参数',
  json_result TEXT COMMENT '返回参数',
  status CHAR(1) DEFAULT '0' COMMENT '操作状态',
  error_msg TEXT COMMENT '错误消息',
  oper_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  cost_time BIGINT DEFAULT 0 COMMENT '消耗时间'
) COMMENT='系统操作日志表';
