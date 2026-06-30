# 数据库设计规范与现有 SQL 草案审查

本文定义“综合分析室数据分析平台”的数据库设计规范，并基于 `database/001_system_tables.sql`、`database/002_business_common_tables.sql`、`database/003_daily_violation_tables.sql` 指出现有 SQL 草案的调整方向。

本文只定义规范和审查意见，不修改 SQL，不包含代码实现。

## 1. 数据库选择

| 项目 | 规范 |
| --- | --- |
| 第一阶段数据库 | MySQL 8.x |
| 迁移预留 | 设计上避免过度绑定 MySQL 特性，保留未来 PostgreSQL 可能 |
| 字符集 | 建议统一 `utf8mb4` |
| 排序规则 | 第一阶段可使用 MySQL 默认业务适配规则，后续需统一确定 |
| 迁移方式 | 后续建议使用可重复执行的迁移脚本或 Flyway / Liquibase 类工具 |

设计原则：

1. 表结构应优先使用通用 SQL 类型，减少数据库方言依赖。
2. JSON 字段仅用于快照、原始导入行、字段变更、扩展信息，不用于高频查询。
3. 主业务查询字段必须拆为普通列，并建立必要索引。
4. 不依赖数据库触发器实现核心业务逻辑，业务状态流转必须由后端服务控制。

## 2. 表命名规范

| 前缀 | 范围 | 示例 |
| --- | --- | --- |
| `sys_` | 系统权限、用户、角色、部门、菜单、登录日志、系统操作日志 | `sys_user`、`sys_role`、`sys_dept` |
| `base_` | 基础数据、人员、组织、违标编码、导入模板 | `base_person`、`base_org`、`base_violation_code` |
| `biz_` | 平台公共业务能力、信箱、待办、文件、业务日志、导入导出 | `biz_task`、`biz_message`、`biz_file` |
| `daily_violation_` | 每日 LKJ 音视频违标公示业务表 | `daily_violation_record`、`daily_violation_result` |

命名规则：

1. 表名使用小写英文和下划线。
2. 不使用中文、拼音混写或含义不稳定的缩写。
3. 每张表的主键建议统一命名为 `id`；如系统表保留 `user_id`、`role_id` 等兼容风格，必须在代码和文档中保持一致。
4. 外键字段使用 `{business_name}_id` 或 `{table_name_without_prefix}_id`，例如 `record_id`、`batch_id`、`file_id`。
5. 状态字段统一使用 `status` 或更具体的 `current_status`，值必须是稳定英文 code。

## 3. 通用字段规范

除关联表、日志明细表等极简表外，业务表、公共业务表、基础数据表建议包含以下通用字段：

| 字段 | 类型建议 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | BIGINT | 是 | 主键 |
| `created_by` | BIGINT / VARCHAR | 否 | 创建人 ID 或账号；最终需统一 |
| `created_time` | DATETIME | 是 | 创建时间 |
| `updated_by` | BIGINT / VARCHAR | 否 | 更新人 ID 或账号；最终需统一 |
| `updated_time` | DATETIME | 是 | 更新时间 |
| `deleted` | TINYINT / BOOLEAN | 是 | 逻辑删除标记，0 未删除，1 已删除 |
| `remark` | VARCHAR / TEXT | 否 | 备注 |

调整方向：

1. 当前 SQL 中存在 `create_by`、`create_time`、`update_by`、`update_time`、`del_flag`，建议后续统一为 `created_by`、`created_time`、`updated_by`、`updated_time`、`deleted`。
2. 如果为了兼容若依风格保留 `create_by` / `del_flag`，必须全库统一，不要混用。
3. 业务表建议使用 `created_by` 保存用户 ID，另用快照字段保存操作人姓名。
4. 日志表必须保存操作人快照，不能只保存用户 ID。

## 4. 逻辑删除规范

