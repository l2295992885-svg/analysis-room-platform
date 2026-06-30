# RuoYi-Vue-Plus Controller Inventory

本文是从 RuoYi-Vue-Plus 本地参考仓库抽取的 Controller 接口清单，用于后续逐接口查阅。业务设计说明见 `docs/RUOYI_VUE_PLUS_CODE_READING.md`。

说明：

- 抽取范围：`ruoyi-admin/src/main/java/**/*Controller.java` 和 `ruoyi-modules/**/src/main/java/**/*Controller.java`
- Controller 数量：49
- Endpoint 数量：302
- `Permission` 来源于 `@SaCheckPermission` 或 `@SaCheckRole`
- `Notes` 中 `log` 表示 `@Log`，`repeat-submit` 表示 `@RepeatSubmit`，`rate-limit` 表示 `@RateLimiter`

## ruoyi-admin

### AuthController `/auth`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| POST | `/auth/login` | `login` | `-` | - |
| GET | `/auth/binding/{source}` | `authBinding` | `-` | - |
| POST | `/auth/social/callback` | `socialCallback` | `-` | - |
| DELETE | `/auth/unlock/{socialId}` | `unlockSocial` | `-` | - |
| POST | `/auth/logout` | `logout` | `-` | - |
| POST | `/auth/register` | `register` | `-` | - |
| GET | `/auth/tenant/list` | `tenantList` | `-` | rate-limit |

### CaptchaController `/`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/resource/sms/code` | `smsCode` | `-` | rate-limit |
| GET | `/resource/email/code` | `emailCode` | `-` | - |
| GET | `/auth/code` | `getCode` | `-` | - |

### IndexController `/`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/` | `index` | `-` | - |

## ruoyi-modules/ruoyi-system

### CacheController `/monitor/cache`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/monitor/cache` | `getInfo` | `monitor:cache:list` | - |

### SysLogininforController `/monitor/logininfor`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/monitor/logininfor/list` | `list` | `monitor:logininfor:list` | - |
| POST | `/monitor/logininfor/export` | `export` | `monitor:logininfor:export` | log |
| DELETE | `/monitor/logininfor/{infoIds}` | `remove` | `monitor:logininfor:remove` | log |
| DELETE | `/monitor/logininfor/clean` | `clean` | `monitor:logininfor:remove` | log |
| GET | `/monitor/logininfor/unlock/{userName}` | `unlock` | `monitor:logininfor:unlock` | log, repeat-submit |

### SysOperlogController `/monitor/operlog`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/monitor/operlog/list` | `list` | `monitor:operlog:list` | - |
| POST | `/monitor/operlog/export` | `export` | `monitor:operlog:export` | log |
| DELETE | `/monitor/operlog/{operIds}` | `remove` | `monitor:operlog:remove` | log |
| DELETE | `/monitor/operlog/clean` | `clean` | `monitor:operlog:remove` | log |

### SysUserOnlineController `/monitor/online`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/monitor/online/list` | `list` | `monitor:online:list` | - |
| DELETE | `/monitor/online/{tokenId}` | `forceLogout` | `monitor:online:forceLogout` | log, repeat-submit |
| GET | `/monitor/online` | `getInfo` | `-` | - |
| DELETE | `/monitor/online/myself/{tokenId}` | `remove` | `-` | log, repeat-submit |

### SysClientController `/system/client`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/client/list` | `list` | `system:client:list` | - |
| POST | `/system/client/export` | `export` | `system:client:export` | log |
| GET | `/system/client/{id}` | `getInfo` | `system:client:query` | - |
| POST | `/system/client` | `add` | `system:client:add` | log, repeat-submit |
| PUT | `/system/client` | `edit` | `system:client:edit` | log, repeat-submit |
| PUT | `/system/client/changeStatus` | `changeStatus` | `system:client:edit` | log |
| DELETE | `/system/client/{ids}` | `remove` | `system:client:remove` | log |

### SysConfigController `/system/config`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/config/list` | `list` | `system:config:list` | - |
| POST | `/system/config/export` | `export` | `system:config:export` | log |
| GET | `/system/config/{configId}` | `getInfo` | `system:config:query` | - |
| GET | `/system/config/configKey/{configKey}` | `getConfigKey` | `-` | - |
| POST | `/system/config` | `add` | `system:config:add` | log, repeat-submit |
| PUT | `/system/config` | `edit` | `system:config:edit` | log, repeat-submit |
| PUT | `/system/config/updateByKey` | `updateByKey` | `system:config:edit` | log, repeat-submit |
| DELETE | `/system/config/{configIds}` | `remove` | `system:config:remove` | log |
| DELETE | `/system/config/refreshCache` | `refreshCache` | `system:config:remove` | log |

