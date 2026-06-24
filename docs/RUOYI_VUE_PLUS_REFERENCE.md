# RuoYi-Vue-Plus 参考说明

## 参考仓库

- 源仓库：https://gitee.com/dromara/RuoYi-Vue-Plus.git
- GitHub 镜像仓库：https://github.com/l2295992885-svg/ruoyi-vue-plus-reference.git
- 用途：作为 `analysis-room-platform` 后端底座设计参考仓库。

该仓库仅作为后端权限、安全、日志、数据权限、Excel、文件存储、代码生成器等成熟设计参考。

严禁直接把 RuoYi-Vue-Plus 源码复制进本项目，严禁把本项目改造成 RuoYi-Vue-Plus。

## 第一阶段重点参考

第一阶段重点参考以下能力：

- 用户管理
- 角色管理
- 部门管理
- 菜单权限
- 按钮权限
- 数据权限
- 登录日志
- 操作日志
- 文件上传
- Excel 导入导出
- 代码生成器

第一阶段暂不引入以下能力：

- 多租户
- 多数据源
- 复杂工作流
- 分布式任务
- 链路追踪
- 大规模监控体系

## RuoYi-Vue-Plus 目录结构分析

RuoYi-Vue-Plus 是 Spring Boot 多模块后端项目，主要目录如下：

- `ruoyi-admin`：应用启动入口和 Web 聚合模块。包含 `DromaraApplication`、登录认证控制器、登录服务、运行配置和打包配置。
- `ruoyi-common`：通用基础能力模块。包含 core、web、security、satoken、mybatis、log、excel、oss、redis、tenant 等横切能力。
- `ruoyi-extend`：扩展服务模块。主要包含 Spring Boot Admin 监控服务和 SnailJob 服务端，第一阶段不建议引入。
- `ruoyi-modules`：业务与平台模块。第一阶段重点参考 `ruoyi-system`；代码生成器参考 `ruoyi-generator`。`ruoyi-job`、`ruoyi-demo`、`ruoyi-workflow` 不建议进入第一阶段。
- `script`：启动脚本、Docker 配置、数据库初始化脚本和版本迁移脚本。

## 关键参考位置

用户、角色、部门、菜单、权限：

- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysUserController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysRoleController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysDeptController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysMenuController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/SysPermissionServiceImpl.java`
- `ruoyi-common/ruoyi-common-satoken/src/main/java/org/dromara/common/satoken/core/service/SaPermissionImpl.java`
- `script/sql/ry_vue_5.X.sql`

数据权限：

- `ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/annotation/DataPermission.java`
- `ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/annotation/DataColumn.java`
- `ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/aspect/DataPermissionAdvice.java`
- `ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/handler/PlusDataPermissionHandler.java`
- `ruoyi-common/ruoyi-common-mybatis/src/main/java/org/dromara/common/mybatis/interceptor/PlusDataPermissionInterceptor.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/SysDataScopeServiceImpl.java`

登录日志、操作日志：

- `ruoyi-common/ruoyi-common-log/src/main/java/org/dromara/common/log/annotation/Log.java`
- `ruoyi-common/ruoyi-common-log/src/main/java/org/dromara/common/log/aspect/LogAspect.java`
- `ruoyi-common/ruoyi-common-log/src/main/java/org/dromara/common/log/event/LogininforEvent.java`
- `ruoyi-common/ruoyi-common-log/src/main/java/org/dromara/common/log/event/OperLogEvent.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/monitor/SysLogininforController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/monitor/SysOperlogController.java`

Excel 导入导出：

- `ruoyi-common/ruoyi-common-excel/src/main/java/org/dromara/common/excel/utils/ExcelUtil.java`
- `ruoyi-common/ruoyi-common-excel/src/main/java/org/dromara/common/excel/core/DefaultExcelListener.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/listener/SysUserImportListener.java`
- `ruoyi-modules/ruoyi-demo/src/main/java/org/dromara/demo/controller/TestDemoController.java`

文件上传、OSS、MinIO：

- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysOssController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/controller/system/SysOssConfigController.java`
- `ruoyi-modules/ruoyi-system/src/main/java/org/dromara/system/service/impl/SysOssServiceImpl.java`
- `ruoyi-common/ruoyi-common-oss/src/main/java/org/dromara/common/oss/core/OssClient.java`
- `ruoyi-common/ruoyi-common-oss/src/main/java/org/dromara/common/oss/factory/OssFactory.java`

代码生成器：

- `ruoyi-modules/ruoyi-generator/src/main/java/org/dromara/generator/controller/GenController.java`
- `ruoyi-modules/ruoyi-generator/src/main/java/org/dromara/generator/service/GenTableServiceImpl.java`
- `ruoyi-modules/ruoyi-generator/src/main/resources/vm/`
- `ruoyi-modules/ruoyi-generator/src/main/resources/generator.yml`

## Codex 后续参考方式

后续让 Codex 实现后端能力时，应按以下方式使用该参考仓库：

1. 先阅读本文档和对应 RuoYi-Vue-Plus 参考路径。
2. 只抽取设计思路、命名方式、表关系、权限模式和日志模式。
3. 在本项目自己的包名、目录、实体、接口和数据库脚本内重新实现。
4. 第一阶段保持简单 Spring Boot 后端、MySQL 8.x、用户/角色/部门/菜单/按钮/数据权限、日志、文件上传、Excel、代码生成器参考能力。
5. 不复制 RuoYi-Vue-Plus 源文件、License、包名、生成代码或可选平台复杂度。

## 后端初始化建议目录

建议第一阶段后端目录：

```text
backend/
  pom.xml
  src/main/java/
    com/analysisroom/platform/
      AnalysisRoomApplication.java
      common/
        core/
        web/
        security/
        log/
        excel/
        file/
        mybatis/
      system/
        user/
        role/
        dept/
        menu/
        permission/
        log/
      business/
        violation/
        attachment/
        inbox/
        todo/
      generator/
  src/main/resources/
    application.yml
    application-dev.yml
    mapper/
database/
  mysql/
    init_schema.sql
    init_data.sql
```

第一条后端主线应先建立认证、后端权限校验、数据权限过滤、登录/操作日志、附件上传元数据、Excel 导入导出工具和最小代码生成约定，再实现每日 LKJ 音视频违标公示业务闭环。
