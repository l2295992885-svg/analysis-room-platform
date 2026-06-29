# 后端 API 设计规范与第一阶段接口草案

本文定义“综合分析室数据分析平台”的后端 API 设计规范，以及第一阶段围绕“违章管理 > 每日 LKJ 音视频违标公示”的接口草案。本文只定义接口约定和业务契约，不包含代码实现。

第一阶段接口必须服务于平台底座和样板业务闭环：登录、当前用户、菜单权限、数据权限、文件、信箱、待办、聊天协同入口、Excel 导入导出、业务日志、每日 LKJ 音视频违标公示状态流转和结果库。

## 1. API 总体规范

| 规范项 | 要求 |
| --- | --- |
| 统一前缀 | 所有后端接口统一使用 `/api` 前缀 |
| URL 语言 | URL 只使用英文、小写、数字和连字符，不使用中文 |
| URL 语义 | 资源型接口使用名词，流程动作使用明确动作后缀 |
| 接口稳定性 | URL、请求字段、响应字段一旦进入前后端联调，不应随意改名 |
| 鉴权方式 | 统一使用登录态 token，建议请求头为 `Authorization: Bearer <token>` |
| 数据格式 | 普通请求和响应使用 JSON，文件上传使用 multipart |
| 时间格式 | 统一使用 `yyyy-MM-dd HH:mm:ss` 或 ISO 8601，后续需在实现前固定一种 |
| 幂等与并发 | 状态流转接口必须防止重复提交和并发覆盖 |
| 审计要求 | 写操作、导入、导出、附件、分享、状态流转必须写日志 |

HTTP 方法建议：

| 方法 | 用途 |
| --- | --- |
| `GET` | 查询列表、详情、下拉选项、当前用户信息 |
| `POST` | 新增、登录、导入、导出、流程动作、上传 |
| `PUT` | 编辑配置类或基础数据类资源 |
| `DELETE` | 仅用于允许删除的配置类资源；业务历史数据不得物理删除 |

## 2. 统一响应结构

所有接口统一返回以下结构：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `code` | number | 是 | 业务响应码 |
| `message` | string | 是 | 可读提示信息 |
| `data` | object / array / null | 是 | 响应数据，无数据时返回 null |
| `traceId` | string | 是 | 请求追踪 ID，用于排查问题 |
| `timestamp` | string | 是 | 服务端响应时间 |

响应规则：

1. `code = 200` 表示业务成功。
2. HTTP 状态码和业务 `code` 应保持一致语义，例如未登录应返回 HTTP 401 和 `code = 401`。
3. 业务校验失败使用 `422`，例如违标编码不存在、状态不允许提交、字段不一致。
4. 状态冲突使用 `409`，例如重复提交、记录已被其他人处理。
5. `traceId` 必须写入后端日志，便于前后端联合排查。

## 3. 分页请求和分页响应