| 数据类型 | 删除策略 |
| --- | --- |
| 业务历史数据 | 不允许物理删除，使用 `deleted` 标记 |
| 附件元数据 | 不允许物理删除，使用 `deleted` 标记，并保留对象存储引用 |
| 业务日志 | 不允许物理删除 |
| 字段变更日志 | 不允许物理删除 |
| 结果库 | 不允许物理删除 |
| 导入原始行 | 不允许物理删除 |
| 系统配置类数据 | 可逻辑删除；是否允许物理清理需单独授权 |

规则：

1. 删除优先使用 `deleted` 标记。
2. 删除业务草稿也应保留必要操作记录。
3. 附件、日志、结果库、导入原始行不得物理删除。
4. 查询默认过滤 `deleted = 0`，但审计、日志和历史查询应能查看已删除历史。
5. 物理清理如后续需要，必须作为离线归档策略单独设计，不属于第一阶段 MVP。

## 5. 状态字段规范

| 规范项 | 要求 |
| --- | --- |
| 数据库存储 | 使用稳定英文 code |
| 展示名称 | 由前端、字典或后端枚举转换展示 |
| 禁止事项 | 不使用中文状态作为数据库值 |
| 对齐来源 | 必须与 `docs/STATE_TRANSITION_MATRIX.md` 对齐 |
| 变更方式 | 状态只能由后端状态机动作改变 |

每日 LKJ 状态 code 应对齐状态矩阵，例如：

| 中文状态 | 建议 code |
| --- | --- |
| 草稿 | `DRAFT` |
| 分析员已提交 | `ANALYST_SUBMITTED` |
| 班长待审核 | `LEADER_PENDING` |
| 主任待审核 | `DIRECTOR_PENDING` |
| 主任待下发车间 | `DIRECTOR_APPROVED_PENDING_DISPATCH` |
| 主任已下发车间 | `DIRECTOR_DISPATCHED_WORKSHOP` |
| 车间待确认 | `WORKSHOP_PENDING` |
| 车队待确认 | `TEAM_PENDING` |
| 指导组待确认 | `GUIDE_PENDING` |
| 指导组确认无误 | `GUIDE_CONFIRMED` |
| 指导组反馈不属实 | `GUIDE_REJECTED` |
| 返回主任待复核 | `RETURNED_DIRECTOR_RECHECK` |
| 主任最终确认 | `FINAL_CONFIRMED` |
| 已入结果库 | `ARCHIVED` |
| 已撤销不计入 | `CANCELLED_EXCLUDED` |

## 6. 快照字段规范

以下场景必须保存快照：

| 快照类型 | 触发场景 | 建议保存内容 |
| --- | --- | --- |
| 人员信息快照 | 新增、导入、提交、入结果库 | 工号、姓名、人员 ID、所属组织、岗位或角色 |
| 部门/组织快照 | 新增、导入、下发、入结果库 | 部门 ID、部门名称、车间、车队、指导组层级 |
| 违标编码快照 | 选择编码、提交、入结果库 | 编码、名称、性质、类别、类型、版本 |
| Excel 原始行 JSON 快照 | Excel 导入 | 原始单元格值、行号、模板版本 |
| 入结果库快照 | 主任最终确认后入库 | 业务记录、人员、组织、编码、附件引用、流程摘要 |
| 字段变更前后快照 | 关键字段修改、授权覆盖、更正 | 字段名、修改前、修改后、原因、操作人 |

规则：

1. 快照用于保留历史事实，不能以后续基础数据变化覆盖。
2. 快照字段可以使用 JSON 保存完整副本，但高频查询字段仍必须拆列。
3. 入结果库快照必须足以还原当时业务记录、流程、附件和编码认定。
4. 字段变更日志必须记录修改原因和是否授权覆盖。

## 7. JSON 字段使用原则

| 允许使用 JSON 的场景 | 示例 |
| --- | --- |
| 快照 | `result_snapshot`、人员快照、组织快照、编码快照 |
| 原始导入行 | `raw_json` |
| 字段变更 | `changed_fields_json`、字段变更前后值 |
| 扩展信息 | 低频读取、无需作为筛选条件的扩展属性 |

