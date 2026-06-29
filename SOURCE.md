# Source Boundary

本文件说明本仓库后续代码来源、参考边界和许可证处理规则，避免把原型代码、参考仓库和正式底座混在一起。

## 当前仓库状态

| 区域 | 当前状态 | 处理原则 |
| --- | --- | --- |
| `backend/` | 已导入 RuoYi-Vue-Plus 5.X 后端 | 作为正式后端工程底座，保留原 LICENSE 和来源说明 |
| `frontend/` | 已导入 plus-ui 5.X 前端 | 作为第一阶段正式前端工程底座，保留原 LICENSE 和来源说明 |
| `prototype/backend-cleanroom/` | Codex 生成的 cleanroom Spring Boot 原型，包名为 `com.analysisroom.platform` | 仅作为设计验证和参考，不作为正式运行主线 |
| `database/` | 当前是项目 SQL 草案 | 作为业务建模参考；迁移到 RuoYi 5.X 后需重新对齐正式框架迁移方式 |
| `docs/` | 当前包含权限、流程、API、数据库、日志、导入导出和样板业务文档 | 继续作为业务契约和实现约束 |

## 正式底座来源

正式 MVP 主线以 RuoYi-Vue-Plus 5.X 后端和匹配的 plus-ui 5.X 前端作为工程底座。

| 来源 | 用途 | 源仓库 | 分支 | commit | 本地目录 |
| --- | --- | --- | --- | --- | --- |
| RuoYi-Vue-Plus 5.X 后端 | 正式后端工程底座 | `https://github.com/dromara/RuoYi-Vue-Plus.git` | `5.X` | `e49f02f89e17ee5a4cc14048af99cc83d72872a7` | `backend/` |
| plus-ui 5.X 前端 | 正式前端工程底座 | `https://github.com/JavaLionLi/plus-ui.git` | `5.X` | `d0d451967676707021b9857df529c395b27e90a7` | `frontend/` |
| Art Design Pro | UI 风格和交互体验参考 | `https://github.com/Daymychen/art-design-pro.git` | 未导入源码 | 未导入源码 | 仅文档参考 |
| cleanroom 后端 | 原型参考 | Codex 生成 | 本地归档 | 非上游仓库 | `prototype/backend-cleanroom/` |

说明：

1. RuoYi-Vue-Plus 和 plus-ui 的 `.git` 目录没有复制进正式项目。
2. 两个正式底座均保留原始 `LICENSE`。
3. 本项目对默认配置做了安全收敛：数据库、Redis、SnailJob、第三方登录密钥使用环境变量或占位值，不提交真实密码、token、密钥。
4. Art Design Pro 当前只作为 UI 风格和交互参考，没有复制源码到本仓库。

## 许可证与版权规则

1. 不得删除 RuoYi-Vue-Plus、plus-ui、Art Design Pro 或其他来源项目的 LICENSE、版权和作者信息。
2. 导入第三方源码时必须保留来源说明。
3. 当前 cleanroom 原型不得伪装成 RuoYi 官方源码。
4. 正式后端/前端导入后，不得将 cleanroom 代码与 RuoYi 源码无边界混合。
5. 本项目业务文档、状态机、权限矩阵和每日 LKJ 样板业务属于 `analysis-room-platform` 项目约束，后续实现必须继续遵守。

## 后续 Codex 规则

后续 Codex 继续开发时应先判断当前目录是：

1. RuoYi 5.X 正式底座；
2. plus-ui/RuoYi 前端正式底座；
3. cleanroom 原型归档；
4. 项目业务文档。

不同来源的代码只能按明确边界参考和迁移，不允许无来源说明地复制、覆盖或混合。