分页请求统一使用查询参数。

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNo` | number | 否 | 1 | 当前页码，从 1 开始 |
| `pageSize` | number | 否 | 20 | 每页数量 |
| `sortBy` | string | 否 | 业务默认 | 排序字段，必须在后端白名单内 |
| `sortOrder` | string | 否 | `desc` | `asc` 或 `desc` |
| 业务筛选字段 | string / number / date | 否 | 无 | 由具体接口定义 |

分页响应的 `data` 结构：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `records` | array | 是 | 当前页数据 |
| `total` | number | 是 | 总记录数 |
| `pageNo` | number | 是 | 当前页码 |
| `pageSize` | number | 是 | 每页数量 |
| `pages` | number | 是 | 总页数 |

分页规则：

1. 所有列表接口必须在后端分页，不允许前端一次性拉全量数据再分页。
2. 所有列表接口必须执行数据权限过滤。
3. 排序字段必须使用后端白名单，禁止前端直接拼接 SQL 字段。
4. 导出接口不得直接复用分页结果，应按导出权限和数据权限重新查询导出范围。

## 4. 错误码规范

| code | 含义 | 典型场景 | 前端建议处理 |
| --- | --- | --- | --- |
| `200` | 成功 | 查询、新增、提交、审核成功 | 正常展示数据或成功提示 |
| `400` | 参数错误 | 参数格式错误、缺少必填参数 | 展示参数错误 |
| `401` | 未登录或登录过期 | token 缺失、token 过期 | 清理登录态并跳转登录页 |
| `403` | 无权限 | 无功能权限、操作权限、数据权限、字段权限、导出权限 | 展示无权限提示 |
| `404` | 资源不存在 | 记录不存在、附件不存在、接口路径不存在 | 展示不存在或已删除提示 |
| `409` | 状态冲突 | 重复提交、记录状态已变化、并发处理冲突 | 提示刷新后重试 |
| `422` | 业务校验失败 | 编码不存在、编码性质不匹配、校验失败行不得提交 | 展示业务校验原因 |
| `500` | 服务端错误 | 未预期异常、数据库异常、文件服务异常 | 展示系统错误并提示 traceId |

## 5. 权限标识规范

权限标识统一使用：

```text
模块:资源:动作
```

命名规则：

| 规则 | 示例 |
| --- | --- |
| 模块使用业务域或平台域 | `violation`、`system`、`file`、`mailbox` |
| 资源使用英文名词 | `daily`、`user`、`role`、`attachment` |
| 动作使用英文动词或动作短语 | `view`、`add`、`submit`、`leader-audit` |
| 不使用中文权限码 | 使用 `violation:daily:submit`，不使用中文拼音混写 |
| 粒度以安全边界为准 | 班长审核和主任审核必须拆成不同权限码 |

每日 LKJ 音视频违标公示第一阶段核心权限码：

| 权限码 | 含义 |
| --- | --- |
| `violation:daily:view` | 查看公示业务 |
| `violation:daily:add` | 新增记录 |
| `violation:daily:edit` | 编辑草稿或退回记录 |
| `violation:daily:import` | Excel 导入 |
| `violation:daily:preview` | 预览确认 |
| `violation:daily:submit` | 提交班长 |
| `violation:daily:leader-audit` | 班长审核 |
| `violation:daily:director-audit` | 主任审核 |
| `violation:daily:return` | 退回 |
| `violation:daily:dispatch-workshop` | 下发车间 |
| `violation:daily:dispatch-team` | 下发车队 |
| `violation:daily:dispatch-guide-group` | 下发指导组 |
| `violation:daily:guide-confirm` | 指导组确认无误 |
| `violation:daily:feedback` | 指导组反馈不属实 |
| `violation:daily:review` | 返回复核 |
| `violation:daily:confirm` | 主任最终确认 |
| `violation:daily:archive` | 入结果库 |
| `violation:daily:cancel` | 撤销不计入 |
| `violation:daily:export` | 导出公示数据 |
| `violation:daily:result:view` | 查看结果库 |
| `violation:daily:result:export` | 导出结果库 |
| `violation:daily:result:correct` | 结果库更正 |
| `violation:daily:result:version:view` | 查看结果库版本 |
| `violation:daily:attachment:upload` | 上传业务附件 |
| `violation:daily:attachment:view` | 查看附件 |
| `violation:daily:attachment:download` | 下载附件 |
| `violation:daily:share` | 分享业务卡片 |
| `violation:daily:override` | 授权覆盖关键字段 |

## 6. 登录与当前用户接口草案

| 方法 | 路径 | 权限码 | 用途 | 关键请求 | 关键响应 | 安全要求 |
| --- | --- | --- | --- | --- | --- | --- |
| `POST` | `/api/auth/login` | 无 | 用户登录 | 账号、密码、验证码可选 | token、过期时间 | 成功和失败都写登录日志 |
| `POST` | `/api/auth/logout` | 登录用户 | 退出登录 | 无 | 操作结果 | 清理服务端登录态并写日志 |
| `GET` | `/api/auth/profile` | 登录用户 | 获取当前用户 | 无 | 用户 ID、姓名、部门、角色 | 返回当前登录用户快照 |
| `GET` | `/api/auth/menus` | 登录用户 | 获取当前用户菜单 | 无 | 菜单树、路由信息 | 只返回有功能权限的菜单 |
| `GET` | `/api/auth/permissions` | 登录用户 | 获取按钮和操作权限 | 无 | 权限码数组 | 只返回当前用户有效权限 |
| `POST` | `/api/auth/password/change` | 登录用户 | 修改本人密码 | 原密码、新密码 | 操作结果 | 校验原密码，写操作日志 |

说明：

1. 当前用户信息、菜单和权限必须以后端为准。
2. 前端可缓存菜单和权限用于显示，但不能作为接口授权依据。
3. 系统管理员也必须通过正常登录态访问系统管理接口。

## 7. 系统权限接口草案

### 7.1 用户、角色、部门、菜单

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/system/users` | `system:user:list` | 用户分页列表 | 数据权限过滤 |
| `GET` | `/api/system/users/{userId}` | `system:user:query` | 用户详情 | 数据权限过滤 |
| `POST` | `/api/system/users` | `system:user:add` | 新增用户 | 写操作日志 |
| `PUT` | `/api/system/users/{userId}` | `system:user:edit` | 编辑用户 | 写操作日志 |
| `POST` | `/api/system/users/{userId}/status` | `system:user:edit` | 启停用户 | 写操作日志 |
| `POST` | `/api/system/users/{userId}/password/reset` | `system:user:reset-password` | 重置密码 | 写操作日志，敏感操作 |
| `GET` | `/api/system/roles` | `system:role:list` | 角色分页列表 | 数据权限过滤 |
| `POST` | `/api/system/roles` | `system:role:add` | 新增角色 | 写操作日志 |
| `PUT` | `/api/system/roles/{roleId}` | `system:role:edit` | 编辑角色 | 写操作日志 |
| `POST` | `/api/system/roles/{roleId}/menus` | `system:role:grant-menu` | 分配菜单和按钮权限 | 写操作日志 |
| `POST` | `/api/system/roles/{roleId}/data-scope` | `system:role:data-scope` | 配置角色数据权限 | 写操作日志 |
| `GET` | `/api/system/depts/tree` | `system:dept:list` | 部门树 | 数据权限过滤 |
| `POST` | `/api/system/depts` | `system:dept:add` | 新增部门 | 写操作日志 |
| `PUT` | `/api/system/depts/{deptId}` | `system:dept:edit` | 编辑部门 | 写操作日志 |
| `GET` | `/api/system/menus/tree` | `system:menu:list` | 菜单树 | 按权限返回 |
| `POST` | `/api/system/menus` | `system:menu:add` | 新增菜单或按钮 | 写操作日志 |
| `PUT` | `/api/system/menus/{menuId}` | `system:menu:edit` | 编辑菜单或按钮 | 写操作日志 |