禁止或不建议：

1. 不把高频查询条件只放在 JSON 中。
2. 不用 JSON 存放状态、提报日期、责任部门、工号、车间、车队、指导组等筛选字段。
3. 不依赖 MySQL JSON 函数完成核心业务查询。
4. 如未来迁移 PostgreSQL，JSON 字段应能平滑映射到 `jsonb` 或文本快照。

## 8. 索引规范

必须考虑索引的字段：

| 字段类型 | 示例字段 |
| --- | --- |
| 状态 | `status`、`current_status` |
| 批次 ID | `batch_id`、`import_batch_id` |
| 责任部门 | `responsible_dept_id` |
| 责任人/工号 | `employee_no`、`employee_id` |
| 车间/车队/指导组 | `workshop_id`、`team_id`、`guide_group_id` |
| 创建时间 | `created_time` |
| 提报日期 | `report_date` |
| 违章发生日期 | `violation_date` |
| 结果库统计字段 | `result_status`、`included_flag`、`result_version` |
| 逻辑删除 | `deleted` |

索引规则：

1. 业务列表页的常用筛选字段应建组合索引。
2. 数据权限字段应建索引，例如部门、创建人、接收角色、接收部门。
3. 逻辑删除字段可与状态、时间、部门组合建索引。
4. 不为低选择性字段单独滥建索引，优先结合实际查询组合。
5. 导入原始行应按 `import_batch_id + validate_status` 建组合索引。
6. 结果库应按 `record_id + result_version` 保持唯一约束。

## 9. 文件中心设计原则

文件中心应将文件元数据和业务绑定关系分离。

| 表类型 | 作用 | 示例 |
| --- | --- | --- |
| 文件元数据表 | 保存物理文件或对象存储信息 | `biz_file` |
| 业务绑定表 | 保存文件与业务记录、业务动作的关系 | `biz_file_bind` |

文件元数据表建议字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 文件 ID |
| `storage_type` | `local`、`nas`、`minio` |
| `bucket` | 对象存储桶或逻辑目录 |
| `object_key` | 对象 key 或文件相对路径 |
| `original_name` | 原始文件名 |
| `content_type` | MIME 类型 |
| `size` | 文件大小 |
| `hash` | 文件哈希，用于去重和校验 |
| `upload_user` | 上传人 |
| `permission_scope` | 权限范围 |
| `created_time` | 上传时间 |
| `deleted` | 逻辑删除 |

业务绑定表建议字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 绑定 ID |
| `file_id` | 文件 ID |
| `business_type` | 业务类型 |
| `business_id` | 业务记录 ID |
| `batch_id` | 批次 ID |
| `action_type` | 上传时对应业务动作 |
| `action_log_id` | 关联业务日志 |
| `created_by` | 绑定人 |
| `created_time` | 绑定时间 |

第一阶段可支持本地磁盘或 NAS，设计上保留 `storage_type`、`bucket`、`object_key`，后续兼容 MinIO。

## 10. 日志证据链设计原则

业务日志独立于技术操作日志。

| 日志类型 | 作用 |
| --- | --- |
| 系统操作日志 | 记录接口、请求、响应、耗时、异常等技术审计 |
| 业务证据链日志 | 记录业务动作、流程状态、人员快照、意见、附件和字段变更 |

业务证据链日志必须记录：

| 字段 | 说明 |
| --- | --- |
| `business_type` | 业务类型 |
| `business_id` | 业务 ID |
| `batch_id` | 批次 ID |
| `action_type` | 动作 |
| `before_status` | 前状态 |
| `after_status` | 后状态 |
| `operator_id` | 操作人 ID |
| `operator_name_snapshot` | 操作人姓名快照 |
| `operator_dept_snapshot` | 操作部门快照 |
| `operator_role_snapshot` | 操作角色快照 |
| `opinion` | 审核、退回、反馈、复核、最终确认意见 |
| `attachment_refs` | 附件引用 |
| `changed_fields_json` | 字段变更 JSON |
| `created_time` | 操作时间 |

