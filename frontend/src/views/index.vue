<template>
  <div class="platform-home">
    <section class="home-header">
      <div>
        <p class="eyebrow">综合分析室数据分析平台</p>
        <h2>极简功能首页</h2>
        <p class="summary">
          围绕信箱、待办、文件、协同、违章管理和统计分析组织日常工作入口，第一阶段优先保留若依成熟的权限、日志、文件和代码生成底座。
        </p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="goModule('/system/user')">系统管理</el-button>
        <el-button @click="goModule('/tool/gen')">代码生成</el-button>
      </div>
    </section>

    <section class="quick-grid" aria-label="平台模块入口">
      <button
        v-for="item in modules"
        :key="item.name"
        class="module-card"
        :class="{ 'is-ready': item.status === '已接入', 'is-disabled': !item.path }"
        type="button"
        @click="goModule(item.path)"
      >
        <span class="module-icon" :class="'tone-' + item.tone">{{ item.shortName }}</span>
        <span class="module-content">
          <span class="module-title">{{ item.name }}</span>
          <span class="module-desc">{{ item.description }}</span>
        </span>
        <span class="module-status">{{ item.status }}</span>
      </button>
    </section>

    <section class="workbench-band">
      <div class="band-column">
        <h3>第一阶段重点</h3>
        <ul>
          <li>保留系统管理、用户、角色、部门、菜单、字典、参数、日志、文件和代码生成能力。</li>
          <li>违章管理先以“每日 LKJ 音视频违标公示”为样板业务推进。</li>
          <li>前端按钮隐藏只做体验优化，后端接口仍必须做权限和数据范围校验。</li>
        </ul>
      </div>
      <div class="band-column">
        <h3>暂缓启用</h3>
        <ul>
          <li>多租户、多数据源、复杂工作流、分布式任务、链路追踪和大规模监控。</li>
          <li>若依演示菜单和测试页面仅保留源码，不作为正式业务入口。</li>
          <li>模板演示数据不得被当作综合分析室正式业务数据。</li>
        </ul>
      </div>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
const router = useRouter();

type ModuleEntry = {
  name: string;
  shortName: string;
  description: string;
  status: '已接入' | '待建设';
  tone: number;
  path?: string;
};

const modules: ModuleEntry[] = [
  { name: '信箱中心', shortName: '信', description: '正式业务流转消息入口', status: '已接入', tone: 1, path: '/mailbox/messages' },
  { name: '待办中心', shortName: '办', description: '当前必须处理的流程任务', status: '已接入', tone: 2, path: '/todo/my' },
  { name: '文件中心', shortName: '文', description: '平台文件与业务附件管理', status: '已接入', tone: 3, path: '/file/attachment' },
  { name: '协同聊天', shortName: '聊', description: '轻量提醒和业务卡片协同', status: '已接入', tone: 4, path: '/chat/conversations' },
  { name: '违章管理', shortName: '违', description: '每日 LKJ 样板业务入口', status: '已接入', tone: 5, path: '/violation/daily' },
  { name: 'LKJ 分析', shortName: 'L', description: 'LKJ 违标分析专题', status: '待建设', tone: 6 },
  { name: '音视频分析', shortName: '音', description: '音视频违标分析专题', status: '待建设', tone: 7 },
  { name: '单兵分析', shortName: '单', description: '单兵作业分析专题', status: '待建设', tone: 8 },
  { name: '解锁统计', shortName: '解', description: '解锁记录统计与追踪', status: '待建设', tone: 9 },
  { name: '调度命令', shortName: '令', description: '调度命令分析与留痕', status: '待建设', tone: 10 },
  { name: '施工作业', shortName: '施', description: '施工作业分析与归档', status: '待建设', tone: 11 },
  { name: '试运机车', shortName: '试', description: '试运机车记录与分析', status: '待建设', tone: 12 },
  { name: '月度报表', shortName: '报', description: '月度业务汇总与导出', status: '待建设', tone: 13 },
  { name: '统计分析', shortName: '统', description: '业务统计和趋势分析', status: '待建设', tone: 14 },
  { name: '基础数据', shortName: '基', description: '人员、组织、违标编码维护', status: '已接入', tone: 15, path: '/base/personnel' },
  { name: '系统管理', shortName: '系', description: '用户、角色、菜单、字典配置', status: '已接入', tone: 16, path: '/system/user' }
];

const goModule = (path?: string) => {
  if (!path) {
    return;
  }
  router.push(path);
};
</script>

<style lang="scss" scoped>
.platform-home {
  min-height: calc(100vh - 84px);
  padding: 24px;
  background: #f6f7fb;
  color: #1f2937;
}

.home-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;

  h2 {
    margin: 6px 0 10px;
    font-size: 24px;
    font-weight: 700;
    letter-spacing: 0;
  }
}

.eyebrow {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #2563eb;
}

.summary {
  max-width: 760px;
  margin: 0;
  line-height: 1.7;
  color: #4b5563;
}

.header-actions {
  display: flex;
  flex-shrink: 0;
  gap: 10px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.module-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  min-height: 92px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    border-color: #93c5fd;
    box-shadow: 0 10px 24px rgb(15 23 42 / 8%);
    transform: translateY(-1px);
  }

  &.is-disabled {
    cursor: default;
  }

  &.is-disabled:hover {
    border-color: #e5e7eb;
    box-shadow: none;
    transform: none;
  }
}

.module-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  border-radius: 8px;
}

.module-content {
  min-width: 0;
}

.module-title,
.module-desc {
  display: block;
}

.module-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.module-desc {
  margin-top: 6px;
  overflow: hidden;
  font-size: 13px;
  line-height: 1.45;
  color: #6b7280;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.module-status {
  align-self: start;
  padding: 3px 8px;
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  border-radius: 999px;
}

.is-ready .module-status {
  color: #047857;
  background: #d1fae5;
}

.workbench-band {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-top: 18px;
}

.band-column {
  padding: 20px 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;

  h3 {
    margin: 0 0 12px;
    font-size: 17px;
    font-weight: 700;
    letter-spacing: 0;
  }

  ul {
    padding-left: 18px;
    margin: 0;
    color: #4b5563;
  }

  li + li {
    margin-top: 8px;
  }
}

.tone-1 {
  background: #2563eb;
}

.tone-2 {
  background: #059669;
}

.tone-3 {
  background: #7c3aed;
}

.tone-4 {
  background: #db2777;
}

.tone-5 {
  background: #dc2626;
}

.tone-6 {
  background: #0891b2;
}

.tone-7 {
  background: #ea580c;
}

.tone-8 {
  background: #4f46e5;
}

.tone-9 {
  background: #0f766e;
}

.tone-10 {
  background: #9333ea;
}

.tone-11 {
  background: #b45309;
}

.tone-12 {
  background: #be123c;
}

.tone-13 {
  background: #0369a1;
}

.tone-14 {
  background: #15803d;
}

.tone-15 {
  background: #52525b;
}

.tone-16 {
  background: #1d4ed8;
}

@media (max-width: 900px) {
  .home-header,
  .workbench-band {
    grid-template-columns: 1fr;
  }

  .home-header {
    display: grid;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 520px) {
  .platform-home {
    padding: 14px;
  }

  .module-card {
    grid-template-columns: 40px minmax(0, 1fr);
  }

  .module-status {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