### 7.2 系统日志与基础权限数据

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/system/login-logs` | `system:login-log:list` | 登录日志列表 | 数据权限或管理权限 |
| `POST` | `/api/system/login-logs/export` | `system:login-log:export` | 导出登录日志 | 独立导出权限，写导出日志 |
| `GET` | `/api/system/operation-logs` | `system:operation-log:list` | 操作日志列表 | 管理权限 |
| `POST` | `/api/system/operation-logs/export` | `system:operation-log:export` | 导出操作日志 | 独立导出权限，写导出日志 |
| `GET` | `/api/base/persons` | `base:person:list` | 人员基础数据 | 数据权限过滤 |
| `GET` | `/api/base/orgs/tree` | `base:org:list` | 组织层级 | 数据权限过滤 |
| `GET` | `/api/base/violation-codes` | `base:violation-code:list` | 违标编码字典 | 按授权查询 |
| `GET` | `/api/base/violation-codes/{code}` | `base:violation-code:query` | 违标编码详情 | 用于编码校验 |

说明：

1. 系统管理接口只处理配置和权限，不得绕过业务流程修改历史业务数据。
2. 用户、角色、部门、菜单的删除规则后续需单独细化，第一阶段应避免物理删除关键配置。

## 8. 每日 LKJ 音视频违标公示接口草案

### 8.1 查询、录入、预览

| 方法 | 路径 | 权限码 | 用途 | 关键请求 | 关键响应 | 安全要求 |
| --- | --- | --- | --- | --- | --- | --- |
| `GET` | `/api/violation/daily/records` | `violation:daily:view` | 公示记录分页列表 | 日期、部门、状态、编码、关键词 | 分页记录 | 数据权限过滤 |
| `GET` | `/api/violation/daily/records/{recordId}` | `violation:daily:view` | 公示记录详情 | recordId | 基本信息、附件、日志、流转 | 数据权限过滤 |
| `POST` | `/api/violation/daily/records` | `violation:daily:add` | 新增记录 | 录入字段 | recordId | 校验字段权限，写新增日志 |
| `PUT` | `/api/violation/daily/records/{recordId}` | `violation:daily:edit` | 编辑草稿或退回记录 | 录入字段 | 操作结果 | 仅草稿或退回范围可改 |
| `POST` | `/api/violation/daily/records/{recordId}/save-draft` | `violation:daily:edit` | 保存草稿 | 草稿字段 | 操作结果 | 校验本人或任务归属 |
| `POST` | `/api/violation/daily/records/{recordId}/preview-confirm` | `violation:daily:preview` | 单条预览确认 | recordId | 校验结果 | 编码和必填项通过才可确认 |
| `POST` | `/api/violation/daily/batches/{batchId}/preview-confirm` | `violation:daily:preview` | 批次预览确认 | batchId、勾选记录 ID | 校验分区结果 | 校验失败记录不得确认 |
| `GET` | `/api/violation/daily/records/{recordId}/logs` | `violation:daily:view` | 查看业务证据链 | recordId | 日志列表 | 数据权限过滤 |

### 8.2 Excel 导入

| 方法 | 路径 | 权限码 | 用途 | 关键请求 | 关键响应 | 安全要求 |
| --- | --- | --- | --- | --- | --- | --- |
| `POST` | `/api/violation/daily/imports` | `violation:daily:import` | 上传并解析 Excel | Excel 文件、模板类型 | importBatchId、校验汇总 | 保存原始文件和行级 JSON |
| `GET` | `/api/violation/daily/imports/{importBatchId}` | `violation:daily:import` | 导入批次详情 | importBatchId | 批次信息、统计 | 数据权限过滤 |
| `GET` | `/api/violation/daily/imports/{importBatchId}/rows` | `violation:daily:import` | 导入预览行列表 | 校验状态、分页 | 分页预览行 | 区分通过、需确认、失败 |
| `POST` | `/api/violation/daily/imports/{importBatchId}/validate` | `violation:daily:import` | 重新校验导入批次 | importBatchId | 校验汇总 | 不写入正式业务表 |
| `POST` | `/api/violation/daily/imports/{importBatchId}/error-report` | `violation:daily:export` | 下载错误报告 | importBatchId | 文件流 | 校验导出权限，写日志 |
| `POST` | `/api/violation/daily/imports/{importBatchId}/submit` | `violation:daily:submit` | 提交导入通过的记录 | 勾选记录 ID | 操作结果 | 校验失败记录不得提交 |

### 8.3 流程动作

| 方法 | 路径 | 权限码 | 用途 | 前置状态 | 目标状态 | 安全要求 |
| --- | --- | --- | --- | --- | --- | --- |
| `POST` | `/api/violation/daily/records/{recordId}/submit` | `violation:daily:submit` | 提交班长 | 草稿 | 分析员已提交 | 校验预览确认、数据权限、当前状态 |
| `POST` | `/api/violation/daily/records/{recordId}/leader-audit` | `violation:daily:leader-audit` | 班长审核通过 | 分析员已提交 / 班长待审核 | 主任待审核 | 写日志，生成主任待办 |
| `POST` | `/api/violation/daily/records/{recordId}/director-audit` | `violation:daily:director-audit` | 主任审核通过 | 主任待审核 | 主任待下发车间 | 写审核日志，等待下发；不得设计为自循环 |
| `POST` | `/api/violation/daily/records/{recordId}/return` | `violation:daily:return` | 班长或主任退回 | 班长待审核 / 主任待审核 | 草稿 / 返回主任待复核 | 必须填写退回原因 |
| `POST` | `/api/violation/daily/records/{recordId}/dispatch-workshop` | `violation:daily:dispatch-workshop` | 主任下发车间 | 主任待下发车间 | 主任已下发车间 | 校验目标车间 |
| `POST` | `/api/violation/daily/records/{recordId}/dispatch-team` | `violation:daily:dispatch-team` | 车间下发车队 | 主任已下发车间 / 车间待确认 | 车队待确认 | 校验车间和车队关系 |
| `POST` | `/api/violation/daily/records/{recordId}/dispatch-guide-group` | `violation:daily:dispatch-guide-group` | 车队下发指导组 | 车队待确认 | 指导组待确认 | 校验车队和指导组关系 |
| `POST` | `/api/violation/daily/records/{recordId}/guide-confirm` | `violation:daily:guide-confirm` | 指导组确认无误 | 指导组待确认 | 指导组确认无误 | 校验指导组数据权限 |
| `POST` | `/api/violation/daily/records/{recordId}/guide-reject` | `violation:daily:feedback` | 指导组反馈不属实 | 指导组待确认 | 指导组反馈不属实 | 必须填写原因类型和说明，支持附件 |
| `POST` | `/api/violation/daily/records/{recordId}/return-recheck` | `violation:daily:review` | 返回复核 | 指导组确认无误 / 指导组反馈不属实 | 返回主任待复核 | 写返回复核日志 |
| `POST` | `/api/violation/daily/records/{recordId}/final-confirm` | `violation:daily:confirm` | 主任最终确认 | 返回主任待复核 | 主任最终确认 | 必须有复核意见 |
| `POST` | `/api/violation/daily/records/{recordId}/archive` | `violation:daily:archive` | 入结果库 | 主任最终确认 | 已入结果库 | 校验完整日志，保存入库快照 |
| `POST` | `/api/violation/daily/records/{recordId}/cancel` | `violation:daily:cancel` | 撤销不计入 | 主任最终确认 | 已撤销不计入 | 必须填写撤销原因 |

说明：

1. 目标状态必须由后端根据当前状态和动作计算，前端不得直接传目标状态。
2. 每个流程动作必须同时校验登录态、功能权限、操作权限、数据权限、当前状态、字段权限或附件权限，并写业务日志。
3. `指导组确认无误` 使用 `violation:daily:guide-confirm`；`指导组反馈不属实` 使用 `violation:daily:feedback`。

### 8.4 结果库

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/violation/daily/results` | `violation:daily:result:view` | 结果库分页列表 | 数据权限过滤 |
| `GET` | `/api/violation/daily/results/{resultId}` | `violation:daily:result:view` | 结果库详情 | 数据权限过滤 |
| `GET` | `/api/violation/daily/results/{resultId}/versions` | `violation:daily:result:version:view` | 查看结果版本 | 数据权限过滤 |
| `POST` | `/api/violation/daily/results/{resultId}/correct` | `violation:daily:result:correct` | 结果库更正 | 不覆盖旧版本，写更正日志 |
| `POST` | `/api/violation/daily/results/export` | `violation:daily:result:export` | 导出结果库 | 独立导出权限，写导出日志 |