规则：

1. 所有提交、审核、退回、下发、确认、反馈、上传、下载、导出、入库、撤销都必须写业务日志。
2. 没有完整日志证据链的数据不得进入结果库。
3. 日志不得物理删除。
4. 字段变更明细可以单独明细表保存，也可以在日志中保留 JSON 摘要；高风险字段建议两者都保留。

## 11. Excel 导入设计原则

| 规则 | 要求 |
| --- | --- |
| 原始文件 | 必须保存原始 Excel 文件 |
| 原始行 | 每一行必须保存原始 JSON |
| staging / 预览 | 预览表与正式业务表分离 |
| 校验状态 | 校验通过、需要确认、校验失败分开记录 |
| 失败数据 | 校验失败不得提交 |
| 疑似重复 | 只提示，不自动删除 |
| 错误报告 | 支持下载错误报告，并记录导出日志 |

建议表结构方向：

| 表 | 作用 |
| --- | --- |
| `biz_import_batch` | 通用导入批次 |
| `daily_violation_import_row` | 每日违标导入预览行 |
| `daily_violation_import_error` | 每行每字段错误明细 |
| `daily_violation_record` | 校验通过并提交后的正式业务记录 |

导入流程：

```text
保存原始文件
→ 保存导入批次
→ 保存行级 JSON 快照
→ 行级校验
→ staging / 预览
→ 勾选 1-N 条提交
→ 写入正式业务表
```

## 12. 结果库设计原则

| 规则 | 要求 |
| --- | --- |
| 表分离 | 结果库表与流程业务表分离 |
| 入库快照 | 入库时保存完整快照 |
| 历史保护 | 后续修改不能覆盖历史 |
| 版本支持 | 结果库支持版本 |
| 撤销不计入 | 撤销记录保留历史，但标记不计入 |
| 导出权限 | 结果库导出使用独立权限 |

建议字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 结果 ID |
| `record_id` | 来源业务记录 |
| `result_version` | 结果版本 |
| `result_status` | `VALID`、`CANCELLED_EXCLUDED`、`CORRECTED` 等 |
| `included` | 是否计入统计 |
| `result_snapshot` | 入库快照 |
| `archived_by` | 入库人 |
| `archived_time` | 入库时间 |
| `corrected_from_result_id` | 更正来源版本 |
| `correct_reason` | 更正原因 |

## 13. 现有 SQL 草案审查

### 13.1 已有设计优点

| 文件 | 优点 |
| --- | --- |
| `001_system_tables.sql` | 已覆盖用户、角色、部门、菜单、角色菜单、角色部门、登录日志、系统操作日志，具备权限底座雏形 |
| `002_business_common_tables.sql` | 已覆盖待办、信箱、附件、业务动作日志、字段变更日志、导入批次、导出记录 |
| `003_daily_violation_tables.sql` | 已覆盖违标编码、每日违标记录、导入原始行、不属实反馈、结果库 |

已有亮点：

1. 已考虑用户、角色、部门、菜单和数据权限关联。
2. 已考虑业务日志与字段变更明细。
3. 已保存 Excel 原始行 JSON。
4. 已将结果库表与业务记录表分离。
5. 已在部分关键字段上建立索引，例如状态、部门、编码、工号、日期。

### 13.2 缺少哪些表

| 缺少表 | 说明 |
| --- | --- |
| `base_person` | 人员基础数据，支持工号带出责任人 |
| `base_org` | 业务组织层级，若不完全复用 `sys_dept` 需单独维护 |
| `base_violation_code` | 违标编码基础数据；当前 `violation_code_dict` 命名不符合 `base_` 前缀 |
| `base_import_template` | 导入导出模板配置 |
| `biz_file` | 文件元数据表 |
| `biz_file_bind` | 文件与业务记录绑定表 |
| `daily_violation_batch` | 手工录入和公示批次表，不应只依赖导入批次 |
| `daily_violation_flow` | 结构化流程流转记录，可与业务日志互补 |
| `daily_violation_import_row` | 每日违标 staging / 预览行，建议从 raw row 中拆出业务字段 |
| `daily_violation_import_error` | 导入错误明细和错误报告来源 |
| `daily_violation_result_version` | 如结果库版本复杂，可单独拆版本表 |