### SysDeptController `/system/dept`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/dept/list` | `list` | `system:dept:list` | - |
| GET | `/system/dept/list/exclude/{deptId}` | `excludeChild` | `system:dept:list` | - |
| GET | `/system/dept/{deptId}` | `getInfo` | `system:dept:query` | - |
| POST | `/system/dept` | `add` | `system:dept:add` | log, repeat-submit |
| PUT | `/system/dept` | `edit` | `system:dept:edit` | log, repeat-submit |
| DELETE | `/system/dept/{deptId}` | `remove` | `system:dept:remove` | log |
| GET | `/system/dept/optionselect` | `optionselect` | `system:dept:query` | - |

### SysDictDataController `/system/dict/data`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/dict/data/list` | `list` | `system:dict:list` | - |
| POST | `/system/dict/data/export` | `export` | `system:dict:export` | log |
| GET | `/system/dict/data/{dictCode}` | `getInfo` | `system:dict:query` | - |
| GET | `/system/dict/data/type/{dictType}` | `dictType` | `-` | - |
| POST | `/system/dict/data` | `add` | `system:dict:add` | log, repeat-submit |
| PUT | `/system/dict/data` | `edit` | `system:dict:edit` | log, repeat-submit |
| DELETE | `/system/dict/data/{dictCodes}` | `remove` | `system:dict:remove` | log |

### SysDictTypeController `/system/dict/type`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/dict/type/list` | `list` | `system:dict:list` | - |
| POST | `/system/dict/type/export` | `export` | `system:dict:export` | log |
| GET | `/system/dict/type/{dictId}` | `getInfo` | `system:dict:query` | - |
| POST | `/system/dict/type` | `add` | `system:dict:add` | log, repeat-submit |
| PUT | `/system/dict/type` | `edit` | `system:dict:edit` | log, repeat-submit |
| DELETE | `/system/dict/type/{dictIds}` | `remove` | `system:dict:remove` | log |
| DELETE | `/system/dict/type/refreshCache` | `refreshCache` | `system:dict:remove` | log |
| GET | `/system/dict/type/optionselect` | `optionselect` | `-` | - |

### SysMenuController `/system/menu`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/menu/getRouters` | `getRouters` | `-` | - |
| GET | `/system/menu/list` | `list` | `system:menu:list` | - |
| GET | `/system/menu/{menuId}` | `getInfo` | `system:menu:query` | - |
| GET | `/system/menu/treeselect` | `treeselect` | `system:menu:query` | - |
| GET | `/system/menu/roleMenuTreeselect/{roleId}` | `roleMenuTreeselect` | `system:menu:query` | - |
| GET | `/system/menu/tenantPackageMenuTreeselect/{packageId}` | `tenantPackageMenuTreeselect` | `system:menu:query` | - |
| POST | `/system/menu` | `add` | `system:menu:add` | log, repeat-submit |
| PUT | `/system/menu` | `edit` | `system:menu:edit` | log, repeat-submit |
| DELETE | `/system/menu/{menuId}` | `remove` | `system:menu:remove` | log |
| DELETE | `/system/menu/cascade/{menuIds}` | `remove` | `system:menu:remove` | log |

### SysNoticeController `/system/notice`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/notice/list` | `list` | `system:notice:list` | - |
| GET | `/system/notice/{noticeId}` | `getInfo` | `system:notice:query` | - |
| POST | `/system/notice` | `add` | `system:notice:add` | log, repeat-submit |
| PUT | `/system/notice` | `edit` | `system:notice:edit` | log, repeat-submit |
| DELETE | `/system/notice/{noticeIds}` | `remove` | `system:notice:remove` | log |

### SysOssConfigController `/resource/oss/config`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/resource/oss/config/list` | `list` | `system:ossConfig:list` | - |
| GET | `/resource/oss/config/{ossConfigId}` | `getInfo` | `system:ossConfig:list` | - |
| POST | `/resource/oss/config` | `add` | `system:ossConfig:add` | log, repeat-submit |
| PUT | `/resource/oss/config` | `edit` | `system:ossConfig:edit` | log, repeat-submit |
| DELETE | `/resource/oss/config/{ossConfigIds}` | `remove` | `system:ossConfig:remove` | log |
| PUT | `/resource/oss/config/changeStatus` | `changeStatus` | `system:ossConfig:edit` | log, repeat-submit |

