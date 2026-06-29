# Backend Source

本目录为综合分析室数据分析平台的正式后端底座。

| 项 | 内容 |
| --- | --- |
| 来源项目 | RuoYi-Vue-Plus |
| 源仓库 | `https://github.com/dromara/RuoYi-Vue-Plus.git` |
| 镜像源 | `https://gitee.com/dromara/RuoYi-Vue-Plus.git` |
| 分支 | `5.X` |
| 导入 commit | `e49f02f89e17ee5a4cc14048af99cc83d72872a7` |
| 本地目录 | `backend/` |
| 许可证 | 保留原始 `LICENSE` |

## 本项目处理

1. 未复制上游 `.git` 目录。
2. 保留 RuoYi-Vue-Plus 原始 LICENSE、README、目录结构和版权说明。
3. 默认配置已改为环境变量或占位值，避免提交真实数据库密码、Redis 密码、SnailJob token、第三方登录密钥。
4. 当前阶段仅完成底座导入，不新增每日 LKJ 业务代码。
5. 后续业务开发应在 RuoYi 5.X 成熟权限、安全、日志、Excel、文件、代码生成能力之上扩展。

## cleanroom 原型边界

此前 Codex 生成的自研 Spring Boot 原型已归档到：

```text
prototype/backend-cleanroom/
```

该原型只用于理解接口约定、权限矩阵、日志和系统管理验证思路，不作为正式运行主线，也不得与 RuoYi 主线无边界混合。