### 13.3 缺少哪些字段

| 表 | 缺少字段或调整方向 |
| --- | --- |
| 多数表 | 缺少统一 `id`、`created_by`、`created_time`、`updated_by`、`updated_time`、`deleted`、`remark` 风格 |
| `sys_dept` | 可补 `deleted`、`remark`；`del_flag` 与目标规范需统一 |
| `sys_user` | 可补人员快照相关基础字段应下沉到 `base_person`；`phone` 与 RuoYi 风格 `phonenumber` 需统一 |
| `biz_task` | 可补 `deleted`、`remark`、任务完成动作、业务 URL、优先级、处理意见 |
| `biz_message` | 可补消息归档状态、消息来源动作、业务卡片 payload、`deleted` |
| `biz_attachment` | 当前混合元数据和业务绑定；缺 `storage_type`、`bucket`、`object_key`、`original_name`、`content_type`、`hash`、`permission_scope` |
| `biz_action_log` | 缺操作人部门快照、角色快照、意见、附件引用、字段变更 JSON、traceId |
| `biz_action_log_detail` | 缺修改人、修改时间、修改原因、是否授权覆盖 |
| `biz_import_batch` | 可补原始文件 ID、模板版本、需要确认行数命名统一、失败报告文件 ID |
| `biz_export_record` | 可补导出状态、失败原因、traceId、导出权限码、业务记录范围 |
| `daily_violation_record` | 缺车间 ID、车队 ID、指导组 ID、批次 ID、状态版本号、人员 ID、组织完整快照、`deleted` 统一字段 |
| `daily_violation_raw_row` | 缺业务字段拆列、确认状态、确认人、错误明细引用 |
| `daily_violation_feedback` | 缺附件绑定引用、反馈前快照、返回复核状态、业务日志 ID |
| `daily_violation_result` | 缺 `included`、更正来源版本、完整归档人部门/角色快照、导出统计索引字段 |

### 13.4 缺少哪些索引

| 表 | 建议索引 |
| --- | --- |
| `sys_dept` | `parent_id`、`dept_type`、`status`、`deleted` |
| `sys_user` | `dept_id + status + deleted`、`employee_no + deleted` |
| `sys_menu` | `parent_id + menu_type + status`、`perms` |
| `biz_task` | `task_status + receiver_user_id`、`task_status + receiver_dept_id`、`business_type + business_id + task_status` |
| `biz_message` | `receiver_user_id + read_flag + create_time`、`business_type + business_id + create_time` |
| `biz_attachment` | `business_type + business_id`、`action_log_id`、`uploader_id + upload_time`、`deleted` |
| `biz_action_log` | `business_type + business_id + create_time`、`batch_id + create_time`、`action_type + create_time` |
| `biz_import_batch` | `business_type + import_time`、`status + import_user_id` |
| `biz_export_record` | `business_type + export_time`、`exporter_id + export_time` |
| `daily_violation_record` | `report_date + current_status`、`responsible_dept_id + current_status`、`employee_no + violation_date`、`workshop_id + current_status`、`team_id + current_status`、`guide_group_id + current_status`、`deleted + current_status` |
| `daily_violation_raw_row` | `import_batch_id + validate_status` |
| `daily_violation_feedback` | `record_id + handle_status`、`feedback_dept_id + feedback_time` |
| `daily_violation_result` | `result_status + in_time`、`included + in_time`、`record_id + result_version` |

### 13.5 缺少哪些初始化种子数据

