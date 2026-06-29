# Codex 阶段交接报告

更新时间：2026-06-29  
阶段名称：阶段 14 上线前安全边界补强与工作区整理

## 1. 阶段目标

本阶段不新增业务模块，不做统计大屏，不做完整 IM，不做复杂工作流，不扩展新需求。工作范围只限于上线前必须补强的安全边界和工作区整理：

1. 检查当前工作区、构建产物、临时文件和敏感信息风险。
2. 修复前端 URL 中暴露长期 token 的风险。
3. 实现每日 LKJ 记录级数据权限第一版。
4. 实现每日 LKJ 字段权限第一版。
5. 补齐结果库只读版本能力和最小更正能力。
6. 准备开发环境多角色验收账号。
7. 更新验收脚本和安全扫描脚本。
8. 运行后端构建、前端构建、MVP 验收脚本、安全扫描和 `git diff --check`。
9. 更新 handoff 报告。

## 2. 工作区分类

| 分类 | 当前内容 |
| --- | --- |
| 底座导入 | `backend/` RuoYi-Vue-Plus 5.X、`frontend/` plus-ui 5.X，保留来源说明和许可证 |
| 公共能力 | 首页、基础数据、文件中心、待办中心、信箱中心、聊天协同 |
| 每日 LKJ 后端 | 导入导出、状态流转、流程日志、反馈、附件、结果库、数据权限、字段权限、结果版本 |
| 每日 LKJ 前端 | 每日 LKJ 页面、导入预览、流程动作、结果库详情/版本/更正入口 |
| 验收脚本 | `scripts/acceptance/daily-lkj-mvp-check.ps1`、`scripts/acceptance/security-gap-scan.ps1` |
| 文档报告 | `docs/`、`handoff/` 阶段报告与缺口报告 |

`.gitignore` 覆盖 `target/`、`node_modules/`、`frontend/dist/`、日志、本地密钥材料、IDE 和测试输出。当前 `git status --short` 未显示 `target`、`dist`、`node_modules` 被误纳入。

## 3. 本阶段关键修改

| 文件或范围 | 说明 |
| --- | --- |
| `frontend/src/utils/sse.ts` | SSE 不再把长期 token 拼入 URL，改为先请求短期 `sseTicket` |
| `frontend/src/utils/websocket.ts` | WebSocket 不再把长期 token 拼入 URL，改为先请求短期 `wsTicket` |
| `frontend/src/components/Process/flowChart.vue` | 流程 iframe URL 移除 `Authorization=Bearer ...` |
| `frontend/src/views/workflow/processDefinition/design.vue` | 流程设计 iframe URL 移除长期 token |
| `backend/ruoyi-common/ruoyi-common-sse/.../SseController.java` | 新增短期一次性 SSE ticket，Redis 保存 30 秒并消费后删除 |
| `backend/ruoyi-common/ruoyi-common-websocket/...` | 新增 WebSocket ticket controller，并在握手拦截器消费短期 ticket |
| `backend/ruoyi-modules/ruoyi-system/.../DailyViolationServiceImpl.java` | 接入每日 LKJ 数据权限、字段权限、结果版本与更正能力 |
| `BizTaskServiceImpl`、`BizMessageServiceImpl`、`BizChatServiceImpl` | 打开待办、信箱、聊天业务卡片时重新调用每日 LKJ 业务数据校验；无权限统一返回 403 |
| `DailyViolationController.java` | 新增结果详情、版本列表、版本对比、结果更正接口 |
| `frontend/src/views/violation/daily/index.vue` | 结果库页面新增详情、版本列表、版本对比、更正入口 |
| `backend/script/sql/analysis_room_phase7_daily_violation_import_workflow.sql` | 补充结果更正、版本查看、编码覆盖权限码和多角色开发账号权限 |
| `scripts/acceptance/daily-lkj-mvp-check.ps1` | 扩展多角色、数据权限、字段权限、附件、导出、结果版本验收 |
| `scripts/acceptance/security-gap-scan.ps1` | 持续扫描 URL token 风险，覆盖 `Authorization=Bearer` 和 token query 模式 |

## 4. 构建与检查

| 命令 | 结果 |
| --- | --- |
| `mvn clean package -DskipTests` | 通过，36 个 Maven 模块成功，总耗时 43.875s |
| `npm run build:prod` | 通过，Vite 构建耗时 18.47s；存在大 chunk 警告 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\daily-lkj-mvp-check.ps1` | 通过，`PASS=56, FAIL=0, TODO=1, SKIP=0` |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\security-gap-scan.ps1` | 通过，未出现 `url-token-risk`，即 URL token 风险为 0；仍有 `credential-keywords=674` 待人工分级 |
| `git diff --check` | 通过，无空白格式错误 |
| 浏览器访问 `http://localhost:18080/auth/tenant/list` | 可达，返回租户列表 JSON |
| 浏览器访问 `http://127.0.0.1/` | 可达，前端开发服务 HTTP 200 |

验收脚本唯一 TODO：管理员不一定属于具体流程接收人范围，因此“管理员待办数量存在”不作为硬性断言。

## 5. 自动验收覆盖