## 9. 文件接口草案

| 方法 | 路径 | 权限码 | 用途 | 关键请求 | 安全要求 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/api/files/upload` | `file:center:upload` | 上传文件 | multipart 文件、业务类型可选 | 校验文件类型、大小，写上传日志 |
| `GET` | `/api/files` | `file:center:view` | 文件列表 | 业务类型、上传人、时间 | 数据权限过滤 |
| `GET` | `/api/files/{fileId}` | `file:center:view` | 文件详情 | fileId | 校验附件查看权限 |
| `GET` | `/api/files/{fileId}/download` | `file:center:download` | 下载文件 | fileId | 校验下载权限，写下载日志 |
| `POST` | `/api/files/{fileId}/bind` | `file:center:upload` | 绑定业务记录 | businessType、businessId、actionType | 绑定上传人、时间、业务动作 |
| `POST` | `/api/files/{fileId}/share` | `chat:share` | 分享文件 | 接收人或会话 | 写分享日志，不扩大权限 |

每日 LKJ 附件接口可使用业务路径包装：

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/violation/daily/records/{recordId}/attachments` | `violation:daily:attachment:upload` | 上传业务附件 | 校验业务数据权限和当前状态 |
| `GET` | `/api/violation/daily/records/{recordId}/attachments` | `violation:daily:attachment:view` | 附件列表 | 校验业务数据权限 |
| `GET` | `/api/violation/daily/attachments/{attachmentId}/download` | `violation:daily:attachment:download` | 下载业务附件 | 写附件下载日志 |