### SysOssController `/resource/oss`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/resource/oss/list` | `list` | `system:oss:list` | - |
| GET | `/resource/oss/listByIds/{ossIds}` | `listByIds` | `system:oss:query` | - |
| POST | `/resource/oss/upload` | `upload` | `system:oss:upload` | log |
| GET | `/resource/oss/download/{ossId}` | `download` | `system:oss:download` | - |
| DELETE | `/resource/oss/{ossIds}` | `remove` | `system:oss:remove` | log |

### SysPostController `/system/post`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/post/list` | `list` | `system:post:list` | - |
| POST | `/system/post/export` | `export` | `system:post:export` | log |
| GET | `/system/post/{postId}` | `getInfo` | `system:post:query` | - |
| POST | `/system/post` | `add` | `system:post:add` | log, repeat-submit |
| PUT | `/system/post` | `edit` | `system:post:edit` | log, repeat-submit |
| DELETE | `/system/post/{postIds}` | `remove` | `system:post:remove` | log |
| GET | `/system/post/optionselect` | `optionselect` | `system:post:query` | - |
| GET | `/system/post/deptTree` | `deptTree` | `system:post:list` | - |

### SysProfileController `/system/user/profile`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/user/profile` | `profile` | `-` | - |
| PUT | `/system/user/profile` | `updateProfile` | `-` | repeat-submit, log |
| PUT | `/system/user/profile/updatePwd` | `updatePwd` | `-` | repeat-submit, log |
| POST | `/system/user/profile/avatar` | `avatar` | `-` | repeat-submit, log |

### SysRoleController `/system/role`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/role/list` | `list` | `system:role:list` | - |
| POST | `/system/role/export` | `export` | `system:role:export` | log |
| GET | `/system/role/{roleId}` | `getInfo` | `system:role:query` | - |
| POST | `/system/role` | `add` | `system:role:add` | log, repeat-submit |
| PUT | `/system/role` | `edit` | `system:role:edit` | log, repeat-submit |
| PUT | `/system/role/dataScope` | `dataScope` | `system:role:edit` | log, repeat-submit |
| PUT | `/system/role/changeStatus` | `changeStatus` | `system:role:edit` | log, repeat-submit |
| DELETE | `/system/role/{roleIds}` | `remove` | `system:role:remove` | log |
| GET | `/system/role/optionselect` | `optionselect` | `system:role:query` | - |
| GET | `/system/role/authUser/allocatedList` | `allocatedList` | `system:role:list` | - |
| GET | `/system/role/authUser/unallocatedList` | `unallocatedList` | `system:role:list` | - |
| PUT | `/system/role/authUser/cancel` | `cancelAuthUser` | `system:role:edit` | log, repeat-submit |
| PUT | `/system/role/authUser/cancelAll` | `cancelAuthUserAll` | `system:role:edit` | log, repeat-submit |
| PUT | `/system/role/authUser/selectAll` | `selectAuthUserAll` | `system:role:edit` | log, repeat-submit |
| GET | `/system/role/deptTree/{roleId}` | `roleDeptTreeselect` | `system:role:list` | - |

### SysSocialController `/system/social`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/social/list` | `list` | `-` | - |

### SysTenantController `/system/tenant`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/tenant/list` | `list` | `system:tenant:list` | - |
| POST | `/system/tenant/export` | `export` | `system:tenant:export` | log |
| GET | `/system/tenant/{id}` | `getInfo` | `system:tenant:query` | - |
| POST | `/system/tenant` | `add` | `system:tenant:add` | log, repeat-submit |
| PUT | `/system/tenant` | `edit` | `system:tenant:edit` | log, repeat-submit |
| PUT | `/system/tenant/changeStatus` | `changeStatus` | `system:tenant:edit` | log, repeat-submit |
| DELETE | `/system/tenant/{ids}` | `remove` | `system:tenant:remove` | log |
| GET | `/system/tenant/dynamic/{tenantId}` | `dynamicTenant` | `-` | - |
| GET | `/system/tenant/dynamic/clear` | `dynamicClear` | `-` | - |
| GET | `/system/tenant/syncTenantPackage` | `syncTenantPackage` | `system:tenant:edit` | log |
| GET | `/system/tenant/syncTenantDict` | `syncTenantDict` | `-` | log |
| GET | `/system/tenant/syncTenantConfig` | `syncTenantConfig` | `-` | log |