| 验收项 | 当前结果 |
| --- | --- |
| 多角色登录 | `analyst_test`、`leader_test`、`director_test`、`workshop_test`、`team_test`、`guide_test` 均登录成功 |
| 每日 LKJ 主流程 | 导入、预览、提交、班长审核、主任审核、下发车间、下发车队、下发指导组、指导组确认、复核、最终确认、入结果库通过 |
| 不属实反馈分支 | 指导组反馈不属实、返回复核、主任最终确认撤销不计入通过 |
| 数据权限 | 车间/车队/指导组用户只能看到自身范围；非接收人打开待办返回 403 |
| 附件权限 | 非授权用户下载附件返回 403 |
| 导出权限 | 记录导出、结果库导出叠加后端权限和数据范围；无权限导出返回 403 |
| 字段权限 | 非 DRAFT 修改事实字段被拒绝，返回 403 |
| 结果库版本 | 入库版本 1、更正版本 2、旧版本保留、版本对比接口通过 |
| 聊天业务卡片 | 打开业务时重新校验目标业务权限和数据权限 |
| 未登录访问 | 返回 401 |
| 普通用户访问系统管理 | 返回 403 |

## 6. 安全边界状态

| 项目 | 当前状态 |
| --- | --- |
| URL token 风险 | 已修复，源码扫描 `url-token-risk = 0` |
| SSE 长连接 | 使用 30 秒短期一次性 ticket，消费后删除 |
| WebSocket 长连接 | 使用 30 秒短期一次性 ticket，握手时换取登录用户 |
| iframe URL | 不再拼接长期 token |
| 记录级数据权限 | 每日 LKJ 查询、详情、日志、反馈、附件、导出、结果、流程动作已接入第一版 |
| 待办/信箱/聊天打开业务 | 打开时重新校验业务权限和数据权限 |
| 字段权限 | DRAFT/非 DRAFT、编码覆盖、结果更正已接入第一版 |
| 结果版本 | 详情、版本列表、版本对比、更正追加版本已接入 |
| 多角色验收 | 开发账号和脚本已通过本地 MVP 验收 |

## 7. 仍未完成项

| 项目 | 说明 |
| --- | --- |
| 真实组织关系 | 当前多角色账号和组织范围是开发环境最小数据，生产必须接入真实车间/车队/指导组隶属关系 |
| 生产密钥治理 | 数据库、Redis、JWT、OSS 等必须通过生产密钥管理或安全环境变量注入 |
| 访问日志脱敏 | 仍需在网关、后端访问日志、异常日志中统一脱敏 query 和敏感头 |
| 开发账号处理 | `*_test` 开发账号生产前必须删除、禁用或强制重置 |
| CI 门禁 | 构建、验收脚本、安全扫描尚未接入 GitHub Actions |
| 运维闭环 | HTTPS、备份、监控告警、审计留存策略尚未闭环 |

## 8. 建议拆分提交方案

详细路径分组和提交前检查见 `handoff/COMMIT_SPLIT_PLAN.md`。摘要如下：

1. `chore: import ruoyi 5.x and plus-ui baselines`
2. `docs: add platform API permission state database guidance`
3. `feat: add platform common modules for base data files todos mailbox chat`
4. `feat: add daily LKJ violation backend MVP`
5. `feat: add daily LKJ frontend workflow pages`
6. `feat: strengthen daily LKJ security boundaries`
7. `test: add daily LKJ acceptance and security scan scripts`
8. `docs: update phase handoff reports`

## 9. GitHub 状态

| 项目 | 当前状态 |
| --- | --- |
| GitHub CLI | 已安装并登录 `l2295992885-svg` |
| 远端仓库 | `https://github.com/l2295992885-svg/analysis-room-platform.git` |
| 默认分支 | `main` |
| 当前本地分支 | `feature/ruoyi-5x-baseline-import` |
| 远端同名分支 | 尚不存在 |
| 当前打开 PR | `#1 docs: add RuoYi-Vue-Plus reference guidance`、`#2 feat: initialize frontend with Art Design Pro`，均 open、未合并 |
| 推送建议 | 当前工作区跨度很大，建议按拆分提交方案分批提交，不建议直接推送一个巨大提交 |

## 10. 是否建议进入内部试运行准备

建议进入“内部试运行准备”，不建议直接生产上线。当前本地 MVP 已通过构建和阶段 14 自动验收，但生产上线前仍必须完成真实组织数据复验、生产密钥治理、HTTPS、访问日志脱敏、备份、监控告警和 CI 门禁。

## 11. 本阶段停止点

阶段 14 已按安全边界补强收口。未继续实现统计分析、大屏、完整 IM、复杂工作流或其他新增业务模块。

## 2026-06-30 增量验证与安全补强

在 PR #3 合并冲突解决并完成初轮验证后，额外发现认证异常日志可能输出无效 token 原文。已补充公共日志脱敏：`LogSanitizer` 统一脱敏 JWT、Bearer、Authorization、Cookie、token、password、secret、credential 等敏感片段；请求参数日志也扩展了敏感字段过滤。

最新验证结果：

| 命令或检查 | 结果 |
| --- | --- |
| `mvn clean package -DskipTests` | 通过，36 个 Maven 模块成功，总耗时 47.733s |
| `npm run build:prod` | 通过，构建耗时 16.48s，仍有大 chunk 警告 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\daily-lkj-mvp-check.ps1` | `PASS=56, FAIL=0, TODO=1, SKIP=0`；无失败项 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\security-gap-scan.ps1` | 未出现 `url-token-risk`；`credential-keywords=681` |
| 运行时伪造 JWT 认证失败日志 | 输出 `[REDACTED]`，未输出 token 原文 |
| `git diff --check` | 通过，仅 Windows LF/CRLF 工作区警告 |

PR #3 保持 Draft，不自动合并。建议继续按 commit group 审查，生产前补齐 CI、真实组织数据复核、生产密钥治理、HTTPS、网关/日志平台脱敏、备份和监控告警。