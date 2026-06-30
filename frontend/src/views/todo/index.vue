<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="业务类型" prop="businessType">
              <el-input v-model="queryParams.businessType" placeholder="请输入业务类型" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务ID" prop="businessId">
              <el-input v-model="queryParams.businessId" placeholder="请输入业务ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="待办标题" prop="taskTitle">
              <el-input v-model="queryParams.taskTitle" placeholder="请输入待办标题" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="当前节点" prop="currentNode">
              <el-input v-model="queryParams.currentNode" placeholder="请输入当前节点" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="12">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
              <el-tab-pane label="我的待办" name="pending" />
              <el-tab-pane label="已处理" name="done" />
            </el-tabs>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" :data="todoList" border>
        <el-table-column label="待办标题" align="left" prop="taskTitle" min-width="220" show-overflow-tooltip />
        <el-table-column label="业务类型" align="center" prop="businessType" min-width="150" show-overflow-tooltip />
        <el-table-column label="业务ID" align="center" prop="businessId" width="130" show-overflow-tooltip />
        <el-table-column label="当前节点" align="center" prop="currentNode" width="150" show-overflow-tooltip />
        <el-table-column label="优先级" align="center" prop="priority" width="100">
          <template #default="scope">
            <el-tag :type="priorityTag(scope.row.priority)">{{ scope.row.priority || 'NORMAL' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="taskStatus" width="110">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.taskStatus)">{{ scope.row.taskStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送人" align="center" prop="senderUserName" width="110" />
        <el-table-column label="接收部门" align="center" prop="receiverDeptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="截止时间" align="center" prop="dueTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.dueTime, '{y}-{m}-{d} {h}:{i}') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="150">
          <template #default="scope">
            <el-tooltip content="详情" placement="top">
              <el-button v-hasPermi="['todo:view']" link type="primary" icon="View" @click="handleDetail(scope.row)" />
            </el-tooltip>
            <el-tooltip content="打开业务" placement="top">
              <el-button v-hasPermi="['todo:view']" link type="primary" icon="Position" @click="handleOpen(scope.row)" />
            </el-tooltip>
            <el-tooltip v-if="scope.row.taskStatus === 'PENDING'" content="关闭" placement="top">
              <el-button v-hasPermi="['todo:close']" link type="primary" icon="CircleClose" @click="handleClose(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" title="待办详情" width="680px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="待办标题" :span="2">{{ detail.taskTitle }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.businessType }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detail.businessId }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detail.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.taskStatus }}</el-descriptions-item>
        <el-descriptions-item label="发送人">{{ detail.senderUserName }}</el-descriptions-item>
        <el-descriptions-item label="接收人">{{ detail.receiverUserName || detail.receiverRoleKey || detail.receiverDeptName }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ detail.taskContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成说明" :span="2">{{ detail.finishComment || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">关闭</el-button>
          <el-button v-if="detail.businessUrl" type="primary" @click="goBusiness(detail.businessUrl)">打开业务</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TodoCenter" lang="ts">
import { closeTodo, getTodo, listDoneTodos, listMyTodos, openTodo } from '@/api/todo';
import { TodoTaskQuery, TodoTaskVO } from '@/api/todo/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const loading = ref(false);
const showSearch = ref(true);
const activeTab = ref('pending');
const todoList = ref<TodoTaskVO[]>([]);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const detail = ref<Partial<TodoTaskVO>>({});

const dialog = reactive<DialogOption>({
  visible: false,
  title: '待办详情'
});

const data = reactive<PageData<{}, TodoTaskQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessType: '',
    businessId: '',
    batchId: '',
    taskTitle: '',
    currentNode: '',
    priority: ''
  },
  rules: {}
});

const { queryParams } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = activeTab.value === 'done' ? await listDoneTodos(queryParams.value) : await listMyTodos(queryParams.value);
  todoList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleTabChange = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const handleDetail = async (row: TodoTaskVO) => {
  const { data } = await getTodo(row.id);
  detail.value = data;
  dialog.visible = true;
};

const handleOpen = async (row: TodoTaskVO) => {
  const { data } = await openTodo(row.id);
  if (data.businessUrl) {
    goBusiness(data.businessUrl);
  } else {
    proxy?.$modal.msgWarning('该待办暂未配置业务跳转地址');
  }
};

const handleClose = async (row: TodoTaskVO) => {
  const result: any = await proxy?.$modal.prompt('请输入关闭说明');
  await closeTodo(row.id, { finishComment: result?.value || '' });
  proxy?.$modal.msgSuccess('关闭成功');
  await getList();
};

const goBusiness = (url: string) => {
  dialog.visible = false;
  router.push(url);
};

const statusTag = (status?: string) => {
  if (status === 'PENDING') return 'warning';
  if (status === 'DONE') return 'success';
  if (status === 'CLOSED') return 'info';
  return 'info';
};

const priorityTag = (priority?: string) => {
  if (priority === 'URGENT') return 'danger';
  if (priority === 'HIGH') return 'warning';
  if (priority === 'LOW') return 'info';
  return '';
};

onMounted(() => {
  getList();
});
</script>