### SysTenantPackageController `/system/tenant/package`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/tenant/package/list` | `list` | `system:tenantPackage:list` | - |
| GET | `/system/tenant/package/selectList` | `selectList` | `system:tenantPackage:list` | - |
| POST | `/system/tenant/package/export` | `export` | `system:tenantPackage:export` | log |
| GET | `/system/tenant/package/{packageId}` | `getInfo` | `system:tenantPackage:query` | - |
| POST | `/system/tenant/package` | `add` | `system:tenantPackage:add` | log, repeat-submit |
| PUT | `/system/tenant/package` | `edit` | `system:tenantPackage:edit` | log, repeat-submit |
| PUT | `/system/tenant/package/changeStatus` | `changeStatus` | `system:tenantPackage:edit` | log, repeat-submit |
| DELETE | `/system/tenant/package/{packageIds}` | `remove` | `system:tenantPackage:remove` | log |

### SysUserController `/system/user`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/system/user/list` | `list` | `system:user:list` | - |
| POST | `/system/user/export` | `export` | `system:user:export` | log |
| POST | `/system/user/importData` | `importData` | `system:user:import` | log |
| POST | `/system/user/importTemplate` | `importTemplate` | `-` | - |
| GET | `/system/user/getInfo` | `getInfo` | `-` | - |
| GET | `/system/user/` | `getInfo` | `system:user:query` | - |
| GET | `/system/user/{userId}` | `getInfo` | `system:user:query` | - |
| POST | `/system/user` | `add` | `system:user:add` | log, repeat-submit |
| PUT | `/system/user` | `edit` | `system:user:edit` | log, repeat-submit |
| DELETE | `/system/user/{userIds}` | `remove` | `system:user:remove` | log |
| GET | `/system/user/optionselect` | `optionselect` | `system:user:query` | - |
| PUT | `/system/user/resetPwd` | `resetPwd` | `system:user:resetPwd` | log, repeat-submit |
| PUT | `/system/user/changeStatus` | `changeStatus` | `system:user:edit` | log, repeat-submit |
| GET | `/system/user/authRole/{userId}` | `authRole` | `system:user:query` | - |
| PUT | `/system/user/authRole` | `insertAuthRole` | `system:user:edit` | log, repeat-submit |
| GET | `/system/user/deptTree` | `deptTree` | `system:user:list` | - |
| GET | `/system/user/list/dept/{deptId}` | `listByDept` | `system:user:list` | - |

## ruoyi-modules/ruoyi-generator

### GenController `/tool/gen`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/tool/gen/list` | `genList` | `tool:gen:list` | - |
| GET | `/tool/gen/{tableId}` | `getInfo` | `tool:gen:query` | repeat-submit |
| GET | `/tool/gen/db/list` | `dataList` | `tool:gen:list` | - |
| GET | `/tool/gen/column/{tableId}` | `columnList` | `tool:gen:list` | - |
| POST | `/tool/gen/importTable` | `importTableSave` | `tool:gen:import` | log, repeat-submit |
| PUT | `/tool/gen` | `editSave` | `tool:gen:edit` | log, repeat-submit |
| DELETE | `/tool/gen/{tableIds}` | `remove` | `tool:gen:remove` | log |
| GET | `/tool/gen/preview/{tableId}` | `preview` | `tool:gen:preview` | - |
| GET | `/tool/gen/download/{tableId}` | `download` | `tool:gen:code` | log |
| GET | `/tool/gen/synchDb/{tableId}` | `synchDb` | `tool:gen:edit` | log |
| GET | `/tool/gen/batchGenCode` | `batchGenCode` | `tool:gen:code` | log |
| GET | `/tool/gen/getDataNames` | `getCurrentDataSourceNameList` | `tool:gen:list` | - |

## ruoyi-modules/ruoyi-demo

