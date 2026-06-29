-- Analysis room platform phase 6: lightweight chat collaboration and business cards.
-- Chat is a non-formal collaboration reminder. Business cards are navigation hints, not authorization.

CREATE TABLE IF NOT EXISTS biz_chat_conversation (
    id BIGINT NOT NULL COMMENT '会话ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    conversation_type VARCHAR(32) NOT NULL DEFAULT 'BUSINESS' COMMENT '会话类型：DIRECT/GROUP/BUSINESS',
    conversation_title VARCHAR(200) NOT NULL COMMENT '会话标题',
    business_type VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型',
    business_id VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    business_title VARCHAR(200) DEFAULT NULL COMMENT '关联业务标题快照',
    business_url VARCHAR(500) DEFAULT NULL COMMENT '关联业务跳转地址',
    conversation_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '会话状态：ACTIVE/ARCHIVED',
    last_message_id BIGINT DEFAULT NULL COMMENT '最后消息ID',
    last_message_content VARCHAR(500) DEFAULT NULL COMMENT '最后消息摘要',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_biz_chat_conversation_business (business_type, business_id, del_flag),
    KEY idx_biz_chat_conversation_status (conversation_status, del_flag),
    KEY idx_biz_chat_conversation_last_time (last_message_time),
    KEY idx_biz_chat_conversation_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轻量聊天会话表';

CREATE TABLE IF NOT EXISTS biz_chat_conversation_member (
    id BIGINT NOT NULL COMMENT '会话成员ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    member_user_id BIGINT NOT NULL COMMENT '成员用户ID',
    member_user_name VARCHAR(64) DEFAULT NULL COMMENT '成员用户名称',
    member_dept_id BIGINT DEFAULT NULL COMMENT '成员部门ID',
    member_dept_name VARCHAR(128) DEFAULT NULL COMMENT '成员部门名称',
    member_role VARCHAR(32) DEFAULT 'MEMBER' COMMENT '成员角色：OWNER/MEMBER',
    last_read_message_id BIGINT DEFAULT NULL COMMENT '最后已读消息ID',
    unread_count INT DEFAULT 0 COMMENT '未读数量',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_biz_chat_member (conversation_id, member_user_id, del_flag),
    KEY idx_biz_chat_member_user (member_user_id, del_flag),
    KEY idx_biz_chat_member_conversation (conversation_id, del_flag),
    KEY idx_biz_chat_member_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轻量聊天会话成员表';

CREATE TABLE IF NOT EXISTS biz_chat_message (
    id BIGINT NOT NULL COMMENT '聊天消息ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    message_type VARCHAR(32) NOT NULL COMMENT '消息类型：TEXT/BUSINESS_CARD',
    message_content TEXT COMMENT '消息内容',
    business_type VARCHAR(64) DEFAULT NULL COMMENT '业务卡片业务类型',
    business_id VARCHAR(64) DEFAULT NULL COMMENT '业务卡片业务ID',
    business_title VARCHAR(200) DEFAULT NULL COMMENT '业务卡片标题快照',
    business_url VARCHAR(500) DEFAULT NULL COMMENT '业务卡片跳转地址',
    business_payload TEXT COMMENT '业务卡片JSON快照',
    sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
    sender_user_name VARCHAR(64) DEFAULT NULL COMMENT '发送人名称',
    sender_dept_id BIGINT DEFAULT NULL COMMENT '发送部门ID',
    sender_dept_name VARCHAR(128) DEFAULT NULL COMMENT '发送部门名称',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_biz_chat_message_conversation (conversation_id, create_time, del_flag),
    KEY idx_biz_chat_message_sender (sender_user_id, create_time, del_flag),
    KEY idx_biz_chat_message_business (business_type, business_id, del_flag),
    KEY idx_biz_chat_message_type (message_type, del_flag),
    KEY idx_biz_chat_message_del_flag (del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轻量聊天消息表';

INSERT IGNORE INTO biz_chat_conversation
    (id, tenant_id, conversation_type, conversation_title, business_type, business_id, business_title, business_url,
     conversation_status, last_message_id, last_message_content, last_message_time, del_flag, remark,
     create_dept, create_by, create_time, update_by, update_time)
VALUES
    (4000000000000000201, '000000', 'BUSINESS', '示例：LKJ 公示业务协同',
     'DAILY_LKJ_VIOLATION', 'DEMO-001', '示例：班长待审核 LKJ 公示记录', '/index',
     'ACTIVE', 4000000000000000222, '业务卡片：LKJ 公示 DEMO-001', sysdate(), '0',
     '阶段6本地示例会话，不代表正式业务数据', 103, 1, sysdate(), 1, sysdate());

INSERT IGNORE INTO biz_chat_conversation_member
    (id, tenant_id, conversation_id, member_user_id, member_user_name, member_dept_id, member_dept_name, member_role,
     last_read_message_id, unread_count, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (4000000000000000211, '000000', 4000000000000000201, 1, 'admin', 103, '研发部门', 'OWNER',
     4000000000000000221, 1, '0', '阶段6本地示例成员', 103, 1, sysdate(), 1, sysdate());

INSERT IGNORE INTO biz_chat_message
    (id, tenant_id, conversation_id, message_type, message_content, business_type, business_id, business_title, business_url, business_payload,
     sender_user_id, sender_user_name, sender_dept_id, sender_dept_name, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (4000000000000000221, '000000', 4000000000000000201, 'TEXT',
     '示例聊天：请关注班长待审核记录，聊天仅作提醒，不代表审批授权。',
     NULL, NULL, NULL, NULL, NULL, 1, 'admin', 103, '研发部门', '0', '阶段6本地示例文本消息', 103, 1, DATE_SUB(sysdate(), INTERVAL 5 MINUTE), NULL, NULL),
    (4000000000000000222, '000000', 4000000000000000201, 'BUSINESS_CARD',
     '业务卡片：LKJ 公示 DEMO-001',
     'DAILY_LKJ_VIOLATION', 'DEMO-001', '示例：班长待审核 LKJ 公示记录', '/index',
     '{"businessType":"DAILY_LKJ_VIOLATION","businessId":"DEMO-001","note":"业务卡片只作提醒，不代表授权"}',
     1, 'admin', 103, '研发部门', '0', '阶段6本地示例业务卡片', 103, 1, sysdate(), NULL, NULL);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22400, '聊天协同', 0, 12, 'chat', NULL, '', 1, 0, 'M', '0', '0', '', 'message', 103, 1, sysdate(), NULL, NULL, '综合分析室平台轻量聊天协同'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22400);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 22401, '协同会话', 22400, 1, 'conversations', 'chat/index', '', 1, 0, 'C', '0', '0', 'chat:view', 'message', 103, 1, sysdate(), NULL, NULL, '轻量协同会话和业务卡片'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 22401);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 22500 AS menu_id, '发送聊天消息' AS menu_name, 22401 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'chat:view' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 22501, '发送业务卡片', 22401, 2, '#', '', '', 1, 0, 'F', '0', '0', 'chat:share', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

UPDATE sys_menu
SET menu_name = CASE menu_id
        WHEN 22400 THEN '聊天协同'
        WHEN 22401 THEN '协同会话'
        WHEN 22500 THEN '发送聊天消息'
        WHEN 22501 THEN '发送业务卡片'
        ELSE menu_name
    END,
    remark = CASE menu_id
        WHEN 22400 THEN '综合分析室平台轻量聊天协同'
        WHEN 22401 THEN '轻量协同会话和业务卡片'
        ELSE remark
    END
WHERE menu_id IN (22400, 22401, 22500, 22501);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (SELECT 1 AS role_id UNION ALL SELECT 3 AS role_id) roles
JOIN sys_menu menus ON menus.menu_id BETWEEN 22400 AND 22501
WHERE NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = roles.role_id AND rm.menu_id = menus.menu_id
  );