## 10. 信箱、待办、聊天协同接口草案

### 10.1 信箱

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/mailbox/messages` | `mailbox:view` | 信箱消息列表 | 只看当前用户、部门、角色范围消息 |
| `GET` | `/api/mailbox/messages/{messageId}` | `mailbox:view` | 信箱消息详情 | 校验消息接收范围 |
| `POST` | `/api/mailbox/messages/{messageId}/read` | `mailbox:view` | 标记已读 | 只能处理自己的消息 |
| `POST` | `/api/mailbox/messages/{messageId}/archive` | `mailbox:view` | 归档消息 | 只能处理自己的消息 |

### 10.2 待办

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/todos/my` | `todo:view` | 我的待办 | 当前用户、部门、角色范围 |
| `GET` | `/api/todos/done` | `todo:view` | 已处理任务 | 当前用户处理历史 |
| `GET` | `/api/todos/{todoId}` | `todo:view` | 待办详情 | 校验待办归属 |
| `POST` | `/api/todos/{todoId}/open` | `todo:view` | 打开待办并跳转业务 | 重新校验业务功能权限和数据权限 |

待办规则：

1. 待办只显示当前必须处理的任务。
2. 待办完成必须由正式业务动作驱动，不允许单独完成待办绕过流程。

### 10.3 聊天协同