| Controller | Base path | Endpoint count | Interface scope |
| --- | --- | ---: | --- |
| `MailSendController` | `/demo/mail` | 3 | `sendSimpleMessage`、`sendMessageWithAttachment`、`sendMessageWithAttachments` |
| `BoundedQueueController` | `/demo/queue/bounded` | 3 | `add`、`remove`、`get` |
| `DelayedQueueController` | `/demo/queue/delayed` | 4 | `subscribe`、`add`、`remove`、`destroy` |
| `PriorityQueueController` | `/demo/queue/priority` | 3 | `add`、`remove`、`get` |
| `RedisCacheController` | `/demo/cache` | 4 | `test1`、`test2`、`test3`、`test6` |
| `RedisLockController` | `/demo/redisLock` | 2 | `testLock4j`、`testLock4jLockTemplate` |
| `RedisPubSubController` | `/demo/redis/pubsub` | 2 | `pub`、`sub` |
| `RedisRateLimiterController` | `/demo/rateLimiter` | 4 | `test`、`testip`、`testcluster`、`testObj` |
| `SaTokenTestController` | `/demo/saTokenDoc` | 16 | Sa-Token 登录、角色、权限、通配符、临时权限演示 |
| `SmsController` | `/demo/sms` | 4 | `sendAliyun`、`sendTencent`、`addBlacklist`、`removeBlacklist` |
| `Swagger3DemoController` | `/swagger/demo` | 1 | `upload` |
| `TestBatchController` | `/demo/batch` | 3 | `add`、`addOrUpdate`、`remove` |
| `TestDemoController` | `/demo/demo` | 8 | `list`、`page`、`importData`、`export`、`getInfo`、`add`、`edit`、`remove` |
| `TestEncryptController` | `/demo/encrypt` | 1 | `test` |
| `TestExcelController` | `/demo/excel` | 6 | `exportTemplateOne`、`exportTemplateMuliti`、`exportWithOptions`、`customExport`、`exportTemplateMultiSheet`、`importWithOptions` |
| `TestI18nController` | `/demo/i18n` | 3 | `get`、`test1`、`test2` |
| `TestSensitiveController` | `/demo/sensitive` | 1 | `test` |
| `TestTreeController` | `/demo/tree` | 6 | `list`、`export`、`getInfo`、`add`、`edit`、`remove` |
| `WebSocketController` | `/demo/websocket` | 1 | `send` |

## ruoyi-modules/ruoyi-workflow

### FlwCategoryController `/workflow/category`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/workflow/category/list` | `list` | `workflow:category:list` | - |
| POST | `/workflow/category/export` | `export` | `workflow:category:export` | log |
| GET | `/workflow/category/{categoryId}` | `getInfo` | `workflow:category:query` | - |
| POST | `/workflow/category` | `add` | `workflow:category:add` | log, repeat-submit |
| PUT | `/workflow/category` | `edit` | `workflow:category:edit` | log, repeat-submit |
| DELETE | `/workflow/category/{categoryId}` | `remove` | `workflow:category:remove` | log |
| GET | `/workflow/category/categoryTree` | `categoryTree` | `-` | - |

### FlwDefinitionController `/workflow/definition`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/workflow/definition/list` | `list` | `workflow:definition:list` | - |
| GET | `/workflow/definition/unPublishList` | `unPublishList` | `workflow:definition:list` | - |
| GET | `/workflow/definition/{id}` | `getInfo` | `workflow:definition:query` | - |
| POST | `/workflow/definition` | `add` | `workflow:definition:add` | log, repeat-submit |
| PUT | `/workflow/definition` | `edit` | `workflow:definition:edit` | log, repeat-submit |
| PUT | `/workflow/definition/publish/{id}` | `publish` | `workflow:definition:publish` | log, repeat-submit |
| PUT | `/workflow/definition/unPublish/{id}` | `unPublish` | `workflow:definition:publish` | log, repeat-submit |
| DELETE | `/workflow/definition/{ids}` | `remove` | `workflow:definition:remove` | log |
| POST | `/workflow/definition/copy/{id}` | `copy` | `workflow:definition:copy` | log, repeat-submit |
| POST | `/workflow/definition/importDef` | `importDef` | `workflow:definition:import` | log |
| POST | `/workflow/definition/exportDef/{id}` | `exportDef` | `workflow:definition:export` | log |
| GET | `/workflow/definition/xmlString/{id}` | `xmlString` | `workflow:definition:query` | - |
| PUT | `/workflow/definition/active/{id}` | `active` | `workflow:definition:active` | repeat-submit, log |