| 种子数据 | 用途 |
| --- | --- |
| 超级管理员用户 | 初始登录和系统配置 |
| 初始角色 | 分析员、分析班长、分析室主任、车间、车队、指导组、系统管理员 |
| 初始菜单 | 首页、信箱、待办、文件、聊天、违章管理、每日 LKJ、基础数据、系统管理 |
| 初始按钮权限 | 与 `docs/PERMISSION_MATRIX.md` 中权限码对齐 |
| 初始部门树 | 分析室、车间、车队、指导组层级 |
| 示例人员 | 工号带出责任人 |
| 示例违标编码 | 编码、性质、类别、类型校验 |
| 导入导出模板 | 每日 LKJ 公示 Excel 模板 |
| 系统参数 | 文件存储类型、导出限制、附件大小限制 |

### 13.6 哪些字段命名需要统一

| 当前命名 | 建议方向 | 说明 |
| --- | --- | --- |
| `create_by` / `create_time` | `created_by` / `created_time` | 与通用字段规范统一 |
| `update_by` / `update_time` | `updated_by` / `updated_time` | 与通用字段规范统一 |
| `del_flag` | `deleted` | 统一逻辑删除字段 |
| `file_name` | `original_name` | 文件原始名与对象 key 分离 |
| `file_path` | `object_key` | 兼容本地、NAS、MinIO |
| `file_type` | `content_type` 或业务文件分类 | MIME 类型和业务分类需区分 |
| `warning_rows` | `confirm_rows` 或 `need_confirm_rows` | 与“需要确认”业务术语统一 |
| `validate_status` | `validation_status` | 命名更清晰 |
| `in_user_id` / `in_time` | `archived_by` / `archived_time` | 入结果库语义更清楚 |
| `violation_code_dict` | `base_violation_code` | 符合基础数据表前缀 |

### 13.7 哪些设计可能不利于未来 PostgreSQL 迁移

| 设计点 | 风险 | 调整方向 |
| --- | --- | --- |
| `AUTO_INCREMENT` | PostgreSQL 使用 identity / sequence | 迁移工具统一主键生成策略 |
| `COMMENT` 语法 | PostgreSQL 注释语法不同 | 注释交给迁移工具或单独 comment 语句 |
| `DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | PostgreSQL 无直接等价 | 更新时间由应用层或触发器策略统一 |
| `JSON` 类型 | PostgreSQL 通常使用 `jsonb` | 保持 JSON 用途简单，迁移时映射 |
| `CHAR(1)` 状态和删除标记 | 可读性弱，扩展差 | 使用 `VARCHAR` 状态 code，`BOOLEAN/TINYINT` 删除标记 |
| MySQL 字符集和排序规则 | PostgreSQL 处理方式不同 | 避免依赖特定排序规则做业务判断 |
| `TEXT` 存储导出条件 JSON | 无结构约束 | 可统一为 JSON / JSONB 或文本快照，避免查询依赖 |

## 14. 后续需要确认的问题

1. 通用字段最终采用 `created_by` / `created_time` 风格，还是兼容当前 `create_by` / `create_time` 风格。
2. 主键是否全库统一为 `id`，还是系统表保留 `user_id`、`role_id`、`dept_id` 等业务化主键。
3. `sys_dept` 是否同时承担业务组织层级，还是新增 `base_org` 独立维护车间、车队、指导组关系。
4. 违标编码表是否从 `violation_code_dict` 调整为 `base_violation_code`。
5. 文件中心是否第一阶段拆出 `biz_file` 和 `biz_file_bind`，还是暂时沿用 `biz_attachment` 后续迁移。
6. 每日 LKJ 是否需要独立 `daily_violation_batch` 支持手工录入批次和公示批次。
7. 状态 code 是否全部采用大写英文枚举，例如 `DIRECTOR_APPROVED_PENDING_DISPATCH`。
8. 导入预览是否从 `daily_violation_raw_row` 拆出业务字段 staging 表。
9. 结果库更正是否第一阶段实现完整版本链，还是只预留字段。
10. 未来 PostgreSQL 迁移是否需要从第一版就引入迁移工具和跨库类型约定。
