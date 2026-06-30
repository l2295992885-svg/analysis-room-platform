# 全栈平台代码搭建流程

## 前置合并顺序

建议先合并已经准备好的两个基础 PR：

1. RuoYi-Vue-Plus 参考说明 PR。
2. Art Design Pro 前端模板导入 PR。

如果两个 PR 尚未合并，本流程仍可作为实施顺序，但实际代码应等对应目录和文档进入 `main` 后再继续。

## 本地环境

建议版本：

```text
JDK 17+
Maven 3.9+
Node.js >= 20.19.0
pnpm >= 8.8.0
MySQL 8.x
Git
GitHub CLI gh
```

Windows 下如果 `pnpm` 不在 PATH，可使用：

```powershell
corepack prepare pnpm@latest --activate
corepack pnpm --version
```

## 目标目录

```text
analysis-room-platform/
  frontend/
  backend/
  database/
    mysql/
  docs/
  scripts/
```

## 阶段一：准备前端工程

进入前端目录：

```bash
cd frontend
corepack pnpm install --ignore-scripts
corepack pnpm build
```

普通 `pnpm install` 可能因为模板 Husky prepare 脚本找不到 `frontend/.git` 而失败。子目录工程中建议先使用 `--ignore-scripts`，后续若需要 Git hooks，再在仓库根目录统一配置。

前端基础配置：

```text
frontend/.env
frontend/.env.development
frontend/.env.production
```

第一阶段建议改造：

```text
VITE_ACCESS_MODE = backend
VITE_API_URL = /api
VITE_API_PROXY_URL = http://localhost:8080
```

本地开发代理应由 Vite 转发到 Spring Boot 后端：

```text
浏览器 -> Vite dev server -> /api 代理 -> Spring Boot 8080
```

## 阶段二：初始化后端工程

建议后端使用 Maven 多模块或单模块起步。第一阶段可先单模块，目录清晰即可。

```bash
mkdir backend
cd backend
```

建议 Maven 坐标：

```text
groupId: com.analysisroom
artifactId: analysis-room-platform-backend
package: com.analysisroom.platform
java: 17
spring boot: 3.x
```

建议依赖：

```text
spring-boot-starter-web
spring-boot-starter-validation
spring-boot-starter-security 或 Sa-Token
mybatis-plus-spring-boot3-starter
mysql-connector-j
spring-boot-starter-aop
spring-boot-starter-data-redis 可后置
springdoc-openapi-starter-webmvc-ui
easyexcel 或 fastexcel
minio 或 AWS S3 SDK 可后置
lombok
mapstruct 可选
```

启动类：

```text
backend/src/main/java/com/analysisroom/platform/AnalysisRoomApplication.java
```

配置文件：

```text
backend/src/main/resources/application.yml
backend/src/main/resources/application-dev.yml
```

## 阶段三：数据库脚本

创建：

```text
database/mysql/001_init_system.sql
database/mysql/002_init_common_business.sql
database/mysql/003_init_violation_daily.sql
database/mysql/004_init_seed_data.sql
```

系统底座先建：

```text
sys_user
sys_role
sys_dept
sys_menu
sys_user_role
sys_role_menu
sys_role_dept
sys_login_log
sys_oper_log
```

再建公共业务表：

```text
biz_inbox_message
biz_todo_task
biz_file
biz_file_bind
biz_evidence_log
base_person
base_org
base_violation_code
base_import_template
```

最后建样板业务表：

```text
violation_daily_batch
violation_daily_item
violation_daily_flow
violation_daily_result
violation_daily_import_error
```

## 阶段四：后端基础包

先搭建通用层：

```text
common/core
common/web
common/security
common/permission
common/log
common/excel
common/file
common/mybatis
```

必须先完成：

1. 统一响应 `R<T>`。
2. 统一分页 `PageResult<T>`。
3. 全局异常处理。
4. 登录态解析。
5. 权限注解或权限拦截。
6. 操作日志注解。
7. 数据权限上下文。
8. MyBatis Plus 分页配置。

## 阶段五：认证和用户上下文

后端先实现：

```text
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/profile
GET  /api/auth/menus
GET  /api/auth/permissions
```

前端改造：

```text
frontend/src/utils/http/index.ts
frontend/src/store/modules/user.ts
frontend/src/store/modules/menu.ts
frontend/src/router/guards/beforeEach.ts
frontend/src/directives/core/auth.ts
```

验收标准：

- 登录成功后保存 token。
- 刷新页面后能恢复用户信息。
- 后端返回菜单后前端能渲染侧边栏。
- 无权限按钮不显示。
- 直接请求无权限接口返回 `403`。

