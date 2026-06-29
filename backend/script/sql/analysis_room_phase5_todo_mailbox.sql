-- Analysis room platform phase 5: todo center and mailbox center.
-- Keep tasks and formal flow messages as platform common business abilities.

CREATE TABLE IF NOT EXISTS biz_task (
    id BIGINT NOT NULL COMMENT '待办ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    batch_id VARCHAR(64) DEFAULT NULL COMMENT '批次ID',
    task_title VARCHAR(200) NOT NULL COMMENT '待办标题',
    task_content TEXT COMMENT '待办内容',
    task_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '待办状态：PENDING/DONE/CLOSED/CANCELLED',
    current_node VARCHAR(64) DEFAULT NULL COMMENT '当前节点',
    priority VARCHAR(32) DEFAULT 'NORMAL' COMMENT '优先级：LOW/NORMAL/HIGH/URGENT',
    business_url VARCHAR(500) DEFAULT NULL COMMENT '业务跳转地址',
    receiver_user_id BIGINT DEFAULT NULL COMMENT '接收人ID',
    receiver_user_name VARCHAR(64) DEFAULT NULL COMMENT '接收人名称',
    receiver_dept_id BIGINT DEFAULT NULL COMMENT '接收部门ID',
    receiver_dept_name VARCHAR(128) DEFAULT NULL COMMENT '接收部门名称',
    receiver_role_key VARCHAR(64) DEFAULT NULL COMMENT '接收角色权限标识',
    sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
    sender_user_name VARCHAR(64) DEFAULT NULL COMMENT '发送人名称',
    sender_dept_id BIGINT DEFAULT NULL COMMENT '发送部门ID',
    sender_dept_name VARCHAR(128) DEFAULT NULL COMMENT '发送部门名称',
    due_time DATETIME DEFAULT NULL COMMENT '截止时间',
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    finish_user_id BIGINT DEFAULT NULL COMMENT '完成人ID',
    finish_user_name VARCHAR(64) DEFAULT NULL COMMENT '完成人名称',
    finish_comment VARCHAR(500) DEFAULT NULL COMMENT '完成或关闭说明',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_biz_task_receiver_user (receiver_user_id, task_status, del_flag),
    KEY idx_biz_task_receiver_dept (receiver_dept_id, task_status, del_flag),
    KEY idx_biz_task_receiver_role (receiver_role_key, task_status, del_flag),
    KEY idx_biz_task_business (business_type, business_id, task_status, del_flag),
    KEY idx_biz_task_finish_user (finish_user_id, task_status, del_flag),
    KEY idx_biz_task_create_time (create_time),
    KEY idx_biz_task_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一待办任务表';

CREATE TABLE IF NOT EXISTS biz_message (
    id BIGINT NOT NULL COMMENT '信箱消息ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    business_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    batch_id VARCHAR(64) DEFAULT NULL COMMENT '批次ID',
    message_type VARCHAR(64) NOT NULL COMMENT '消息类型',
    message_title VARCHAR(200) NOT NULL COMMENT '消息标题',
    message_content TEXT COMMENT '消息内容',
    source_action VARCHAR(64) DEFAULT NULL COMMENT '来源动作',
    business_url VARCHAR(500) DEFAULT NULL COMMENT '业务跳转地址',
    business_payload TEXT COMMENT '业务卡片JSON快照',
    sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
    sender_user_name VARCHAR(64) DEFAULT NULL COMMENT '发送人名称',
    sender_dept_id BIGINT DEFAULT NULL COMMENT '发送部门ID',
    sender_dept_name VARCHAR(128) DEFAULT NULL COMMENT '发送部门名称',
    receiver_user_id BIGINT DEFAULT NULL COMMENT '接收人ID',
    receiver_user_name VARCHAR(64) DEFAULT NULL COMMENT '接收人名称',
    receiver_dept_id BIGINT DEFAULT NULL COMMENT '接收部门ID',
    receiver_dept_name VARCHAR(128) DEFAULT NULL COMMENT '接收部门名称',
    receiver_role_key VARCHAR(64) DEFAULT NULL COMMENT '接收角色权限标识',
    read_flag CHAR(1) DEFAULT '0' COMMENT '已读标志（0未读 1已读）',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    archive_flag CHAR(1) DEFAULT '0' COMMENT '归档标志（0未归档 1已归档）',
    archive_time DATETIME DEFAULT NULL COMMENT '归档时间',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_biz_message_receiver_user (receiver_user_id, read_flag, archive_flag, del_flag),
    KEY idx_biz_message_receiver_dept (receiver_dept_id, read_flag, archive_flag, del_flag),
    KEY idx_biz_message_receiver_role (receiver_role_key, read_flag, archive_flag, del_flag),
    KEY idx_biz_message_business (business_type, business_id, del_flag),
    KEY idx_biz_message_create_time (create_time),
    KEY idx_biz_message_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一信箱消息表';

INSERT IGNORE INTO biz_task
    (id, tenant_id, business_type, business_id, batch_id, task_title, task_content, task_status, current_node, priority, business_url,
     receiver_user_id, receiver_user_name, receiver_dept_id, receiver_dept_name, receiver_role_key,
     sender_user_id, sender_user_name, sender_dept_id, sender_dept_name, due_time, finish_time, finish_user_id, finish_user_name,
     finish_comment, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (4000000000000000001, '000000', 'DAILY_LKJ_VIOLATION', 'DEMO-001', NULL, '示例：班长待审核 LKJ 公示记录',
     '阶段5本地验收待办：用于验证待办中心列表、详情和打开业务入口。', 'PENDING', 'LEADER_AUDIT', 'NORMAL', '/index',
     1, 'admin', 103, '研发部门', 'superadmin',
     1, 'admin', 103, '研发部门', DATE_ADD(sysdate(), INTERVAL 2 DAY), NULL, NULL, NULL,
     NULL, '0', '阶段5本地示例待办，不代表正式业务数据', 103, 1, sysdate(), NULL, NULL),
    (4000000000000000002, '000000', 'DAILY_LKJ_VIOLATION', 'DEMO-002', NULL, '示例：已处理 LKJ 公示记录',
     '阶段5本地验收已处理任务。', 'DONE', 'DIRECTOR_CONFIRM', 'LOW', '/index',
     1, 'admin', 103, '研发部门', 'superadmin',
     1, 'admin', 103, '研发部门', NULL, sysdate(), 1, 'admin',
     '阶段5本地示例已处理', '0', '阶段5本地示例待办，不代表正式业务数据', 103, 1, sysdate(), 1, sysdate()),
    (4000000000000000003, '000000', 'DAILY_LKJ_VIOLATION', 'DEMO-CLOSE', NULL, '示例：关闭动作验收待办',
     '阶段5本地验收待办：用于验证关闭待办动作，不代表正式业务流程完成。', 'PENDING', 'LOCAL_SMOKE_TEST', 'LOW', '/index',
     1, 'admin', 103, '研发部门', 'superadmin',
     1, 'admin', 103, '研发部门', DATE_ADD(sysdate(), INTERVAL 1 DAY), NULL, NULL, NULL,
     '阶段5本地示例已处理', '0', '阶段5本地示例待办，不代表正式业务数据', 103, 1, sysdate(), 1, sysdate());

INSERT IGNORE INTO biz_message
    (id, tenant_id, business_type, business_id, batch_id, message_type, message_title, message_content, source_action, business_url, business_payload,
     sender_user_id, sender_user_name, sender_dept_id, sender_dept_name,
     receiver_user_id, receiver_user_name, receiver_dept_id, receiver_dept_name, receiver_role_key,
     read_flag, read_time, archive_flag, archive_time, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (4000000000000000101, '000000', 'DAILY_LKJ_VIOLATION', 'DEMO-001', NULL, 'SUBMIT', '示例：收到 LKJ 公示审核消息',
     '阶段5本地验收信箱消息：正式业务流转消息入口。', 'SUBMIT_TO_LEADER', '/index',
     '{"businessType":"DAILY_LKJ_VIOLATION","businessId":"DEMO-001"}',
     1, 'admin', 103, '研发部门', 1, 'admin', 103, '研发部门', 'superadmin',
     '0', NULL, '0', NULL, '0', '阶段5本地示例信箱，不代表正式业务数据', 103, 1, sysdate(), NULL, NULL),
    (4000000000000000102, '000000', 'DAILY_LKJ_VIOLATION', 'DEMO-002', NULL, 'DONE', '示例：LKJ 公示已处理消息',
     '阶段5本地验收已读信箱消息。', 'DIRECTOR_CONFIRM', '/index',
     '{"businessType":"DAILY_LKJ_VIOLATION","businessId":"DEMO-002"}',
     1, 'admin', 103, '研发部门', 1, 'admin', 103, '研发部门', 'superadmin',
     '1', sysdate(), '0', NULL, '0', '阶段5本地示例信箱，不代表正式业务数据', 103, 1, sysdate(), 1, sysdate());

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22000, '信箱中心', 0, 10, 'mailbox', NULL, '', 1, 0, 'M', '0', '0', '', 'message', 103, 1, sysdate(), NULL, NULL, '综合分析室平台信箱中心'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22000);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22001, '收件箱', 22000, 1, 'messages', 'mailbox/index', '', 1, 0, 'C', '0', '0', 'mailbox:view', 'email', 103, 1, sysdate(), NULL, NULL, '正式业务流转消息'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22001);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 22100 AS menu_id, '信箱标记已读' AS menu_name, 22001 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'mailbox:view' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 22101, '信箱归档', 22001, 2, '#', '', '', 1, 0, 'F', '0', '0', 'mailbox:view', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22200, '待办中心', 0, 11, 'todo', NULL, '', 1, 0, 'M', '0', '0', '', 'waiting', 103, 1, sysdate(), NULL, NULL, '综合分析室平台待办中心'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22200);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22201, '我的待办', 22200, 1, 'my', 'todo/index', '', 1, 0, 'C', '0', '0', 'todo:view', 'waiting', 103, 1, sysdate(), NULL, NULL, '当前必须处理的业务任务'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22201);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 22300 AS menu_id, '打开待办' AS menu_name, 22201 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'todo:view' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 22301, '关闭待办', 22201, 2, '#', '', '', 1, 0, 'F', '0', '0', 'todo:close', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (SELECT 1 AS role_id UNION ALL SELECT 3 AS role_id) roles
JOIN sys_menu menus ON menus.menu_id BETWEEN 22000 AND 22301
WHERE NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = roles.role_id AND rm.menu_id = menus.menu_id
  );
