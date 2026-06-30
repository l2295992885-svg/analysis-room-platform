# RuoYi 5.X Baseline Plan

本文是阶段 0 的底座迁移方案。目标是把“综合分析室数据分析平台”的正式 MVP 主线从当前 cleanroom 原型，迁移到 RuoYi-Vue-Plus 5.X 后端和匹配的 RuoYi/plus-ui 5.X 前端底座。

## 1. 阶段 0 盘点结论

| 检查项 | 当前结果 | 结论 |
| --- | --- | --- |
| 后端 | 阶段 0 初始盘点时 `backend/` 是 Spring Boot cleanroom 原型；阶段 1 已导入 RuoYi-Vue-Plus 5.X | 当前 `backend/` 已是 RuoYi 5.X 正式底座 |
| RuoYi 后端目录 | 当前已存在 `ruoyi-admin`、`ruoyi-common`、`ruoyi-modules`、`ruoyi-extend` | RuoYi 5.X 后端已落地 |
| 前端 | 阶段 0 初始盘点时 `frontend/` 只有 `.gitkeep`；阶段 1 已导入 plus-ui 5.X | 当前 `frontend/` 已是配套前端底座 |
| 数据库 | `database/` 有项目 SQL 草案；RuoYi 5.X 在 `backend/script/sql/` 提供初始化 SQL | 项目草案作为业务建模参考，正式底座以 RuoYi SQL 为主 |
| 文档 | 已有项目简介、模块规划、权限规则、流程规则、API、数据库、日志、导入导出、每日 LKJ 样板业务、状态矩阵、权限矩阵等文档 | 业务契约较完整，可继续作为后续实现约束 |
| 参考阅读 | 已有 RuoYi-Vue-Plus 和 Art Design Pro 代码阅读文档 | 可作为迁移和对接参考，但不能替代实际底座导入 |

## 2. 正式工程决策

| 决策 | 内容 |
| --- | --- |
| 后端主线 | 使用 RuoYi-Vue-Plus 5.X 作为正式后端底座 |
| 前端主线 | 使用匹配的 RuoYi/plus-ui 5.X 前端作为正式前端底座 |
| Art Design Pro | 作为 UI 风格、表格、表单、搜索栏、工作台体验参考，不作为当前阶段默认源码底座 |
| cleanroom 后端 | 已归档到 `prototype/backend-cleanroom/`，不再作为正式业务主线继续扩展 |
| 当前文档 | 继续作为本项目业务边界、权限、安全、日志、状态流转和每日 LKJ 样板业务契约 |

## 3. cleanroom 原型处理方案

此前 `backend/` 具备健康检查、统一响应、登录认证、系统管理只读接口、操作日志和写保护框架等原型能力。这些代码不能直接等同于 RuoYi 5.X 正式底座。

推荐处理方式：

| 方案 | 路径 | 说明 | 推荐度 |
| --- | --- | --- | --- |
| 归档原型 | `prototype/backend-cleanroom/` | 保留可阅读源码，作为接口约定和实现参考 | 推荐 |
| 历史归档 | `archive/backend-cleanroom/` | 表示不再维护，只用于查阅 | 可选 |
| 直接删除 | 无 | 会丢失此前验证成果，不建议在未确认前执行 | 不推荐 |

阶段 1 已完成明确迁移动作：cleanroom 后端归档到 `prototype/backend-cleanroom/`，RuoYi 5.X 后端放入 `backend/`。

## 3.1 已锁定的正式来源

| 来源 | 仓库 | 分支 | commit |
| --- | --- | --- | --- |
| RuoYi-Vue-Plus 5.X 后端 | `https://github.com/dromara/RuoYi-Vue-Plus.git` | `5.X` | `e49f02f89e17ee5a4cc14048af99cc83d72872a7` |
| plus-ui 5.X 前端 | `https://github.com/JavaLionLi/plus-ui.git` | `5.X` | `d0d451967676707021b9857df529c395b27e90a7` |

## 4. 推荐目录结构

正式底座导入后的建议结构：

```text
analysis-room-platform/
  backend/
    ruoyi-admin/
    ruoyi-common/
    ruoyi-extend/
    ruoyi-modules/
    script/
    pom.xml
    LICENSE
    SOURCE.md
  frontend/
    src/
    public/
    package.json
    pnpm-lock.yaml
    LICENSE
    SOURCE.md
  docs/
    DAILY_LKJ_VIOLATION_PUBLICITY.md
    STATE_TRANSITION_MATRIX.md
    PERMISSION_MATRIX.md
    API_CONVENTIONS.md
    DATABASE_CONVENTIONS.md
    LOG_RULES.md
    EXPORT_RULES.md
  database/
    001_system_tables.sql
    002_business_common_tables.sql
    003_daily_violation_tables.sql
  prototype/
    backend-cleanroom/
  handoff/
    CODEX_PHASE_REPORT.md
  SOURCE.md
```