## 阶段六：系统管理

后端实现：

```text
/api/system/users
/api/system/roles
/api/system/depts
/api/system/menus
/api/system/login-logs
/api/system/oper-logs
```

前端先复用 Art Design Pro 的系统管理页面结构，再替换为真实接口。

验收标准：

- 用户可分配角色。
- 角色可分配菜单和按钮权限。
- 角色可设置部门数据权限。
- 登录日志、操作日志能查询。
- 用户、角色、部门、菜单接口均有后端权限校验。

## 阶段七：公共业务能力

实现顺序：

1. 文件中心。
2. 信箱中心。
3. 待办中心。
4. 业务证据链日志。
5. Excel 导入导出。

建议接口：

```text
POST /api/files/upload
GET  /api/files/{id}/download
GET  /api/files
POST /api/files/bind

GET  /api/inbox/messages
POST /api/inbox/messages/{id}/read

GET  /api/todos/my
POST /api/todos/{id}/complete

GET  /api/evidence/logs
```

验收标准：

- 附件必须绑定业务记录、上传人、上传时间、业务动作。
- 待办必须能按当前用户和角色过滤。
- 信箱消息必须可读、可归档。
- Excel 导出必须写导出日志。

## 阶段八：基础数据

先做：

```text
人员管理
组织管理
违标编码字典
导入导出模板
```

接口：

```text
/api/base/persons
/api/base/orgs
/api/base/violation-codes
/api/base/import-templates
```

验收标准：

- 违标编码不存在时不允许导入。
- 编码、性质、类别、类型必须由数据库校验。
- 人员和组织变更不得影响历史业务快照。

## 阶段九：每日 LKJ 音视频违标公示

后端按状态机实现，不引入复杂工作流引擎。

核心状态：

```text
DRAFT
IMPORTED
SUBMITTED
LEADER_APPROVED
DIRECTOR_APPROVED
DISPATCHED
TEAM_CONFIRMED
GUIDE_CONFIRMED
GUIDE_REJECTED
RETURNED
RECHECKED
FINAL_CONFIRMED
ARCHIVED
```

核心接口：

```text
POST /api/violation/daily/import
GET  /api/violation/daily/batches
GET  /api/violation/daily/items
POST /api/violation/daily/{id}/submit
POST /api/violation/daily/{id}/approve
POST /api/violation/daily/{id}/return
POST /api/violation/daily/{id}/dispatch
POST /api/violation/daily/{id}/feedback
POST /api/violation/daily/{id}/recheck
POST /api/violation/daily/{id}/final-confirm
POST /api/violation/daily/{id}/archive
GET  /api/violation/daily/export
```

前端页面：

```text
frontend/src/views/violation/daily/index.vue
frontend/src/views/violation/daily/import.vue
frontend/src/views/violation/daily/preview.vue
frontend/src/views/violation/daily/detail-drawer.vue
frontend/src/views/violation/daily/components/
```

验收标准：

- 导入后只进入预览，不直接入结果库。
- 校验失败行有错误原因。
- 每次状态变化写业务证据链。
- 不同角色只看到自己可处理的数据。
- 退回、反馈不属实必须填写原因。
- 入结果库后不可物理删除，不可覆盖快照。

## 阶段十：联调与质量门槛

每个纵向切片至少验证：

```text
前端构建：corepack pnpm build
后端测试：mvn test
后端打包：mvn package
接口文档：/swagger-ui 或 /v3/api-docs
数据库脚本：空库可初始化
权限校验：无权限返回 403
数据权限：跨组织数据不可见
日志校验：关键动作有日志
导出校验：导出有权限和日志
```

## 推荐分支顺序

```text
feature/init-backend-scaffold
feature/connect-auth-menu
feature/system-permission
feature/common-file-inbox-todo
feature/base-data
feature/daily-violation-import
feature/daily-violation-flow
feature/daily-violation-export
feature/security-review
```

每个分支应是可运行、可验证的纵向切片，避免长期大分支。

## 本地启动顺序

1. 启动 MySQL，执行 `database/mysql` 初始化脚本。
2. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

3. 启动前端：

```bash
cd frontend
corepack pnpm install --ignore-scripts
corepack pnpm dev
```

4. 浏览器访问前端地址。
5. 前端通过 `/api` 代理访问后端。

## 首个可交付切片

建议第一个真正可运行切片是：

```text
登录
获取用户信息
获取菜单
渲染首页
退出登录
后端登录日志
```

该切片跑通后，再推进用户、角色、部门、菜单管理和每日 LKJ 音视频违标公示业务。
