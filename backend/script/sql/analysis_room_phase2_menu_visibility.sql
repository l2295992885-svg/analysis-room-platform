-- 综合分析室平台阶段 2 菜单显隐配置
-- 只隐藏暂不作为正式业务入口的上游默认菜单，不删除菜单、权限码或源码。
-- sys_menu.visible: 0 显示，1 隐藏。

UPDATE sys_menu
SET visible = '1',
    update_time = sysdate(),
    remark = CONCAT(IFNULL(remark, ''), '；综合分析室阶段2隐藏')
WHERE menu_id IN (
    2,     -- 系统监控
    4,     -- PLUS官网
    5,     -- 测试菜单
    6,     -- 租户管理
    117,   -- Admin监控
    120,   -- 任务调度中心
    11616, -- 工作流
    11618  -- 我的任务
);

UPDATE sys_menu
SET visible = '0',
    update_time = sysdate()
WHERE menu_id IN (
    1,   -- 系统管理
    3,   -- 系统工具
    100, -- 用户管理
    101, -- 角色管理
    102, -- 菜单管理
    103, -- 部门管理
    105, -- 字典管理
    106, -- 参数设置
    108, -- 日志管理
    115, -- 代码生成
    118  -- 文件管理
);