| 方法 | 路径 | 权限码 | 用途 | 安全要求 |
| --- | --- | --- | --- | --- |
| `GET` | `/api/chat/conversations` | `chat:view` | 会话列表 | 当前用户会话范围 |
| `GET` | `/api/chat/conversations/{conversationId}/messages` | `chat:view` | 消息列表 | 校验会话成员 |
| `POST` | `/api/chat/conversations/{conversationId}/messages` | `chat:view` | 发送消息 | 校验会话成员 |
| `POST` | `/api/chat/business-cards` | `violation:daily:share` | 发送业务卡片 | 写业务分享和聊天卡片日志 |
| `GET` | `/api/chat/business-cards/{cardId}/open` | `chat:view` | 打开业务卡片 | 重新校验业务权限、数据权限、流程状态 |

聊天业务卡片规则：

1. 业务卡片只是提醒和跳转入口。
2. 收到卡片不代表拥有查看权限。
3. 收到卡片不代表拥有审核、下发、反馈权限。
4. 点击卡片后必须重新校验功能权限、数据权限和流程状态。

## 11. 导出接口规则

导出接口统一使用 `POST`，请求体中传导出条件。导出必须由后端重新查询数据，不能导出前端当前表格缓存。

| 导出场景 | 路径 | 权限码 | 日志要求 |
| --- | --- | --- | --- |
| 每日 LKJ 公示列表导出 | `/api/violation/daily/records/export` | `violation:daily:export` | 写导出人、时间、条件、数量、文件名、页面 |
| 每日 LKJ 导入错误报告 | `/api/violation/daily/imports/{importBatchId}/error-report` | `violation:daily:export` | 使用 `POST` 下载，写导出日志，记录导入批次 |
| 每日 LKJ 结果库导出 | `/api/violation/daily/results/export` | `violation:daily:result:export` | 写结果库导出日志 |
| 登录日志导出 | `/api/system/login-logs/export` | `system:login-log:export` | 写系统导出日志 |
| 操作日志导出 | `/api/system/operation-logs/export` | `system:operation-log:export` | 写系统导出日志 |

