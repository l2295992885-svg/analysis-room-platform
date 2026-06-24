-- 002_business_common_tables.sql
-- 公共业务能力表草案：信箱、待办、附件、业务日志、导入导出。
-- 数据库：MySQL 8.x

CREATE TABLE IF NOT EXISTS biz_task (
  task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '待办ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  business_id BIGINT NOT NULL COMMENT '业务记录ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  task_title VARCHAR(200) NOT NULL COMMENT '待办标题',
  task_status VARCHAR(30) NOT NULL DEFAULT 'pending' COMMENT 'pending/done/cancelled',
  current_node VARCHAR(50) DEFAULT NULL COMMENT '当前节点',
  receiver_user_id BIGINT DEFAULT NULL COMMENT '接收人ID',
  receiver_dept_id BIGINT DEFAULT NULL COMMENT '接收部门ID',
  receiver_role_key VARCHAR(64) DEFAULT NULL COMMENT '接收角色',
  sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
  sender_dept_id BIGINT DEFAULT NULL COMMENT '发送部门ID',
  due_time DATETIME DEFAULT NULL COMMENT '截止时间',
  finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_biz_task_receiver (receiver_user_id, receiver_dept_id, receiver_role_key),
  KEY idx_biz_task_business (business_type, business_id),
  KEY idx_biz_task_status (task_status)
) COMMENT='统一待办任务表';

CREATE TABLE IF NOT EXISTS biz_message (
  message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信箱消息ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  business_id BIGINT NOT NULL COMMENT '业务记录ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  message_type VARCHAR(50) NOT NULL COMMENT 'submit/audit/return/dispatch/feedback/cc/done',
  message_title VARCHAR(200) NOT NULL COMMENT '消息标题',
  message_content TEXT COMMENT '消息内容',
  sender_user_id BIGINT DEFAULT NULL COMMENT '发送人ID',
  sender_name VARCHAR(64) DEFAULT NULL COMMENT '发送人姓名',
  receiver_user_id BIGINT DEFAULT NULL COMMENT '接收人ID',
  receiver_dept_id BIGINT DEFAULT NULL COMMENT '接收部门ID',
  receiver_role_key VARCHAR(64) DEFAULT NULL COMMENT '接收角色',
  read_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_biz_message_receiver (receiver_user_id, receiver_dept_id, receiver_role_key),
  KEY idx_biz_message_business (business_type, business_id),
  KEY idx_biz_message_read (read_flag)
) COMMENT='统一信箱消息表';

CREATE TABLE IF NOT EXISTS biz_attachment (
  attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  business_id BIGINT NOT NULL COMMENT '业务记录ID',
  action_log_id BIGINT DEFAULT NULL COMMENT '关联业务日志ID',
  file_name VARCHAR(255) NOT NULL COMMENT '原文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
  file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  file_size BIGINT DEFAULT 0 COMMENT '文件大小',
  uploader_id BIGINT NOT NULL COMMENT '上传人ID',
  uploader_name VARCHAR(64) DEFAULT NULL COMMENT '上传人姓名',
  upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志'
) COMMENT='统一附件表';

CREATE TABLE IF NOT EXISTS biz_action_log (
  action_log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '业务日志ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  business_id BIGINT NOT NULL COMMENT '业务记录ID',
  batch_id BIGINT DEFAULT NULL COMMENT '批次ID',
  operator_id BIGINT NOT NULL COMMENT '操作人ID',
  operator_name VARCHAR(64) NOT NULL COMMENT '操作人姓名',
  operator_role_key VARCHAR(64) DEFAULT NULL COMMENT '操作角色',
  operator_dept_id BIGINT DEFAULT NULL COMMENT '操作部门ID',
  action_type VARCHAR(50) NOT NULL COMMENT '操作类型',
  action_summary VARCHAR(500) DEFAULT NULL COMMENT '操作摘要',
  before_status VARCHAR(50) DEFAULT NULL COMMENT '操作前状态',
  after_status VARCHAR(50) DEFAULT NULL COMMENT '操作后状态',
  ip_addr VARCHAR(128) DEFAULT NULL COMMENT 'IP地址',
  attachment_count INT DEFAULT 0 COMMENT '附件数量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_biz_action_business (business_type, business_id),
  KEY idx_biz_action_operator (operator_id),
  KEY idx_biz_action_time (create_time)
) COMMENT='业务动作日志表';

CREATE TABLE IF NOT EXISTS biz_action_log_detail (
  detail_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志明细ID',
  action_log_id BIGINT NOT NULL COMMENT '业务日志ID',
  field_name VARCHAR(100) NOT NULL COMMENT '字段名',
  field_label VARCHAR(100) DEFAULT NULL COMMENT '字段中文名',
  before_value TEXT COMMENT '修改前值',
  after_value TEXT COMMENT '修改后值'
) COMMENT='业务字段变更日志明细表';

CREATE TABLE IF NOT EXISTS biz_import_batch (
  import_batch_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导入批次ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  template_code VARCHAR(100) DEFAULT NULL COMMENT '模板编码',
  original_file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
  total_rows INT DEFAULT 0 COMMENT '总行数',
  success_rows INT DEFAULT 0 COMMENT '成功行数',
  warning_rows INT DEFAULT 0 COMMENT '需确认行数',
  failed_rows INT DEFAULT 0 COMMENT '失败行数',
  import_user_id BIGINT NOT NULL COMMENT '导入人ID',
  import_user_name VARCHAR(64) DEFAULT NULL COMMENT '导入人姓名',
  import_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',
  status VARCHAR(30) DEFAULT 'preview' COMMENT 'preview/submitted/cancelled'
) COMMENT='导入批次表';

CREATE TABLE IF NOT EXISTS biz_export_record (
  export_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导出记录ID',
  business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
  export_page VARCHAR(100) DEFAULT NULL COMMENT '导出页面',
  export_condition TEXT COMMENT '导出条件JSON',
  export_count INT DEFAULT 0 COMMENT '导出数量',
  file_name VARCHAR(255) DEFAULT NULL COMMENT '导出文件名',
  exporter_id BIGINT NOT NULL COMMENT '导出人ID',
  exporter_name VARCHAR(64) DEFAULT NULL COMMENT '导出人姓名',
  export_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
  ip_addr VARCHAR(128) DEFAULT NULL COMMENT 'IP地址'
) COMMENT='导出记录表';
