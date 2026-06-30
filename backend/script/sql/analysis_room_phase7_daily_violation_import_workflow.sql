-- Phase 7: Daily LKJ audio/video violation import, preview, workflow, feedback and result archive.
-- Execute with utf8mb4. This script is additive and keeps existing RuoYi tables intact.

DELIMITER //
DROP PROCEDURE IF EXISTS add_base_personnel_column//
CREATE PROCEDURE add_base_personnel_column(IN p_column_name VARCHAR(64), IN p_column_definition TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'base_personnel'
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE base_personnel ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_base_personnel_column('nation', 'VARCHAR(32) DEFAULT NULL COMMENT ''nation''');
CALL add_base_personnel_column('line_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''line name''');
CALL add_base_personnel_column('job_title', 'VARCHAR(100) DEFAULT NULL COMMENT ''job title''');
CALL add_base_personnel_column('current_position', 'VARCHAR(100) DEFAULT NULL COMMENT ''current position''');
CALL add_base_personnel_column('command_time', 'VARCHAR(64) DEFAULT NULL COMMENT ''command time''');
CALL add_base_personnel_column('post_time', 'VARCHAR(64) DEFAULT NULL COMMENT ''post time''');
CALL add_base_personnel_column('political_status', 'VARCHAR(64) DEFAULT NULL COMMENT ''political status''');
CALL add_base_personnel_column('qualification', 'VARCHAR(128) DEFAULT NULL COMMENT ''qualification''');
CALL add_base_personnel_column('permitted_locomotive_type', 'VARCHAR(128) DEFAULT NULL COMMENT ''permitted locomotive type''');
CALL add_base_personnel_column('birth_date', 'VARCHAR(64) DEFAULT NULL COMMENT ''birth date''');
CALL add_base_personnel_column('work_start_date', 'VARCHAR(64) DEFAULT NULL COMMENT ''work start date''');
CALL add_base_personnel_column('id_card', 'VARCHAR(64) DEFAULT NULL COMMENT ''id card''');
CALL add_base_personnel_column('work_card_no', 'VARCHAR(64) DEFAULT NULL COMMENT ''work card no''');
CALL add_base_personnel_column('normalized_name', 'VARCHAR(64) DEFAULT NULL COMMENT ''normalized name''');
CALL add_base_personnel_column('raw_json', 'TEXT COMMENT ''raw row json''');
DROP PROCEDURE IF EXISTS add_base_personnel_column;

CREATE TABLE IF NOT EXISTS daily_violation_batch (
    batch_id BIGINT NOT NULL COMMENT 'batch id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    report_date DATE NOT NULL COMMENT 'report date',
    report_title VARCHAR(200) DEFAULT NULL COMMENT 'report title',
    source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/EXCEL_IMPORT',
    source_file_id BIGINT DEFAULT NULL COMMENT 'source file oss id',
    batch_status VARCHAR(64) NOT NULL DEFAULT 'DRAFT' COMMENT 'batch status code',
    total_rows INT DEFAULT 0 COMMENT 'total rows',
    valid_rows INT DEFAULT 0 COMMENT 'valid rows',
    warning_rows INT DEFAULT 0 COMMENT 'warning rows',
    invalid_rows INT DEFAULT 0 COMMENT 'invalid rows',
    submitted_rows INT DEFAULT 0 COMMENT 'submitted rows',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    create_dept BIGINT DEFAULT NULL COMMENT 'create dept',
    create_by BIGINT DEFAULT NULL COMMENT 'create by',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_by BIGINT DEFAULT NULL COMMENT 'update by',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (batch_id),
    KEY idx_daily_violation_batch_report (report_date, del_flag),
    KEY idx_daily_violation_batch_status (batch_status, del_flag),
    KEY idx_daily_violation_batch_source (source_type, del_flag),
    KEY idx_daily_violation_batch_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation batch';

CREATE TABLE IF NOT EXISTS daily_violation_record (
    record_id BIGINT NOT NULL COMMENT 'record id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    batch_id BIGINT DEFAULT NULL COMMENT 'batch id',
    report_date DATE NOT NULL COMMENT 'report date',
    violation_date DATE DEFAULT NULL COMMENT 'violation date',
    violation_time VARCHAR(64) DEFAULT NULL COMMENT 'violation time',
    sequence_no VARCHAR(32) DEFAULT NULL COMMENT 'sequence no',
    violation_code VARCHAR(64) NOT NULL COMMENT 'violation code',
    violation_nature_snapshot VARCHAR(100) DEFAULT NULL COMMENT 'nature snapshot',
    violation_category_snapshot VARCHAR(100) DEFAULT NULL COMMENT 'category snapshot',
    violation_type_snapshot VARCHAR(100) DEFAULT NULL COMMENT 'type snapshot',
    proposed_assessment_content TEXT NOT NULL COMMENT 'proposed assessment content',
    responsible_dept_id BIGINT DEFAULT NULL COMMENT 'responsible dept id',
    responsible_dept_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT 'responsible dept snapshot',
    responsible_person_id BIGINT DEFAULT NULL COMMENT 'responsible person id',
    employee_no VARCHAR(32) DEFAULT NULL COMMENT 'employee no',
    employee_name_snapshot VARCHAR(64) DEFAULT NULL COMMENT 'employee name snapshot',
    locomotive VARCHAR(64) DEFAULT NULL COMMENT 'locomotive',
    train_no VARCHAR(64) DEFAULT NULL COMMENT 'train no',
    location VARCHAR(200) DEFAULT NULL COMMENT 'location',
    time_segment VARCHAR(128) DEFAULT NULL COMMENT 'time segment',
    ticket_no VARCHAR(128) DEFAULT NULL COMMENT 'ticket no',
    guide_driver VARCHAR(128) DEFAULT NULL COMMENT 'guide driver',
    guide_group VARCHAR(128) DEFAULT NULL COMMENT 'guide group',
    party_member_flag VARCHAR(32) DEFAULT NULL COMMENT 'party member flag',
    abcd_assignment VARCHAR(128) DEFAULT NULL COMMENT 'abcd assignment',
    handling_assessment VARCHAR(200) DEFAULT NULL COMMENT 'handling assessment',
    issuing_dept VARCHAR(128) DEFAULT NULL COMMENT 'issuing dept',
    current_status VARCHAR(64) NOT NULL DEFAULT 'DRAFT' COMMENT 'current status code',
    version INT NOT NULL DEFAULT 1 COMMENT 'business version',
    source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/EXCEL_IMPORT',
    import_batch_id BIGINT DEFAULT NULL COMMENT 'import batch id',
    import_row_id BIGINT DEFAULT NULL COMMENT 'import row id',
    workshop_id BIGINT DEFAULT NULL COMMENT 'workshop id',
    workshop_name VARCHAR(128) DEFAULT NULL COMMENT 'workshop name',
    team_id BIGINT DEFAULT NULL COMMENT 'team id',
    team_name VARCHAR(128) DEFAULT NULL COMMENT 'team name',
    guide_group_id BIGINT DEFAULT NULL COMMENT 'guide group id',
    guide_group_name VARCHAR(128) DEFAULT NULL COMMENT 'guide group name',
    preview_confirmed CHAR(1) NOT NULL DEFAULT '0' COMMENT 'preview confirmed',
    validation_status VARCHAR(32) NOT NULL DEFAULT 'VALID' COMMENT 'VALID/NEED_CONFIRM/INVALID',
    validation_message VARCHAR(1000) DEFAULT NULL COMMENT 'validation message',
    personnel_snapshot TEXT COMMENT 'personnel snapshot json',
    org_snapshot TEXT COMMENT 'org snapshot json',
    violation_code_snapshot TEXT COMMENT 'violation code snapshot json',
    final_opinion VARCHAR(1000) DEFAULT NULL COMMENT 'final opinion',
    final_decision VARCHAR(64) DEFAULT NULL COMMENT 'MAINTAIN/CANCEL_EXCLUDED/RECHECK',
    cancel_reason VARCHAR(1000) DEFAULT NULL COMMENT 'cancel reason',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    create_dept BIGINT DEFAULT NULL COMMENT 'create dept',
    create_by BIGINT DEFAULT NULL COMMENT 'create by',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_by BIGINT DEFAULT NULL COMMENT 'update by',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (record_id),
    KEY idx_daily_violation_record_batch (batch_id, del_flag),
    KEY idx_daily_violation_record_import (import_batch_id, import_row_id),
    KEY idx_daily_violation_record_status (current_status, del_flag),
    KEY idx_daily_violation_record_report (report_date, del_flag),
    KEY idx_daily_violation_record_violation_date (violation_date, del_flag),
    KEY idx_daily_violation_record_dept_status (responsible_dept_id, current_status, del_flag),
    KEY idx_daily_violation_record_employee_date (employee_no, violation_date, del_flag),
    KEY idx_daily_violation_record_workshop_status (workshop_id, current_status, del_flag),
    KEY idx_daily_violation_record_team_status (team_id, current_status, del_flag),
    KEY idx_daily_violation_record_guide_status (guide_group_id, current_status, del_flag),
    KEY idx_daily_violation_record_code (violation_code, del_flag),
    KEY idx_daily_violation_record_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation record';

CREATE TABLE IF NOT EXISTS daily_violation_import_batch (
    import_batch_id BIGINT NOT NULL COMMENT 'import batch id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    report_date DATE DEFAULT NULL COMMENT 'report date',
    original_file_id BIGINT DEFAULT NULL COMMENT 'original oss file id',
    original_file_name VARCHAR(255) DEFAULT NULL COMMENT 'original file name',
    sheet_name VARCHAR(128) DEFAULT NULL COMMENT 'sheet name',
    title_text VARCHAR(255) DEFAULT NULL COMMENT 'title text',
    header_row_index INT DEFAULT NULL COMMENT 'header row index',
    total_rows INT DEFAULT 0 COMMENT 'total rows',
    valid_rows INT DEFAULT 0 COMMENT 'valid rows',
    warning_rows INT DEFAULT 0 COMMENT 'warning rows',
    invalid_rows INT DEFAULT 0 COMMENT 'invalid rows',
    import_status VARCHAR(32) NOT NULL DEFAULT 'PARSED' COMMENT 'PARSED/SUBMITTED',
    imported_by BIGINT DEFAULT NULL COMMENT 'imported by',
    imported_user_name VARCHAR(64) DEFAULT NULL COMMENT 'imported user name',
    imported_time DATETIME DEFAULT NULL COMMENT 'imported time',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    create_dept BIGINT DEFAULT NULL COMMENT 'create dept',
    create_by BIGINT DEFAULT NULL COMMENT 'create by',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_by BIGINT DEFAULT NULL COMMENT 'update by',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (import_batch_id),
    KEY idx_daily_violation_import_report (report_date, del_flag),
    KEY idx_daily_violation_import_status (import_status, del_flag),
    KEY idx_daily_violation_import_user_time (imported_by, imported_time),
    KEY idx_daily_violation_import_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation excel import batch';

CREATE TABLE IF NOT EXISTS daily_violation_import_row (
    row_id BIGINT NOT NULL COMMENT 'preview row id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    import_batch_id BIGINT NOT NULL COMMENT 'import batch id',
    row_no INT NOT NULL COMMENT 'excel row no',
    raw_json TEXT NOT NULL COMMENT 'raw row json',
    sequence_no VARCHAR(32) DEFAULT NULL COMMENT 'sequence no',
    violation_code VARCHAR(64) DEFAULT NULL COMMENT 'violation code',
    proposed_assessment_content TEXT COMMENT 'proposed assessment content',
    violation_nature VARCHAR(100) DEFAULT NULL COMMENT 'nature',
    violation_category VARCHAR(100) DEFAULT NULL COMMENT 'category',
    violation_type VARCHAR(100) DEFAULT NULL COMMENT 'type',
    responsible_dept_name VARCHAR(128) DEFAULT NULL COMMENT 'responsible dept name',
    responsible_person_name VARCHAR(64) DEFAULT NULL COMMENT 'responsible person name',
    handling_assessment VARCHAR(200) DEFAULT NULL COMMENT 'handling assessment',
    issuing_dept VARCHAR(128) DEFAULT NULL COMMENT 'issuing dept',
    employee_no VARCHAR(32) DEFAULT NULL COMMENT 'employee no',
    time_segment VARCHAR(128) DEFAULT NULL COMMENT 'time segment',
    party_member_flag VARCHAR(32) DEFAULT NULL COMMENT 'party member flag',
    ticket_no VARCHAR(128) DEFAULT NULL COMMENT 'ticket no',
    guide_driver VARCHAR(128) DEFAULT NULL COMMENT 'guide driver',
    false_reason VARCHAR(1000) DEFAULT NULL COMMENT 'false reason',
    location VARCHAR(200) DEFAULT NULL COMMENT 'location',
    guide_group VARCHAR(128) DEFAULT NULL COMMENT 'guide group',
    abcd_assignment VARCHAR(128) DEFAULT NULL COMMENT 'abcd assignment',
    parsed_violation_date DATE DEFAULT NULL COMMENT 'parsed violation date',
    parsed_violation_time VARCHAR(128) DEFAULT NULL COMMENT 'parsed violation time',
    parsed_locomotive VARCHAR(64) DEFAULT NULL COMMENT 'parsed locomotive',
    parsed_train_no VARCHAR(64) DEFAULT NULL COMMENT 'parsed train no',
    candidate_person_names VARCHAR(500) DEFAULT NULL COMMENT 'candidate names',
    validation_status VARCHAR(32) NOT NULL DEFAULT 'INVALID' COMMENT 'VALID/NEED_CONFIRM/INVALID',
    validation_message VARCHAR(1000) DEFAULT NULL COMMENT 'validation message',
    confirm_status VARCHAR(32) NOT NULL DEFAULT 'UNCONFIRMED' COMMENT 'UNCONFIRMED/CONFIRMED/REJECTED',
    confirm_remark VARCHAR(500) DEFAULT NULL COMMENT 'confirm remark',
    generated_record_id BIGINT DEFAULT NULL COMMENT 'generated record id',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    create_dept BIGINT DEFAULT NULL COMMENT 'create dept',
    create_by BIGINT DEFAULT NULL COMMENT 'create by',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_by BIGINT DEFAULT NULL COMMENT 'update by',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (row_id),
    KEY idx_daily_violation_row_batch_status (import_batch_id, validation_status, confirm_status, del_flag),
    KEY idx_daily_violation_row_record (generated_record_id),
    KEY idx_daily_violation_row_code (violation_code, del_flag),
    KEY idx_daily_violation_row_employee_date (employee_no, parsed_violation_date, del_flag),
    KEY idx_daily_violation_row_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation excel import preview row';

CREATE TABLE IF NOT EXISTS daily_violation_import_error (
    error_id BIGINT NOT NULL COMMENT 'error id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    import_batch_id BIGINT NOT NULL COMMENT 'import batch id',
    row_id BIGINT DEFAULT NULL COMMENT 'preview row id',
    row_no INT NOT NULL COMMENT 'excel row no',
    field_name VARCHAR(128) DEFAULT NULL COMMENT 'field name',
    error_code VARCHAR(64) NOT NULL COMMENT 'error code',
    error_message VARCHAR(500) NOT NULL COMMENT 'error message',
    raw_value VARCHAR(1000) DEFAULT NULL COMMENT 'raw value',
    suggestion VARCHAR(500) DEFAULT NULL COMMENT 'suggestion',
    severity VARCHAR(32) DEFAULT 'ERROR' COMMENT 'ERROR/WARNING',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    PRIMARY KEY (error_id),
    KEY idx_daily_violation_error_batch (import_batch_id),
    KEY idx_daily_violation_error_row (row_id),
    KEY idx_daily_violation_error_code (error_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation import error';

CREATE TABLE IF NOT EXISTS daily_violation_flow_log (
    flow_id BIGINT NOT NULL COMMENT 'flow log id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    record_id BIGINT DEFAULT NULL COMMENT 'record id',
    batch_id BIGINT DEFAULT NULL COMMENT 'batch id',
    action_code VARCHAR(64) NOT NULL COMMENT 'action code',
    before_status VARCHAR(64) DEFAULT NULL COMMENT 'before status',
    after_status VARCHAR(64) DEFAULT NULL COMMENT 'after status',
    operator_id BIGINT DEFAULT NULL COMMENT 'operator id',
    operator_name_snapshot VARCHAR(64) DEFAULT NULL COMMENT 'operator name',
    operator_dept_snapshot VARCHAR(128) DEFAULT NULL COMMENT 'operator dept',
    operator_role_snapshot VARCHAR(500) DEFAULT NULL COMMENT 'operator role snapshot',
    opinion VARCHAR(1000) DEFAULT NULL COMMENT 'opinion',
    attachment_refs TEXT COMMENT 'attachment refs json',
    changed_fields_json TEXT COMMENT 'changed fields json',
    trace_id VARCHAR(64) DEFAULT NULL COMMENT 'trace id',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    PRIMARY KEY (flow_id),
    KEY idx_daily_violation_flow_record_time (record_id, create_time),
    KEY idx_daily_violation_flow_batch_time (batch_id, create_time),
    KEY idx_daily_violation_flow_action_time (action_code, create_time),
    KEY idx_daily_violation_flow_operator_time (operator_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation flow evidence log';

CREATE TABLE IF NOT EXISTS daily_violation_feedback (
    feedback_id BIGINT NOT NULL COMMENT 'feedback id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    record_id BIGINT NOT NULL COMMENT 'record id',
    batch_id BIGINT DEFAULT NULL COMMENT 'batch id',
    reason_type VARCHAR(64) NOT NULL COMMENT 'reason type',
    reason_description VARCHAR(1000) NOT NULL COMMENT 'reason description',
    feedback_dept_id BIGINT DEFAULT NULL COMMENT 'feedback dept id',
    feedback_dept_name_snapshot VARCHAR(128) DEFAULT NULL COMMENT 'feedback dept snapshot',
    feedback_user_id BIGINT DEFAULT NULL COMMENT 'feedback user id',
    feedback_user_name_snapshot VARCHAR(64) DEFAULT NULL COMMENT 'feedback user snapshot',
    attachment_refs TEXT COMMENT 'attachment refs json',
    feedback_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'feedback status',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'remark',
    create_dept BIGINT DEFAULT NULL COMMENT 'create dept',
    create_by BIGINT DEFAULT NULL COMMENT 'create by',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_by BIGINT DEFAULT NULL COMMENT 'update by',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (feedback_id),
    KEY idx_daily_violation_feedback_record (record_id, del_flag),
    KEY idx_daily_violation_feedback_batch (batch_id, del_flag),
    KEY idx_daily_violation_feedback_status (feedback_status, del_flag),
    KEY idx_daily_violation_feedback_dept_time (feedback_dept_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation false feedback';

CREATE TABLE IF NOT EXISTS daily_violation_result (
    result_id BIGINT NOT NULL COMMENT 'result id',
    tenant_id VARCHAR(20) DEFAULT '000000' COMMENT 'tenant id',
    record_id BIGINT NOT NULL COMMENT 'source record id',
    batch_id BIGINT DEFAULT NULL COMMENT 'batch id',
    result_version INT NOT NULL DEFAULT 1 COMMENT 'result version',
    result_status VARCHAR(64) NOT NULL DEFAULT 'ARCHIVED' COMMENT 'result status',
    included CHAR(1) NOT NULL DEFAULT '1' COMMENT 'included in statistics',
    result_snapshot TEXT NOT NULL COMMENT 'result snapshot json',
    archived_by BIGINT DEFAULT NULL COMMENT 'archived by',
    archived_user_name VARCHAR(64) DEFAULT NULL COMMENT 'archived user name',
    archived_time DATETIME DEFAULT NULL COMMENT 'archived time',
    corrected_from_result_id BIGINT DEFAULT NULL COMMENT 'corrected from result id',
    correct_reason VARCHAR(1000) DEFAULT NULL COMMENT 'correct reason',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'delete flag',
    create_time DATETIME DEFAULT NULL COMMENT 'create time',
    update_time DATETIME DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (result_id),
    UNIQUE KEY uk_daily_violation_result_record_version (record_id, result_version),
    KEY idx_daily_violation_result_batch (batch_id, del_flag),
    KEY idx_daily_violation_result_status_time (result_status, archived_time, del_flag),
    KEY idx_daily_violation_result_included_time (included, archived_time, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily violation result archive';

INSERT IGNORE INTO base_violation_code
    (id, violation_code, violation_name, nature, category, violation_type, description, status, del_flag, remark, create_dept, create_by, create_time)
VALUES
    (3100000000000000201, 'QBJ028', 'Sample violation QBJ028', '建议轻微违标', '乘务员两标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000202, 'YBP004', 'Sample violation YBP004', '建议一般违标', '建议轻微违标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000203, 'QBJ079', 'Sample violation QBJ079', '建议轻微违标', '乘务员两标', '制动机使用', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000204, 'QBJ107', 'Sample violation QBJ107', '建议轻微违标', '乘务员两标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000205, 'QBP006', 'Sample violation QBP006', '建议轻微违标', '乘务员两标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000206, 'QBJ118', 'Sample violation QBJ118', '建议轻微违标', '乘务员两标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate()),
    (3100000000000000207, 'YBP106', 'Sample violation YBP106', '建议一般违标', '乘务员两标', '其他', 'sample 6.12 freight worksheet', '0', '0', 'phase7 sample violation code', 103, 1, sysdate());

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 23000, '违章管理', 0, 11, 'violation', NULL, '', 1, 0, 'M', '0', '0', '', 'form', 103, 1, sysdate(), NULL, NULL, 'analysis room violation module'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 23000);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT 23001, '每日LKJ音视频违标公示', 23000, 1, 'daily', 'violation/daily/index', '', 1, 0, 'C', '0', '0', 'violation:daily:view', 'excel', 103, 1, sysdate(), NULL, NULL, 'daily LKJ violation publicity'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 23001);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT * FROM (
    SELECT 23101 AS menu_id, '每日违标查询' AS menu_name, 23001 AS parent_id, 1 AS order_num, '#' AS path, '' AS component, '' AS query_param, 1 AS is_frame, 0 AS is_cache, 'F' AS menu_type, '0' AS visible, '0' AS status, 'violation:daily:view' AS perms, '#' AS icon, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, '' AS remark
    UNION ALL SELECT 23102, '每日违标新增', 23001, 2, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:add', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23103, '每日违标编辑', 23001, 3, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:edit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23104, '每日违标导入', 23001, 4, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:import', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23105, '每日违标预览确认', 23001, 5, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:preview', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23106, '每日违标提交', 23001, 6, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:submit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23107, '班长审核', 23001, 7, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:leader-audit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23108, '主任审核', 23001, 8, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:director-audit', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23109, '退回', 23001, 9, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:return', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23110, '下发车间', 23001, 10, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:dispatch-workshop', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23111, '下发车队', 23001, 11, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:dispatch-team', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23112, '下发指导组', 23001, 12, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:dispatch-guide-group', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23113, '指导组确认', 23001, 13, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:guide-confirm', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23114, '不属实反馈', 23001, 14, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:feedback', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23115, '返回复核', 23001, 15, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:review', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23116, '最终确认', 23001, 16, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:confirm', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23117, '入结果库', 23001, 17, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:archive', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23118, '撤销不计入', 23001, 18, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:cancel', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23119, '每日违标导出', 23001, 19, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:export', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23120, '结果库查看', 23001, 20, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:result:view', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23121, '结果库导出', 23001, 21, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:result:export', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23122, '附件上传', 23001, 22, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:attachment:upload', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23123, '附件查看', 23001, 23, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:attachment:view', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23124, '附件下载', 23001, 24, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:attachment:download', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23125, '结果库更正', 23001, 25, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:result:correct', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23126, '结果库版本查看', 23001, 26, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:result:version:view', '#', 103, 1, sysdate(), NULL, NULL, ''
    UNION ALL SELECT 23127, '编码授权覆盖', 23001, 27, '#', '', '', 1, 0, 'F', '0', '0', 'violation:daily:override', '#', 103, 1, sysdate(), NULL, NULL, ''
) menus
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id);

UPDATE sys_menu
SET component = CASE menu_id WHEN 23001 THEN 'violation/daily/index' ELSE component END,
    visible = '0',
    status = '0'
WHERE menu_id BETWEEN 23000 AND 23127;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (SELECT 1 AS role_id UNION ALL SELECT 3 AS role_id) roles
JOIN sys_menu menus ON menus.menu_id BETWEEN 23000 AND 23127
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = roles.role_id AND rm.menu_id = menus.menu_id
);

INSERT INTO sys_role
    (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT roles.role_id, roles.tenant_id, roles.role_name, roles.role_key, roles.role_sort, roles.data_scope, roles.menu_check_strictly, roles.dept_check_strictly, roles.status, roles.del_flag, roles.create_dept, roles.create_by, roles.create_time, roles.update_by, roles.update_time, roles.remark
FROM (
    SELECT 3101 AS role_id, '000000' AS tenant_id, '开发测试-分析员' AS role_name, 'analyst_test' AS role_key, 31 AS role_sort, '5' AS data_scope, 1 AS menu_check_strictly, 1 AS dept_check_strictly, '0' AS status, '0' AS del_flag, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, 'phase7 dev role' AS remark
    UNION ALL SELECT 3102, '000000', '开发测试-分析班长', 'leader_test', 32, '4', 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, 'phase7 dev role'
    UNION ALL SELECT 3103, '000000', '开发测试-分析室主任', 'director_test', 33, '3', 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, 'phase7 dev role'
    UNION ALL SELECT 3104, '000000', '开发测试-车间', 'workshop_test', 34, '3', 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, 'phase7 dev role'
    UNION ALL SELECT 3105, '000000', '开发测试-车队', 'team_test', 35, '3', 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, 'phase7 dev role'
    UNION ALL SELECT 3106, '000000', '开发测试-指导组', 'guide_test', 36, '3', 1, 1, '0', '0', 103, 1, sysdate(), NULL, NULL, 'phase7 dev role'
) roles
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE sys_role.role_id = roles.role_id);

INSERT INTO sys_user
    (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, create_dept, create_by, create_time, update_by, update_time, remark)
SELECT users.user_id, users.tenant_id, users.dept_id, users.user_name, users.nick_name, users.user_type, users.email, users.phonenumber, users.sex, users.avatar, users.password, users.status, users.del_flag, users.login_ip, users.login_date, users.create_dept, users.create_by, users.create_time, users.update_by, users.update_time, users.remark
FROM (
    SELECT 3101 AS user_id, '000000' AS tenant_id, 103 AS dept_id, 'analyst_test' AS user_name, '开发测试-分析员' AS nick_name, 'sys_user' AS user_type, '' AS email, '' AS phonenumber, '2' AS sex, NULL AS avatar, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne' AS password, '0' AS status, '0' AS del_flag, '127.0.0.1' AS login_ip, sysdate() AS login_date, 103 AS create_dept, 1 AS create_by, sysdate() AS create_time, NULL AS update_by, NULL AS update_time, 'password 666666, development only' AS remark
    UNION ALL SELECT 3102, '000000', 103, 'leader_test', '开发测试-分析班长', 'sys_user', '', '', '2', NULL, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), NULL, NULL, 'password 666666, development only'
    UNION ALL SELECT 3103, '000000', 103, 'director_test', '开发测试-分析室主任', 'sys_user', '', '', '2', NULL, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), NULL, NULL, 'password 666666, development only'
    UNION ALL SELECT 3104, '000000', 103, 'workshop_test', '开发测试-车间', 'sys_user', '', '', '2', NULL, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), NULL, NULL, 'password 666666, development only'
    UNION ALL SELECT 3105, '000000', 103, 'team_test', '开发测试-车队', 'sys_user', '', '', '2', NULL, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), NULL, NULL, 'password 666666, development only'
    UNION ALL SELECT 3106, '000000', 103, 'guide_test', '开发测试-指导组', 'sys_user', '', '', '2', NULL, '$2a$10$b8yUzN0C71sbz.PhNOCgJe.Tu1yWC3RNrTyjSQ8p1W0.aaUXUJ.Ne', '0', '0', '127.0.0.1', sysdate(), 103, 1, sysdate(), NULL, NULL, 'password 666666, development only'
) users
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE sys_user.user_id = users.user_id);

INSERT INTO sys_user_role (user_id, role_id)
SELECT user_role.user_id, user_role.role_id
FROM (
    SELECT 3101 AS user_id, 3101 AS role_id
    UNION ALL SELECT 3102, 3102
    UNION ALL SELECT 3103, 3103
    UNION ALL SELECT 3104, 3104
    UNION ALL SELECT 3105, 3105
    UNION ALL SELECT 3106, 3106
) user_role
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = user_role.user_id AND ur.role_id = user_role.role_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT role_menu.role_id, role_menu.menu_id
FROM (
    SELECT 3101 AS role_id, 23000 AS menu_id UNION ALL SELECT 3101, 23001 UNION ALL SELECT 3101, 23101 UNION ALL SELECT 3101, 23102 UNION ALL SELECT 3101, 23103 UNION ALL SELECT 3101, 23104 UNION ALL SELECT 3101, 23105 UNION ALL SELECT 3101, 23106 UNION ALL SELECT 3101, 23119
    UNION ALL SELECT 3102, 23000 UNION ALL SELECT 3102, 23001 UNION ALL SELECT 3102, 23101 UNION ALL SELECT 3102, 23107 UNION ALL SELECT 3102, 23109
    UNION ALL SELECT 3103, 23000 UNION ALL SELECT 3103, 23001 UNION ALL SELECT 3103, 23101 UNION ALL SELECT 3103, 23108 UNION ALL SELECT 3103, 23109 UNION ALL SELECT 3103, 23110 UNION ALL SELECT 3103, 23115 UNION ALL SELECT 3103, 23116 UNION ALL SELECT 3103, 23117 UNION ALL SELECT 3103, 23118 UNION ALL SELECT 3103, 23119 UNION ALL SELECT 3103, 23120 UNION ALL SELECT 3103, 23121 UNION ALL SELECT 3103, 23125 UNION ALL SELECT 3103, 23126 UNION ALL SELECT 3103, 23127
    UNION ALL SELECT 3104, 23000 UNION ALL SELECT 3104, 23001 UNION ALL SELECT 3104, 23101 UNION ALL SELECT 3104, 23111
    UNION ALL SELECT 3105, 23000 UNION ALL SELECT 3105, 23001 UNION ALL SELECT 3105, 23101 UNION ALL SELECT 3105, 23112
    UNION ALL SELECT 3106, 23000 UNION ALL SELECT 3106, 23001 UNION ALL SELECT 3106, 23101 UNION ALL SELECT 3106, 23113 UNION ALL SELECT 3106, 23114 UNION ALL SELECT 3106, 23122 UNION ALL SELECT 3106, 23123 UNION ALL SELECT 3106, 23124
) role_menu
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = role_menu.role_id AND rm.menu_id = role_menu.menu_id
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT roles.role_id, menus.menu_id
FROM (
    SELECT 3101 AS role_id
    UNION ALL SELECT 3102
    UNION ALL SELECT 3103
    UNION ALL SELECT 3104
    UNION ALL SELECT 3105
    UNION ALL SELECT 3106
) roles
JOIN (
    SELECT 22000 AS menu_id UNION ALL SELECT 22001 UNION ALL SELECT 22100 UNION ALL SELECT 22101
    UNION ALL SELECT 22200 UNION ALL SELECT 22201 UNION ALL SELECT 22300
    UNION ALL SELECT 22400 UNION ALL SELECT 22401 UNION ALL SELECT 22500
) menus ON 1 = 1
WHERE EXISTS (SELECT 1 FROM sys_menu WHERE sys_menu.menu_id = menus.menu_id)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = roles.role_id AND rm.menu_id = menus.menu_id
  );

INSERT IGNORE INTO daily_violation_batch
    (batch_id, tenant_id, report_date, report_title, source_type, batch_status, total_rows, valid_rows, warning_rows, invalid_rows, submitted_rows, del_flag, remark, create_dept, create_by, create_time)
VALUES
    (5100000000000000001, '000000', '2026-06-12', '货运车间违标问题登记簿（6月12日）', 'MANUAL', 'DRAFT', 1, 1, 0, 0, 0, '0', 'phase7 sample batch', 103, 1, sysdate());

INSERT IGNORE INTO daily_violation_record
    (record_id, tenant_id, batch_id, report_date, violation_date, violation_time, sequence_no, violation_code, violation_nature_snapshot,
     violation_category_snapshot, violation_type_snapshot, proposed_assessment_content, responsible_dept_id, responsible_dept_name_snapshot,
     responsible_person_id, employee_no, employee_name_snapshot, locomotive, train_no, location, time_segment, issuing_dept,
     current_status, version, source_type, preview_confirmed, validation_status, validation_message, del_flag, remark, create_dept, create_by, create_time)
VALUES
    (5100000000000000101, '000000', 5100000000000000001, '2026-06-12', '2026-06-09', '13时15分10秒', '1', 'QBJ028', '建议轻微违标',
     '乘务员两标', '其他', '6月9日，货运车间司机示例，使用HXD1B-0378机车，41096次，13时15分10秒，示例违标内容。', 103, '研发部门',
     NULL, '100001', '张三', 'HXD1B-0378', '41096次', '开封运转场', '', '分析室',
     'DRAFT', 1, 'MANUAL', '1', 'VALID', 'phase7 sample record', '0', 'phase7 sample record', 103, 1, sysdate());
