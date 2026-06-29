-- Analysis room platform phase 7: daily LKJ violation database and basic page.
-- This stage creates the business tables, menu, permissions and sample draft data.
-- Flow actions, Excel staging submission and result archiving are implemented in later phases.

CREATE TABLE IF NOT EXISTS daily_lkj_violation_batch (
    id BIGINT NOT NULL COMMENT '批次ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    batch_no VARCHAR(64) NOT NULL COMMENT '批次编号',
    report_date DATE NOT NULL COMMENT '提报日期',
    batch_source VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '批次来源：MANUAL/EXCEL',
    batch_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '批次状态',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    valid_count INT DEFAULT 0 COMMENT '校验通过数',
    confirm_count INT DEFAULT 0 COMMENT '需要确认数',
    failed_count INT DEFAULT 0 COMMENT '校验失败数',
    original_file_oss_id BIGINT DEFAULT NULL COMMENT '原始Excel文件OSS ID',
    original_file_name VARCHAR(255) DEFAULT NULL COMMENT '原始Excel文件名',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_lkj_batch_no (batch_no),
    KEY idx_daily_lkj_batch_report_date (report_date, del_flag),
    KEY idx_daily_lkj_batch_status (batch_status, del_flag),
    KEY idx_daily_lkj_batch_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ违标公示批次表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_record (
    id BIGINT NOT NULL COMMENT '记录ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
    report_date DATE NOT NULL COMMENT '提报日期',
    violation_date DATE NOT NULL COMMENT '违章发生日期',
    violation_time VARCHAR(16) NOT NULL COMMENT '违章发生时间',
    responsible_dept_id BIGINT DEFAULT NULL COMMENT '责任部门ID',
    responsible_dept_name VARCHAR(128) NOT NULL COMMENT '责任部门名称',
    employee_no VARCHAR(32) NOT NULL COMMENT '工号',
    responsible_person VARCHAR(64) NOT NULL COMMENT '责任人',
    locomotive VARCHAR(64) NOT NULL COMMENT '机车',
    train_no VARCHAR(64) NOT NULL COMMENT '车次',
    violation_location VARCHAR(200) NOT NULL COMMENT '地点',
    violation_code VARCHAR(64) NOT NULL COMMENT '违标编码',
    violation_name VARCHAR(200) DEFAULT NULL COMMENT '违标名称快照',
    nature VARCHAR(64) NOT NULL COMMENT '性质',
    category VARCHAR(64) NOT NULL COMMENT '类别',
    violation_type VARCHAR(64) NOT NULL COMMENT '类型',
    assessment_content VARCHAR(1000) NOT NULL COMMENT '拟考核内容',
    current_status VARCHAR(64) NOT NULL DEFAULT 'DRAFT' COMMENT '当前状态',
    preview_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '预览校验状态：PENDING/PASSED/NEED_CONFIRM/FAILED',
    source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '来源：MANUAL/EXCEL',
    workshop_id BIGINT DEFAULT NULL COMMENT '车间ID',
    workshop_name VARCHAR(128) DEFAULT NULL COMMENT '车间名称',
    team_id BIGINT DEFAULT NULL COMMENT '车队ID',
    team_name VARCHAR(128) DEFAULT NULL COMMENT '车队名称',
    guide_group_id BIGINT DEFAULT NULL COMMENT '指导组ID',
    guide_group_name VARCHAR(128) DEFAULT NULL COMMENT '指导组名称',
    submit_user_id BIGINT DEFAULT NULL COMMENT '提交人ID',
    submit_user_name VARCHAR(64) DEFAULT NULL COMMENT '提交人名称',
    submit_time DATETIME DEFAULT NULL COMMENT '提交时间',
    personnel_snapshot TEXT COMMENT '人员快照JSON',
    org_snapshot TEXT COMMENT '组织快照JSON',
    violation_code_snapshot TEXT COMMENT '违标编码快照JSON',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_daily_lkj_record_batch (batch_id, del_flag),
    KEY idx_daily_lkj_record_status (current_status, del_flag),
    KEY idx_daily_lkj_record_report_date (report_date, del_flag),
    KEY idx_daily_lkj_record_violation_date (violation_date, del_flag),
    KEY idx_daily_lkj_record_dept_status (responsible_dept_id, current_status, del_flag),
    KEY idx_daily_lkj_record_employee_date (employee_no, violation_date, del_flag),
    KEY idx_daily_lkj_record_workshop_status (workshop_id, current_status, del_flag),
    KEY idx_daily_lkj_record_team_status (team_id, current_status, del_flag),
    KEY idx_daily_lkj_record_guide_status (guide_group_id, current_status, del_flag),
    KEY idx_daily_lkj_record_code (violation_code, del_flag),
    KEY idx_daily_lkj_record_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ违标公示记录表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_import_row (
    id BIGINT NOT NULL COMMENT '导入行ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    batch_id BIGINT NOT NULL COMMENT '导入批次ID',
    row_no INT NOT NULL COMMENT 'Excel行号',
    raw_json TEXT NOT NULL COMMENT '原始行JSON快照',
    validation_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '校验状态：PENDING/PASSED/NEED_CONFIRM/FAILED',
    record_id BIGINT DEFAULT NULL COMMENT '转正式记录ID',
    error_summary VARCHAR(1000) DEFAULT NULL COMMENT '错误摘要',
    confirm_summary VARCHAR(1000) DEFAULT NULL COMMENT '需确认摘要',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_daily_lkj_import_row_batch_status (batch_id, validation_status, del_flag),
    KEY idx_daily_lkj_import_row_record (record_id),
    KEY idx_daily_lkj_import_row_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ导入预览行表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_import_error (
    id BIGINT NOT NULL COMMENT '导入错误ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    batch_id BIGINT NOT NULL COMMENT '导入批次ID',
    import_row_id BIGINT NOT NULL COMMENT '导入行ID',
    row_no INT NOT NULL COMMENT 'Excel行号',
    field_name VARCHAR(128) DEFAULT NULL COMMENT '字段名',
    error_code VARCHAR(64) NOT NULL COMMENT '错误编码',
    error_message VARCHAR(500) NOT NULL COMMENT '错误说明',
    severity VARCHAR(32) NOT NULL DEFAULT 'ERROR' COMMENT '严重级别：ERROR/WARNING',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_daily_lkj_import_error_batch (batch_id, del_flag),
    KEY idx_daily_lkj_import_error_row (import_row_id, del_flag),
    KEY idx_daily_lkj_import_error_code (error_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ导入错误明细表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_feedback (
    id BIGINT NOT NULL COMMENT '不属实反馈ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    record_id BIGINT NOT NULL COMMENT '记录ID',
    reason_type VARCHAR(64) NOT NULL COMMENT '原因类型',
    feedback_description VARCHAR(1000) NOT NULL COMMENT '反馈说明',
    feedback_snapshot TEXT COMMENT '反馈前记录快照JSON',
    feedback_user_id BIGINT DEFAULT NULL COMMENT '反馈人ID',
    feedback_user_name VARCHAR(64) DEFAULT NULL COMMENT '反馈人名称',
    feedback_dept_id BIGINT DEFAULT NULL COMMENT '反馈部门ID',
    feedback_dept_name VARCHAR(128) DEFAULT NULL COMMENT '反馈部门名称',
    feedback_time DATETIME DEFAULT NULL COMMENT '反馈时间',
    handle_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '处理状态',
    attachment_count INT DEFAULT 0 COMMENT '证据附件数量',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_daily_lkj_feedback_record (record_id, del_flag),
    KEY idx_daily_lkj_feedback_status (handle_status, del_flag),
    KEY idx_daily_lkj_feedback_dept_time (feedback_dept_id, feedback_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ不属实反馈表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_flow_log (
    id BIGINT NOT NULL COMMENT '流程证据链日志ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    business_type VARCHAR(64) NOT NULL DEFAULT 'DAILY_LKJ_VIOLATION' COMMENT '业务类型',
    record_id BIGINT DEFAULT NULL COMMENT '记录ID',
    batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
    action_type VARCHAR(64) NOT NULL COMMENT '动作类型',
    action_description VARCHAR(500) DEFAULT NULL COMMENT '动作说明',
    before_status VARCHAR(64) DEFAULT NULL COMMENT '操作前状态',
    after_status VARCHAR(64) DEFAULT NULL COMMENT '操作后状态',
    operator_user_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_user_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名快照',
    operator_dept_id BIGINT DEFAULT NULL COMMENT '操作部门ID',
    operator_dept_name VARCHAR(128) DEFAULT NULL COMMENT '操作部门名称快照',
    operator_role_snapshot VARCHAR(500) DEFAULT NULL COMMENT '操作角色快照',
    opinion VARCHAR(1000) DEFAULT NULL COMMENT '意见',
    attachment_refs TEXT COMMENT '附件引用JSON',
    changed_fields_json TEXT COMMENT '字段变更JSON',
    trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    ip_address VARCHAR(128) DEFAULT NULL COMMENT 'IP地址',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_daily_lkj_flow_record_time (record_id, create_time),
    KEY idx_daily_lkj_flow_batch_time (batch_id, create_time),
    KEY idx_daily_lkj_flow_action_time (action_type, create_time),
    KEY idx_daily_lkj_flow_operator_time (operator_user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ业务证据链日志表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_result (
    id BIGINT NOT NULL COMMENT '结果库ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    record_id BIGINT NOT NULL COMMENT '来源记录ID',
    result_version INT NOT NULL DEFAULT 1 COMMENT '结果版本',
    result_status VARCHAR(64) NOT NULL DEFAULT 'VALID' COMMENT '结果状态',
    included_flag CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否计入统计（1计入 0不计入）',
    report_date DATE NOT NULL COMMENT '提报日期',
    violation_date DATE NOT NULL COMMENT '违章发生日期',
    responsible_dept_id BIGINT DEFAULT NULL COMMENT '责任部门ID',
    responsible_dept_name VARCHAR(128) DEFAULT NULL COMMENT '责任部门名称',
    employee_no VARCHAR(32) DEFAULT NULL COMMENT '工号',
    responsible_person VARCHAR(64) DEFAULT NULL COMMENT '责任人',
    violation_code VARCHAR(64) DEFAULT NULL COMMENT '违标编码',
    nature VARCHAR(64) DEFAULT NULL COMMENT '性质',
    category VARCHAR(64) DEFAULT NULL COMMENT '类别',
    violation_type VARCHAR(64) DEFAULT NULL COMMENT '类型',
    result_snapshot TEXT NOT NULL COMMENT '入库快照JSON',
    archived_by BIGINT DEFAULT NULL COMMENT '入库人',
    archived_user_name VARCHAR(64) DEFAULT NULL COMMENT '入库人名称',
    archived_time DATETIME DEFAULT NULL COMMENT '入库时间',
    corrected_from_result_id BIGINT DEFAULT NULL COMMENT '更正来源结果ID',
    correct_reason VARCHAR(1000) DEFAULT NULL COMMENT '更正原因',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_lkj_result_record_version (record_id, result_version),
    KEY idx_daily_lkj_result_status_time (result_status, archived_time, del_flag),
    KEY idx_daily_lkj_result_included_time (included_flag, archived_time, del_flag),
    KEY idx_daily_lkj_result_dept_date (responsible_dept_id, violation_date, del_flag),
    KEY idx_daily_lkj_result_employee_date (employee_no, violation_date, del_flag),
    KEY idx_daily_lkj_result_code (violation_code, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ结果库表';

CREATE TABLE IF NOT EXISTS daily_lkj_violation_result_version (
    id BIGINT NOT NULL COMMENT '结果版本ID',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT '租户编号',
    result_id BIGINT NOT NULL COMMENT '结果ID',
    record_id BIGINT NOT NULL COMMENT '来源记录ID',
    result_version INT NOT NULL COMMENT '结果版本',
    version_snapshot TEXT NOT NULL COMMENT '版本快照JSON',
    version_reason VARCHAR(1000) DEFAULT NULL COMMENT '版本原因',
    created_version_by BIGINT DEFAULT NULL COMMENT '版本创建人',
    created_version_user_name VARCHAR(64) DEFAULT NULL COMMENT '版本创建人名称',
    del_flag CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_dept BIGINT DEFAULT NULL COMMENT '创建部门',
    create_by BIGINT DEFAULT NULL COMMENT '创建者',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新者',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_daily_lkj_result_version (result_id, result_version),
    KEY idx_daily_lkj_result_version_record (record_id, result_version, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日LKJ结果库版本表';

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 23000, '违章管理', 0, 11, 'violation', NULL, '', 1, 0, 'M', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, '综合分析室违章管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 23000);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 23001, '每日LKJ音视频违标公示', 23000, 1, 'daily', 'violation/daily/index', '', 1, 0, 'C', '0', '0', 'violation:daily:view', 'excel', 103, 1, sysdate(), NULL, NULL, '每日LKJ音视频违标公示基础页面'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 23001);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 23101 AS menu_id, '每日LKJ查询' AS menu_name, 23001 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'violation:daily:view' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 23102, '每日LKJ新增', 23001, 2, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:add', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23103, '每日LKJ编辑', 23001, 3, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:edit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23104, '每日LKJ删除草稿', 23001, 4, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:remove', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23105, '每日LKJ导入', 23001, 5, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:import', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23106, '每日LKJ预览确认', 23001, 6, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:preview', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23107, '每日LKJ提交班长', 23001, 7, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:submit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23108, '每日LKJ导出', 23001, 8, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:export', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

UPDATE sys_menu
SET menu_name = CASE menu_id
        WHEN 23000 THEN '违章管理'
        WHEN 23001 THEN '每日LKJ音视频违标公示'
        WHEN 23101 THEN '每日LKJ查询'
        WHEN 23102 THEN '每日LKJ新增'
        WHEN 23103 THEN '每日LKJ编辑'
        WHEN 23104 THEN '每日LKJ删除草稿'
        WHEN 23105 THEN '每日LKJ导入'
        WHEN 23106 THEN '每日LKJ预览确认'
        WHEN 23107 THEN '每日LKJ提交班长'
        WHEN 23108 THEN '每日LKJ导出'
        ELSE menu_name
    END,
    remark = CASE menu_id
        WHEN 23000 THEN '综合分析室违章管理'
        WHEN 23001 THEN '每日LKJ音视频违标公示基础页面'
        ELSE remark
    END
WHERE menu_id BETWEEN 23000 AND 23108;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (SELECT 1 AS role_id UNION ALL SELECT 3 AS role_id) roles
JOIN sys_menu menus ON menus.menu_id BETWEEN 23000 AND 23108
WHERE NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = roles.role_id AND rm.menu_id = menus.menu_id
  );

INSERT INTO daily_lkj_violation_batch
    (id, tenant_id, batch_no, report_date, batch_source, batch_status, total_count, valid_count, confirm_count, failed_count,
     del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (5000000000000000001, '000000', 'DLKJ-20260628-DEMO', '2026-06-28', 'MANUAL', 'DRAFT', 1, 1, 0, 0,
     '0', '阶段7本地样例批次', 103, 1, sysdate(), 1, sysdate())
ON DUPLICATE KEY UPDATE update_time = sysdate();

INSERT IGNORE INTO daily_lkj_violation_record
    (id, tenant_id, batch_id, report_date, violation_date, violation_time, responsible_dept_id, responsible_dept_name,
     employee_no, responsible_person, locomotive, train_no, violation_location, violation_code, violation_name,
     nature, category, violation_type, assessment_content, current_status, preview_status, source_type,
     workshop_id, workshop_name, team_id, team_name, guide_group_id, guide_group_name,
     personnel_snapshot, org_snapshot, violation_code_snapshot, del_flag, remark, create_dept, create_by, create_time, update_by, update_time)
VALUES
    (5000000000000000101, '000000', 5000000000000000001, '2026-06-28', '2026-06-27', '08:30', 103, '研发部门',
     '100001', '张三', 'HXD1-0001', 'K123', '示例区间', 'LKJ-001', '未按规定确认信号',
     'A类', 'LKJ', '作业违标', '阶段7样例草稿记录，用于验证基础页面和CRUD。', 'DRAFT', 'PASSED', 'MANUAL',
     3000000000000000002, '示例车间', 3000000000000000003, '示例车队', 3000000000000000004, '示例指导组',
     '{"employeeNo":"100001","responsiblePerson":"张三"}',
     '{"responsibleDeptName":"研发部门","workshopName":"示例车间","teamName":"示例车队","guideGroupName":"示例指导组"}',
     '{"violationCode":"LKJ-001","nature":"A类","category":"LKJ","violationType":"作业违标"}',
     '0', '阶段7本地样例草稿', 103, 1, sysdate(), 1, sysdate());