说明：

1. `backend/` 应成为 RuoYi-Vue-Plus 5.X 后端主线，不再混入 cleanroom 包结构。
2. `frontend/` 应成为匹配的 RuoYi/plus-ui 5.X 前端主线。
3. `prototype/backend-cleanroom/` 仅作参考，不参与正式运行。
4. `docs/` 和 `database/` 保持项目业务约束，但后续 SQL 需要按 RuoYi 正式表结构重新对齐。

## 5. RuoYi 5.X 能力采用边界

| 能力 | 第一阶段处理 |
| --- | --- |
| 登录认证 | 采用 RuoYi 5.X 原有登录、安全、Sa-Token 能力，再按本项目响应和前端需要做适配 |
| 用户、角色、部门、菜单 | 采用 RuoYi 系统管理能力作为底座，补充本项目角色和权限码 |
| 按钮权限 | 采用 RuoYi 菜单/按钮权限模型，权限码对齐 `docs/PERMISSION_MATRIX.md` |
| 数据权限 | 优先复用 RuoYi 数据权限设计，第一阶段按部门、本部门及以下、本人、自定义范围落地 |
| 登录日志、操作日志 | 复用 RuoYi 成熟日志能力，补充每日 LKJ 业务证据链日志 |
| Excel 导入导出 | 参考 RuoYi Excel 工具和本项目导入导出规则 |
| 文件/OSS/MinIO | 参考 RuoYi OSS 能力，第一阶段按本地/NAS 或 MinIO 二选一落地 |
| 代码生成器 | 保留为开发提效工具，后续再接每日 LKJ 表结构 |
| 多租户、多数据源、复杂工作流、分布式任务 | 第一阶段不启用 |

## 6. 阶段 1 进入条件

进入正式 RuoYi 5.X 底座导入前必须确认：

| 条件 | 当前状态 |
| --- | --- |
| RuoYi-Vue-Plus 5.X 后端源仓库 URL | 已锁定并导入 |
| RuoYi/plus-ui 5.X 前端源仓库 URL | 已锁定并导入 |
| 是否允许移动当前 `backend/` 到 `prototype/backend-cleanroom/` | 已执行 |
| MySQL 8.x | cleanroom 原型已验证过，正式 RuoYi 仍需重新验证 |
| Redis | 正式 RuoYi 运行通常需要，当前阶段未验证 |
| LICENSE 和来源说明 | 需要在导入时同步落地到 `backend/SOURCE.md`、`frontend/SOURCE.md` |

## 7. 阶段 1 推荐步骤

1. 新建或切换到独立分支，例如 `feature/ruoyi-5x-baseline-import`。
2. 归档当前 `backend/` 到 `prototype/backend-cleanroom/`。
3. 克隆并导入已确认 tag/commit 的 RuoYi-Vue-Plus 5.X 后端到 `backend/`。
4. 克隆并导入已确认 tag/commit 的 RuoYi/plus-ui 5.X 前端到 `frontend/`。
5. 保留两端 LICENSE、README、版权信息和 SOURCE 说明。
6. 配置 MySQL 8.x、Redis 和本地开发环境变量，不提交真实密码。
7. 启动 RuoYi 后端，验证系统初始化 SQL、登录、当前用户、菜单、用户/角色/部门/菜单管理、登录日志、操作日志。
8. 启动前端，验证登录页、首页、系统管理菜单和基础 API 对接。
9. 对照本项目文档隐藏或后置多租户、多数据源、复杂工作流、分布式任务等非 MVP 能力。

## 8. 风险与阻塞

| 风险 | 影响 | 处理 |
| --- | --- | --- |
| 未锁定 RuoYi 5.X tag/commit | 后续不同 Codex 可能导入不同版本 | 阶段 1 开始前必须记录确切 commit |
| 未锁定 plus-ui 5.X 来源 | 前后端接口和菜单格式可能不匹配 | 必须确认与 RuoYi 5.X 匹配的前端仓库 |
| 当前分支存在大量 cleanroom 未跟踪文件 | 直接替换会造成审阅边界不清 | 阶段 1 使用独立迁移提交或先归档原型 |
| Redis 未验证 | RuoYi 运行可能无法启动或登录失败 | 阶段 1 需加入 Redis 启动说明 |
| SQL 草案与 RuoYi 正式表结构差异 | Flyway/初始化脚本可能冲突 | 以 RuoYi 底座表为准，项目业务表再增量设计 |

## 9. 阶段 0 结论

当前仓库已完成 RuoYi 5.X 后端和 plus-ui 5.X 前端导入，cleanroom 原型已归档。

下一步最小动作应是：完成后端 Maven 构建、前端依赖安装和构建，并在可用 MySQL/Redis 环境下验证启动、登录、系统管理和代码生成器。