### FlwInstanceController `/workflow/instance`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/workflow/instance/pageByRunning` | `selectRunningInstanceList` | `workflow:instance:list` | - |
| GET | `/workflow/instance/pageByFinish` | `selectFinishInstanceList` | `workflow:instance:list` | - |
| GET | `/workflow/instance/getInfo/{businessId}` | `getInfo` | `workflow:instance:query` | - |
| DELETE | `/workflow/instance/deleteByBusinessIds/{businessIds}` | `deleteByBusinessIds` | `workflow:instance:remove` | log |
| DELETE | `/workflow/instance/deleteByInstanceIds/{instanceIds}` | `deleteByInstanceIds` | `workflow:instance:remove` | log |
| DELETE | `/workflow/instance/deleteHisByInstanceIds/{instanceIds}` | `deleteHisByInstanceIds` | `workflow:instance:remove` | log |
| PUT | `/workflow/instance/cancelProcessApply` | `cancelProcessApply` | `workflow:instance:cancel` | repeat-submit, log |
| PUT | `/workflow/instance/active/{id}` | `active` | `workflow:instance:active` | repeat-submit, log |
| GET | `/workflow/instance/pageByCurrent` | `selectCurrentInstanceList` | `workflow:instance:currentList` | - |
| GET | `/workflow/instance/flowHisTaskList/{businessId}` | `flowHisTaskList` | `workflow:instance:query` | - |
| GET | `/workflow/instance/instanceVariable/{instanceId}` | `instanceVariable` | `workflow:instance:variableQuery` | - |
| PUT | `/workflow/instance/updateVariable` | `updateVariable` | `workflow:instance:variable` | repeat-submit, log |
| POST | `/workflow/instance/invalid` | `invalid` | `workflow:instance:invalid` | log, repeat-submit |

### FlwSpelController `/workflow/spel`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/workflow/spel/list` | `list` | `workflow:spel:list` | - |
| GET | `/workflow/spel/{id}` | `getInfo` | `workflow:spel:query` | - |
| POST | `/workflow/spel` | `add` | `workflow:spel:add` | log, repeat-submit |
| PUT | `/workflow/spel` | `edit` | `workflow:spel:edit` | log, repeat-submit |
| DELETE | `/workflow/spel/{ids}` | `remove` | `workflow:spel:remove` | log |

### FlwTaskController `/workflow/task`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| POST | `/workflow/task/startWorkFlow` | `startWorkFlow` | `-` | log, repeat-submit |
| POST | `/workflow/task/completeTask` | `completeTask` | `-` | log, repeat-submit |
| GET | `/workflow/task/pageByTaskWait` | `pageByTaskWait` | `-` | - |
| GET | `/workflow/task/pageByTaskFinish` | `pageByTaskFinish` | `-` | - |
| GET | `/workflow/task/pageByAllTaskWait` | `pageByAllTaskWait` | `workflow:task:list` | - |
| GET | `/workflow/task/pageByAllTaskFinish` | `pageByAllTaskFinish` | `workflow:task:list` | - |
| GET | `/workflow/task/pageByTaskCopy` | `pageByTaskCopy` | `-` | - |
| GET | `/workflow/task/getTask/{taskId}` | `getTask` | `-` | - |
| POST | `/workflow/task/getNextNodeList` | `getNextNodeList` | `-` | - |
| POST | `/workflow/task/terminationTask` | `terminationTask` | `-` | log, repeat-submit |
| POST | `/workflow/task/taskOperation/{taskOperation}` | `taskOperation` | `-` | log, repeat-submit |
| PUT | `/workflow/task/updateAssignee/{userId}` | `updateAssignee` | `workflow:task:edit` | log, repeat-submit |
| POST | `/workflow/task/backProcess` | `backProcess` | `-` | log, repeat-submit |
| GET | `/workflow/task/getBackTaskNode/{taskId}/{nowNodeCode}` | `getBackTaskNode` | `-` | - |
| GET | `/workflow/task/currentTaskAllUser/{taskId}` | `currentTaskAllUser` | `-` | - |
| POST | `/workflow/task/urgeTask` | `urgeTask` | `workflow:task:edit` | log |

### TestLeaveController `/workflow/leave`

| HTTP | Path | Handler | Permission | Notes |
| --- | --- | --- | --- | --- |
| GET | `/workflow/leave/list` | `list` | `workflow:leave:list` | - |
| POST | `/workflow/leave/export` | `export` | `workflow:leave:export` | log |
| GET | `/workflow/leave/{id}` | `getInfo` | `workflow:leave:query` | - |
| POST | `/workflow/leave` | `add` | `workflow:leave:add` | log, repeat-submit |
| POST | `/workflow/leave/submitAndFlowStart` | `submitAndFlowStart` | `workflow:leave:add` | log, repeat-submit |
| PUT | `/workflow/leave` | `edit` | `workflow:leave:edit` | log, repeat-submit |
| DELETE | `/workflow/leave/{ids}` | `remove` | `workflow:leave:remove` | log |
