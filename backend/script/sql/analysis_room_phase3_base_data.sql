-- Phase 3: base data module for analysis room platform.
-- This script is additive and idempotent for local development.

create table if not exists base_personnel (
    id bigint(20) not null comment '主键',
    job_no varchar(32) not null comment '工号',
    person_name varchar(64) not null comment '姓名',
    dept_id bigint(20) default null comment '关联系统部门ID',
    dept_name varchar(100) default '' comment '责任部门',
    workshop varchar(100) default '' comment '车间',
    team_name varchar(100) default '' comment '车队',
    guide_group varchar(100) default '' comment '指导组',
    position_name varchar(100) default '' comment '岗位',
    phone varchar(32) default '' comment '联系电话',
    status char(1) default '0' comment '状态（0正常 1停用）',
    del_flag char(1) default '0' comment '删除标志（0存在 1删除）',
    remark varchar(500) default null comment '备注',
    create_dept bigint(20) default null comment '创建部门',
    create_by bigint(20) default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by bigint(20) default null comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    key idx_base_personnel_job_no (job_no),
    key idx_base_personnel_dept (dept_id),
    key idx_base_personnel_status (status),
    key idx_base_personnel_del_flag (del_flag)
) engine=innodb comment='人员基础数据';

create table if not exists base_org_unit (
    id bigint(20) not null comment '主键',
    parent_id bigint(20) default 0 comment '父级组织ID',
    org_code varchar(64) not null comment '组织编码',
    org_name varchar(100) not null comment '组织名称',
    org_type varchar(32) not null comment '组织类型',
    leader_name varchar(64) default '' comment '负责人',
    sort_order int(4) default 0 comment '排序',
    status char(1) default '0' comment '状态（0正常 1停用）',
    del_flag char(1) default '0' comment '删除标志（0存在 1删除）',
    remark varchar(500) default null comment '备注',
    create_dept bigint(20) default null comment '创建部门',
    create_by bigint(20) default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by bigint(20) default null comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    key idx_base_org_parent (parent_id),
    key idx_base_org_code (org_code),
    key idx_base_org_type (org_type),
    key idx_base_org_status (status),
    key idx_base_org_del_flag (del_flag)
) engine=innodb comment='组织基础数据';

create table if not exists base_violation_code (
    id bigint(20) not null comment '主键',
    violation_code varchar(64) not null comment '违标编码',
    violation_name varchar(200) not null comment '违标名称',
    nature varchar(64) not null comment '性质',
    category varchar(64) not null comment '类别',
    violation_type varchar(64) not null comment '类型',
    description varchar(500) default null comment '说明',
    status char(1) default '0' comment '状态（0正常 1停用）',
    del_flag char(1) default '0' comment '删除标志（0存在 1删除）',
    remark varchar(500) default null comment '备注',
    create_dept bigint(20) default null comment '创建部门',
    create_by bigint(20) default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by bigint(20) default null comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    key idx_base_violation_code (violation_code),
    key idx_base_violation_nature (nature),
    key idx_base_violation_category (category),
    key idx_base_violation_type (violation_type),
    key idx_base_violation_status (status),
    key idx_base_violation_del_flag (del_flag)
) engine=innodb comment='违标编码基础数据';

create table if not exists base_import_template (
    id bigint(20) not null comment '主键',
    template_code varchar(64) not null comment '模板编码',
    template_name varchar(100) not null comment '模板名称',
    business_type varchar(64) not null comment '业务类型',
    version_no varchar(32) default '1.0' comment '版本号',
    file_name varchar(255) default '' comment '模板文件名',
    file_oss_id bigint(20) default null comment '文件OSS ID',
    status char(1) default '0' comment '状态（0正常 1停用）',
    del_flag char(1) default '0' comment '删除标志（0存在 1删除）',
    remark varchar(500) default null comment '备注',
    create_dept bigint(20) default null comment '创建部门',
    create_by bigint(20) default null comment '创建者',
    create_time datetime comment '创建时间',
    update_by bigint(20) default null comment '更新者',
    update_time datetime comment '更新时间',
    primary key (id),
    key idx_base_template_code (template_code),
    key idx_base_template_business (business_type),
    key idx_base_template_status (status),
    key idx_base_template_del_flag (del_flag)
) engine=innodb comment='导入模板基础数据';

insert ignore into base_org_unit values
(3000000000000000001, 0, 'ANALYSIS_ROOM', '综合分析室', 'ANALYSIS_ROOM', '主任', 1, '0', '0', '平台样例组织', 103, 1, sysdate(), null, null),
(3000000000000000002, 3000000000000000001, 'WORKSHOP_A', '示例车间', 'WORKSHOP', '车间负责人', 1, '0', '0', '阶段3样例车间', 103, 1, sysdate(), null, null),
(3000000000000000003, 3000000000000000002, 'TEAM_A', '示例车队', 'TEAM', '车队负责人', 1, '0', '0', '阶段3样例车队', 103, 1, sysdate(), null, null),
(3000000000000000004, 3000000000000000003, 'GUIDE_A', '示例指导组', 'GUIDE_GROUP', '指导组负责人', 1, '0', '0', '阶段3样例指导组', 103, 1, sysdate(), null, null);

