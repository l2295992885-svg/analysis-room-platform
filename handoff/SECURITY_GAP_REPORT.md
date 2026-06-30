# 安全缺口报告

更新时间：2026-06-29
阶段：阶段 14 上线前安全边界补强与工作区整理

## 1. 结论

本阶段已优先修复 URL 长期 token 暴露风险。SSE 和 WebSocket 改为服务端短期一次性 ticket，流程 iframe 不再拼接 `Authorization=Bearer ...`。自动扫描中未再出现 `url-token-risk` 分类，按脚本规则 URL token 风险为 0。

当前没有发现必须立即停止的真实密钥提交证据，但仍存在上线前必须处理的安全工作：生产密钥管理、访问日志 query 脱敏、HTTPS、真实组织数据权限复验、开发账号禁用或重置、CI 安全门禁。

## 2. 自动扫描结果

执行命令：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\acceptance\security-gap-scan.ps1
```

阶段 14 代码修复后结果：

| 类型 | 数量 | 说明 |
| --- | ---: | --- |
| `credential-keywords` | 674 | 命中 password、secret、token、Authorization 等关键词，主要包含模板字段、配置项、测试脚本和文档说明 |
| `url-token-risk` | 0 | 未再发现长期 token 拼 URL 的模式 |
| `private-key-marker` | 0 | 未发现私钥标记 |

说明：关键词命中不等同于真实密钥泄露。生产前仍需人工复核配置文件和部署环境变量。

## 3. URL token 风险处置

| 文件 | 原风险 | 当前处理 |
| --- | --- | --- |
| `frontend/src/utils/sse.ts` | `Authorization=Bearer <token>` 拼到 SSE URL | 改为请求 `/resource/sse/ticket` 获取短期 `sseTicket` |
| `frontend/src/utils/websocket.ts` | WebSocket URL 可能携带长期 token | 改为请求 `/resource/websocket/ticket` 获取短期 `wsTicket` |
| `frontend/src/components/Process/flowChart.vue` | iframe URL 拼接长期 token | 已移除 token query |
| `frontend/src/views/workflow/processDefinition/design.vue` | iframe URL 拼接长期 token | 已移除 token query |
| `SseController.java` | EventSource 原生不能带 header | 新增 30 秒 Redis ticket，连接时消费并删除 |
| `PlusWebSocketInterceptor.java` | 握手 token 可能出现在 URL | 支持短期 `wsTicket` 换取登录用户 |

## 4. 数据权限与导出附件安全

| 场景 | 当前状态 |
| --- | --- |
| 记录列表/详情 | 已叠加每日 LKJ 记录级数据权限 |
| 流程日志/反馈 | 先校验记录可读 |
| 附件列表/下载 | 先校验记录可读和附件绑定 |
| 记录导出 | 导出复用数据范围过滤 |
| 结果库导出 | 结果库查询叠加记录级数据范围 |
| 待办/信箱/聊天卡片打开业务 | 打开时重新校验每日 LKJ 业务数据权限 |

本轮 `daily-lkj-mvp-check.ps1` 已验证非接收人待办打开 403、非授权附件下载 403、无权限结果库导出 403、聊天业务卡片打开重新校验。

## 5. 配置与密钥检查

| 检查项 | 当前结果 | 上线要求 |
| --- | --- | --- |
| 数据库密码 | 本地验收使用开发容器默认值 | 生产必须通过环境变量或密钥管理注入 |
| Redis 密码 | 本地验收使用开发容器默认值 | 生产必须配置强密码或受控网络 |
| JWT secret | 本地验收使用开发占位值 | 生产必须覆盖为高强度 secret |
| OSS accessKey/secretKey | 模板字段存在 | 不得提交真实密钥 |
| 前端 RSA key | 当前为空或占位 | 不得提交真实私钥 |
| 前端 client id | 固定公开客户端标识 | 不能作为安全边界 |
| 开发测试用户 | 存在 `*_test` 用户和开发密码 hash | 生产必须删除、禁用或强制重置 |

## 6. 本轮安全验收

| 验收项 | 结果 |
| --- | --- |
| URL token 风险扫描 | 0 |
| SSE 长连接 | 短期 ticket，不暴露长期 token |
| WebSocket 握手 | 短期 ticket，不暴露长期 token |
| iframe URL | 不再带长期 token |
| 未登录访问 | 返回 401 |
| 无权限访问 | 返回 403 |
| 导出权限 | 后端权限和数据范围双重校验 |
| 附件下载 | 后端记录级数据权限校验 |

## 7. 仍需上线前处理

1. 对网关、后端访问日志、异常日志中的 URL query 做脱敏。
2. 生产环境统一使用密钥管理或环境变量注入数据库、Redis、JWT、OSS、第三方登录 secret。
3. 禁用或重置所有开发测试账号。
4. 用真实组织关系复验分析员、班长、主任、车间、车队、指导组的数据边界。
5. 将安全扫描、构建、每日 LKJ 验收接入 CI。
6. 配置 HTTPS、备份、监控告警和审计日志留存策略。

## 2026-06-30 增量补强：认证异常日志脱敏

本轮复核时发现 Sa-Token 认证失败日志在“token 无效”场景下可能输出 token 原文。已新增 `LogSanitizer`，并接入 Sa-Token 异常处理、MyBatis 包装认证异常、SSE 认证异常、WebSocket 认证失败日志，以及请求参数日志的敏感字段过滤。

验证结果：

| 验证项 | 结果 |
| --- | --- |
| 伪造 JWT 访问受保护接口 | 返回 401 |
| 后端认证失败日志 | `token 无效：[REDACTED]`，未输出 token 原文 |
| `mvn clean package -DskipTests` | 通过，36 个 Maven 模块成功，总耗时 47.733s |
| `npm run build:prod` | 通过，构建耗时 16.48s，仍有大 chunk 警告 |
| `daily-lkj-mvp-check.ps1` | `PASS=56, FAIL=0, TODO=1, SKIP=0`；脚本因 TODO 返回非零，但无失败项 |
| `security-gap-scan.ps1` | 未出现 `url-token-risk`；`credential-keywords=681` 需人工分类 |
| `git diff --check` | 通过，仅 Windows LF/CRLF 工作区警告 |

剩余风险：生产上线前仍需在网关、反向代理、容器日志采集、访问日志格式和异常日志平台上统一做 header/query/body 脱敏策略，并禁用或重置开发测试账号。