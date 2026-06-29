-- Analysis room platform phase 4: file center and business attachment binding.
-- Reuse RuoYi sys_oss as file metadata/storage table and keep business binding separated.

CREATE TABLE IF NOT EXISTS biz_file_bind (
    id BIGINT NOT NULL COMMENT '业务附件绑定ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    oss_id BIGINT NOT NULL COMMENT 'OSS文件ID',
    business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    business_action VARCHAR(64) DEFAULT NULL COMMENT '业务动作',
    attachment_type VARCHAR(64) DEFAULT NULL COMMENT '附件类型',
    permission_scope VARCHAR(64) DEFAULT 'BUSINESS' COMMENT '权限范围',
    upload_user_id BIGINT DEFAULT NULL COMMENT '上传人ID',
    upload_user_name VARCHAR(64) DEFAULT NULL COMMENT '上传人名称',
    upload_dept_id BIGINT DEFAULT NULL COMMENT '上传部门ID',
    upload_dept_name VARCHAR(128) DEFAULT NULL COMMENT '上传部门名称',
    original_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    file_suffix VARCHAR(32) DEFAULT NULL COMMENT '文件后缀',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小',
    content_type VARCHAR(128) DEFAULT NULL COMMENT '内容类型',
    status CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_biz_file_bind_oss_id (oss_id),
    KEY idx_biz_file_bind_business (business_type, business_id, del_flag),
    KEY idx_biz_file_bind_action (business_type, business_action, del_flag),
    KEY idx_biz_file_bind_upload_user (upload_user_id, del_flag),
    KEY idx_biz_file_bind_create_time (create_time),
    KEY idx_biz_file_bind_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务附件绑定表';

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 21000, '文件中心', 0, 30, 'file', NULL, '', 1, 0, 'M', '0', '0', '', 'upload', 103, 1, sysdate(), NULL, NULL, '综合分析室平台文件中心'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 21000);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 21001, '文件列表', 21000, 1, 'oss', 'system/oss/index', '', 1, 0, 'C', '0', '0', 'file:center:view', 'document', 103, 1, sysdate(), NULL, NULL, '复用若依OSS文件管理页面'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 21001);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 21002, '业务附件', 21000, 2, 'attachment', 'file/attachment/index', '', 1, 0, 'C', '0', '0', 'file:attachment:list', 'link', 103, 1, sysdate(), NULL, NULL, '业务附件绑定管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 21002);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 21100 AS menu_id, '附件查询' AS menu_name, 21002 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'file:attachment:query' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 21101, '附件上传', 21002, 2, '#', '', '', 1, 0, 'F', '0', '0', 'file:attachment:upload', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 21102, '附件下载', 21002, 3, '#', '', '', 1, 0, 'F', '0', '0', 'file:attachment:download', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 21103, '附件删除', 21002, 4, '#', '', '', 1, 0, 'F', '0', '0', 'file:attachment:remove', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 21104, '文件中心上传', 21001, 1, '#', '', '', 1, 0, 'F', '0', '0', 'file:center:upload', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 21105, '文件中心下载', 21001, 2, '#', '', '', 1, 0, 'F', '0', '0', 'file:center:download', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 21106, '文件中心删除', 21001, 3, '#', '', '', 1, 0, 'F', '0', '0', 'file:center:remove', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 21000 AND 21106
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = 3 AND rm.menu_id = sys_menu.menu_id
  );