导出规则：

1. 能看不等于能导出。
2. 所有导出必须校验独立导出权限。
3. 所有导出内容必须受数据权限约束。
4. 所有导出必须写业务日志或系统导出日志。
5. 每日 LKJ 音视频违标公示导出标题和副标题必须包含提报日期。
6. 每日 LKJ 音视频违标公示导出表格列只导出违章发生日期，不单独导出提报日期列。
7. 导出失败也应记录失败原因和 traceId。

## 12. 安全原则

| 场景 | 后端必须校验 |
| --- | --- |
| 查询列表 | 登录态、功能权限、数据权限、查询条件白名单 |
| 查询详情 | 登录态、功能权限、数据权限、记录存在性 |
| 新增或编辑 | 登录态、操作权限、数据权限、字段权限、业务校验 |
| 流程动作 | 登录态、功能权限、操作权限、数据权限、当前状态、目标对象范围 |
| 字段覆盖 | 登录态、操作权限、字段权限、覆盖原因、字段变更日志 |
| 附件查看下载 | 登录态、附件权限、业务数据权限、附件归属 |
| Excel 导入 | 登录态、导入权限、文件类型、模板、行级校验、原始快照 |
| Excel 导出 | 登录态、导出权限、数据权限、导出日志 |
| 聊天卡片打开 | 登录态、功能权限、数据权限、流程状态 |

安全底线：

1. 前端传来的角色、部门、状态不可信。
2. 前端隐藏按钮不能作为安全依据。
3. 前端传来的目标状态不可信，状态必须由后端计算。
4. 所有查询接口必须校验数据权限。
5. 所有动作接口必须校验操作权限、数据权限和当前状态。
6. 所有字段修改必须校验字段权限。
7. 所有导出必须写日志。
8. 系统管理员不默认拥有修改历史业务数据的权力。
9. 聊天业务卡片不代表授权。
10. 没有完整业务证据链日志的数据不得进入结果库。

## 13. 后续需要确认的问题

1. 统一时间格式最终采用 `yyyy-MM-dd HH:mm:ss` 还是 ISO 8601。
2. `message` 字段是否统一使用英文 `message`，还是兼容前端模板已有的 `msg`。
3. 是否允许主任审核通过后自动下发车间，还是必须保留主任待下发车间状态。
4. 退回接口是否拆为班长退回、主任退回两个路径和权限码。
5. 车间是否需要单独“接收确认”接口。
6. 指导组确认或反馈后，返回复核是手动逐级返回，还是系统自动回到分析室。
7. 结果库更正流程是否第一阶段实现，还是只预留接口和权限码。
8. 聊天协同第一阶段是否只做业务卡片发送记录，不做完整实时 IM。
