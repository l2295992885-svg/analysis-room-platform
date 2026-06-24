-- 003_daily_violation_tables.sql
-- 每日 LKJ 音视频违标公示业务表草案。
-- 数据库：MySQL 8.x

CREATE TABLE IF NOT EXISTS violation_code_dict (
  code_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编码ID',
  violation_code VARCHAR(100) NOT NULL COMMENT '违标编码',
  code_name VARCHAR(255) NOT NULL COMMENT '违标名称',
  nature VARCHAR(50) DEFAULT NULL COMMENT '性质',
  category VARCHAR(100) DEFAULT NULL COMMENT '类别',
  violation_type VARCHAR(100) DEFAULT NULL COMMENT '类型',
  source_category VARCHAR(50) DEFAULT NULL COMMENT '来源：LKJ/video/audio',
  version_no VARCHAR(50) DEFAULT 'v1' COMMENT '版本号',
  effective_date DATE DEFAULT NULL COMMENT '生效日期',
  expiry_date DATE DEFAULT NULL COMMENT '失效日期',
  status CHAR(1) DEFAULT '0' COMMENT '0启用 1停用',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_violation_code_version (violation_code, version_no),
  KEY idx_violation_code (violation_code),
  KEY idx_violation_code_status (status)
) COMMENT='违标编码字典表';

CREATE TABLE IF NOT EXISTS daily_violation_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '违章记录ID',
  report_date DATE NOT NULL COMMENT '提报日期',
  violation_date DATE NOT NULL COMMENT '违章发生日期',
  violation_time TIME DEFAULT NULL COMMENT '违章发生时间',
  source_type VARCHAR(50) DEFAULT NULL COMMENT '来源类型：LKJ/video/audio',
  responsible_dept_id BIGINT DEFAULT NULL COMMENT '责任部门ID',
  responsible_dept_name VARCHAR(100) DEFAULT NULL COMMENT '责任部门快照',
  employee_no VARCHAR(64) NOT NULL COMMENT '工号',
  employee_name VARCHAR(64) NOT NULL COMMENT '责任人姓名快照',
  locomotive_no VARCHAR(64) DEFAULT NULL COMMENT '机车',
  train_no VARCHAR(64) DEFAULT NULL COMMENT '车次',
  location VARCHAR(255) DEFAULT NULL COMMENT '地点',
  violation_code VARCHAR(100) NOT NULL COMMENT '违标编码快照',
  code_name VARCHAR(255) DEFAULT NULL COMMENT '违标名称快照',
  nature VARCHAR(50) DEFAULT NULL COMMENT '性质快照',
  category VARCHAR(100) DEFAULT NULL COMMENT '类别快照',
  violation_type VARCHAR(100) DEFAULT NULL COMMENT '类型快照',
  code_version VARCHAR(50) DEFAULT NULL COMMENT '编码版本快照',
  assessment_content TEXT NOT NULL COMMENT '拟考核内容',
  current_status VARCHAR(50) NOT NULL DEFAULT 'draft' COMMENT '当前流程状态',
  current_node VARCHAR(50) DEFAULT NULL COMMENT '当前流程节点',
  submitter_id BIGINT DEFAULT NULL COMMENT '提报人ID',
  submitter_name VARCHAR(64) DEFAULT NULL COMMENT '提报人姓名',
  submit_dept_id BIGINT DEFAULT NULL COMMENT '提报部门ID',
  submit_time DATETIME DEFAULT NULL COMMENT '提交时间',
  final_confirm_user_id BIGINT DEFAULT NULL COMMENT '最终确认人ID',
  final_confirm_time DATETIME DEFAULT NULL COMMENT '最终确认时间',
  result_in_time DATETIME DEFAULT NULL COMMENT '入结果库时间',
  import_batch_id BIGINT DEFAULT NULL COMMENT '导入批次ID',
  raw_row_id BIGINT DEFAULT NULL COMMENT '原始导入行ID',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_daily_violation_date (violation_date),
  KEY idx_daily_violation_employee (employee_no),
  KEY idx_daily_violation_code (violation_code),
  KEY idx_daily_violation_status (current_status),
  KEY idx_daily_violation_dept (responsible_dept_id)
) COMMENT='每日LKJ音视频违标记录表';

CREATE TABLE IF NOT EXISTS daily_violation_raw_row (
  raw_row_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '原始行ID',
  import_batch_id BIGINT NOT NULL COMMENT '导入批次ID',
  row_no INT NOT NULL COMMENT 'Excel行号',
  raw_json JSON NOT NULL COMMENT '原始行JSON',
  validate_status VARCHAR(30) DEFAULT 'pending' COMMENT 'pass/warning/failed',
  validate_message TEXT COMMENT '校验信息',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_raw_row_batch (import_batch_id),
  KEY idx_raw_row_status (validate_status)
) COMMENT='每日违标导入原始行表';

CREATE TABLE IF NOT EXISTS daily_violation_feedback (
  feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
  record_id BIGINT NOT NULL COMMENT '违章记录ID',
  feedback_type VARCHAR(50) NOT NULL COMMENT '反馈类型：not_true/need_more/support_original',
  reason_type VARCHAR(100) DEFAULT NULL COMMENT '原因类型',
  reason_detail TEXT NOT NULL COMMENT '原因说明',
  feedback_user_id BIGINT NOT NULL COMMENT '反馈人ID',
  feedback_user_name VARCHAR(64) DEFAULT NULL COMMENT '反馈人姓名',
  feedback_dept_id BIGINT DEFAULT NULL COMMENT '反馈部门ID',
  feedback_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
  handle_status VARCHAR(30) DEFAULT 'pending' COMMENT 'pending/handled/returned',
  handle_user_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handle_user_name VARCHAR(64) DEFAULT NULL COMMENT '处理人姓名',
  handle_opinion TEXT COMMENT '处理意见',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  KEY idx_feedback_record (record_id),
  KEY idx_feedback_status (handle_status)
) COMMENT='每日违标不属实反馈表';

CREATE TABLE IF NOT EXISTS daily_violation_result (
  result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果库ID',
  record_id BIGINT NOT NULL COMMENT '来源记录ID',
  result_version INT NOT NULL DEFAULT 1 COMMENT '结果版本',
  result_status VARCHAR(30) NOT NULL DEFAULT 'valid' COMMENT 'valid/cancelled/revised',
  result_snapshot JSON NOT NULL COMMENT '入库快照JSON',
  in_user_id BIGINT NOT NULL COMMENT '入库人ID',
  in_user_name VARCHAR(64) DEFAULT NULL COMMENT '入库人姓名',
  in_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  revise_reason TEXT COMMENT '修订原因',
  UNIQUE KEY uk_result_record_version (record_id, result_version),
  KEY idx_result_status (result_status),
  KEY idx_result_time (in_time)
) COMMENT='每日违标结果库表';