insert ignore into base_personnel values
(3000000000000000101, '100001', '张三', 103, '研发部门', '示例车间', '示例车队', '示例指导组', '司机', '', '0', '0', '阶段3样例人员', 103, 1, sysdate(), null, null),
(3000000000000000102, '100002', '李四', 103, '研发部门', '示例车间', '示例车队', '示例指导组', '指导司机', '', '0', '0', '阶段3样例人员', 103, 1, sysdate(), null, null);

insert ignore into base_violation_code values
(3000000000000000201, 'LKJ-001', '未按规定确认信号', 'A类', 'LKJ', '作业违标', '用于每日 LKJ 样板业务的编码校验样例', '0', '0', '阶段3样例违标编码', 103, 1, sysdate(), null, null),
(3000000000000000202, 'AV-001', '音视频抽查违标', 'B类', '音视频', '行为违标', '用于每日音视频样板业务的编码校验样例', '0', '0', '阶段3样例违标编码', 103, 1, sysdate(), null, null);

insert ignore into base_import_template values
(3000000000000000301, 'DAILY_LKJ_IMPORT', '每日 LKJ 音视频违标导入模板', 'DAILY_LKJ_VIOLATION', '1.0', '', null, '0', '0', '阶段3仅登记模板元数据，文件上传留到文件中心阶段', 103, 1, sysdate(), null, null);

insert ignore into sys_menu values('20000', '基础数据', '0', '6', 'base', null, '', 1, 0, 'M', '0', '0', '', 'dict', 103, 1, sysdate(), null, null, '基础数据目录');
insert ignore into sys_menu values('20001', '人员基础数据', '20000', '1', 'personnel', 'base/personnel/index', '', 1, 0, 'C', '0', '0', 'base:personnel:list', 'user', 103, 1, sysdate(), null, null, '人员基础数据菜单');
insert ignore into sys_menu values('20002', '组织基础数据', '20000', '2', 'org', 'base/org/index', '', 1, 0, 'C', '0', '0', 'base:org:list', 'tree', 103, 1, sysdate(), null, null, '组织基础数据菜单');
insert ignore into sys_menu values('20003', '违标编码', '20000', '3', 'violationCode', 'base/violationCode/index', '', 1, 0, 'C', '0', '0', 'base:violationCode:list', 'dict', 103, 1, sysdate(), null, null, '违标编码菜单');
insert ignore into sys_menu values('20004', '导入模板', '20000', '4', 'importTemplate', 'base/importTemplate/index', '', 1, 0, 'C', '0', '0', 'base:importTemplate:list', 'excel', 103, 1, sysdate(), null, null, '导入模板菜单');

insert ignore into sys_menu values('20101', '人员查询', '20001', '1', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:query', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20102', '人员新增', '20001', '2', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:add', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20103', '人员修改', '20001', '3', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:edit', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20104', '人员删除', '20001', '4', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:remove', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20105', '人员导出', '20001', '5', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:export', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20106', '人员导入', '20001', '6', '#', '', '', 1, 0, 'F', '0', '0', 'base:personnel:import', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu values('20201', '组织查询', '20002', '1', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:query', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20202', '组织新增', '20002', '2', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:add', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20203', '组织修改', '20002', '3', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:edit', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20204', '组织删除', '20002', '4', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:remove', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20205', '组织导出', '20002', '5', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:export', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20206', '组织导入', '20002', '6', '#', '', '', 1, 0, 'F', '0', '0', 'base:org:import', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu values('20301', '违标编码查询', '20003', '1', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:query', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20302', '违标编码新增', '20003', '2', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:add', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20303', '违标编码修改', '20003', '3', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:edit', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20304', '违标编码删除', '20003', '4', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:remove', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20305', '违标编码导出', '20003', '5', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:export', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20306', '违标编码导入', '20003', '6', '#', '', '', 1, 0, 'F', '0', '0', 'base:violationCode:import', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_menu values('20401', '导入模板查询', '20004', '1', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:query', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20402', '导入模板新增', '20004', '2', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:add', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20403', '导入模板修改', '20004', '3', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:edit', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20404', '导入模板删除', '20004', '4', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:remove', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20405', '导入模板导出', '20004', '5', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:export', '#', 103, 1, sysdate(), null, null, '');
insert ignore into sys_menu values('20406', '导入模板导入', '20004', '6', '#', '', '', 1, 0, 'F', '0', '0', 'base:importTemplate:import', '#', 103, 1, sysdate(), null, null, '');

insert ignore into sys_role_menu(role_id, menu_id)
select 3, menu_id from sys_menu where menu_id between 20000 and 20406;
