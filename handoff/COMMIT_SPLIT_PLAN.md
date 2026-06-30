# Commit Split Plan

更新时间：2026-06-30
适用分支：`feature/ruoyi-5x-baseline-import`

## 1. 目标

当前工作区包含底座导入、公共能力、每日 LKJ 后端、每日 LKJ 前端、验收脚本和阶段报告。不要将所有改动 squash 成一个巨大提交，建议按评审边界拆分，降低回滚和审查成本。

本文件只定义提交拆分方案，不代表已经完成提交或推送。

## 2. 提交顺序

| 顺序 | 建议提交信息 | 范围 | 说明 |
| ---: | --- | --- | --- |
| 1 | `chore: import ruoyi 5.x backend baseline` | `backend/` 中原始 RuoYi 5.X 底座、`backend/SOURCE.md`、`backend/LICENSE`、根 `.gitignore` 中后端构建忽略规则 | 只放后端底座和来源说明，不混入每日 LKJ 定制说明 |
| 2 | `chore: import plus-ui frontend baseline` | `frontend/` 中 plus-ui 底座、`frontend/SOURCE.md`、`frontend/LICENSE`、前端构建忽略规则 | 只放前端底座和来源说明，不混入业务页面二次开发 |
| 3 | `docs: add platform design and governance docs` | `docs/API_CONVENTIONS.md`、`docs/DATABASE_CONVENTIONS.md`、`docs/PERMISSION_MATRIX.md`、`docs/STATE_TRANSITION_MATRIX.md`、`docs/DAILY_LKJ_VIOLATION_PUBLICITY.md`、`docs/RUOYI_5X_BASELINE_PLAN.md` | 文档提交独立，便于先审业务契约 |
| 4 | `feat: add platform common MVP capabilities` | 首页、基础数据、文件中心、待办中心、信箱中心、聊天协同相关后端和前端改动 | 不包含每日 LKJ 核心流程 |
| 5 | `feat: add daily LKJ backend MVP workflow` | 每日 LKJ 后端表结构 SQL、领域对象、导入预览、流程动作、日志、附件、导出、结果库 | 后端业务闭环单独评审 |
| 6 | `feat: add daily LKJ frontend MVP pages` | 每日 LKJ 前端页面、导入预览、流程按钮、结果库详情/版本/更正入口 | 前端业务页面单独评审 |
| 7 | `feat: strengthen daily LKJ security boundaries` | SSE/WebSocket ticket、iframe token 移除、数据权限、字段权限、403 修正、结果库版本更正安全边界 | 上线前安全补强集中评审 |
| 8 | `test: add daily LKJ acceptance and security scans` | `scripts/acceptance/` | 验收脚本和安全扫描独立，便于 CI 后续接入 |
| 9 | `docs: update handoff reports` | `handoff/` | 阶段报告、缺口报告、提交拆分计划 |

## 3. 当前工作区分类

| 分类 | 当前路径 |
| --- | --- |
| 底座导入 | `backend/`、`frontend/`、`SOURCE.md` |
| 规则文档 | `docs/` |
| 验收脚本 | `scripts/acceptance/` |
| 阶段报告 | `handoff/` |
| 原型/参考 | `prototype/` |
| 占位文件删除 | `backend/.gitkeep`、`frontend/.gitkeep` |

## 4. 每组提交前的检查

| 检查项 | 命令 | 要求 |
| --- | --- | --- |
| 构建产物未误纳入 | `git status --short` | 不应出现 `target/`、`dist/`、`node_modules/`、`logs/` |
| 空白格式 | `git diff --check` | 必须通过 |
| URL token 风险 | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\security-gap-scan.ps1` | 不应出现 `url-token-risk` |
| 后端构建 | `cd backend; mvn clean package -DskipTests` | 提交 5、7 前必须通过 |
| 前端构建 | `cd frontend; npm run build:prod` | 提交 6、7 前必须通过 |
| MVP 验收 | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\daily-lkj-mvp-check.ps1` | 提交 7、8 前必须通过或明确阻塞原因 |

## 5. 不应进入任何提交的内容

| 类型 | 示例 |
| --- | --- |
| 构建产物 | `backend/**/target/`、`frontend/dist/` |
| 依赖目录 | `frontend/node_modules/` |
| 日志 | `logs/`、`*.log` |
| 本地凭据 | `.env.local`、真实数据库密码、真实 token、真实 OSS 密钥、私钥文件 |
| 本地 IDE 状态 | `.idea/workspace.xml`、`.vscode/` 本机配置 |

## 6. 推送和 PR 建议

1. 先在本地按顺序拆分提交。
2. 推送到远端前再次运行 `git diff --check` 和安全扫描。
3. 推送当前分支前，确认是否要保留已有 PR #1、#2，避免把不同历史的底座导入 PR 与本分支混合。
4. 建议为阶段 14 汇总创建新的 draft PR，正文引用本文件和 `handoff/CODEX_PHASE_REPORT.md`。
5. 不要自动合并 PR。
