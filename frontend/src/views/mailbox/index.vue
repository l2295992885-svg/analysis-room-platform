<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="消息标题" prop="messageTitle">
              <el-input v-model="queryParams.messageTitle" placeholder="请输入消息标题" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务类型" prop="businessType">
              <el-input v-model="queryParams.businessType" placeholder="请输入业务类型" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务ID" prop="businessId">
              <el-input v-model="queryParams.businessId" placeholder="请输入业务ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="已读" prop="readFlag">
              <el-select v-model="queryParams.readFlag" placeholder="全部" clearable>
                <el-option label="未读" value="0" />
                <el-option label="已读" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="归档" prop="archiveFlag">
              <el-select v-model="queryParams.archiveFlag" placeholder="全部" clearable>
                <el-option label="未归档" value="0" />
                <el-option label="已归档" value="1" />
              </el-select>
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
            <span class="mailbox-title">正式业务流转消息</span>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" :data="messageList" border>
        <el-table-column label="消息标题" align="left" prop="messageTitle" min-width="240" show-overflow-tooltip />
        <el-table-column label="消息类型" align="center" prop="messageType" width="110" />
        <el-table-column label="业务类型" align="center" prop="businessType" min-width="150" show-overflow-tooltip />
        <el-table-column label="业务ID" align="center" prop="businessId" width="130" show-overflow-tooltip />
        <el-table-column label="发送人" align="center" prop="senderUserName" width="110" />
        <el-table-column label="发送部门" align="center" prop="senderDeptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="已读" align="center" prop="readFlag" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.readFlag === '1' ? 'success' : 'warning'">{{ scope.row.readFlag === '1' ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归档" align="center" prop="archiveFlag" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.archiveFlag === '1' ? 'info' : ''">{{ scope.row.archiveFlag === '1' ? '已归档' : '未归档' }}</el-tag>
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
              <el-button v-hasPermi="['mailbox:view']" link type="primary" icon="View" @click="handleDetail(scope.row)" />
            </el-tooltip>
            <el-tooltip v-if="scope.row.readFlag !== '1'" content="标记已读" placement="top">
              <el-button v-hasPermi="['mailbox:view']" link type="primary" icon="Check" @click="handleRead(scope.row)" />
            </el-tooltip>
            <el-tooltip v-if="scope.row.archiveFlag !== '1'" content="归档" placement="top">
              <el-button v-hasPermi="['mailbox:view']" link type="primary" icon="FolderChecked" @click="handleArchive(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" title="信箱消息详情" width="680px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="消息标题" :span="2">{{ detail.messageTitle }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{ detail.messageType }}</el-descriptions-item>
        <el-descriptions-item label="来源动作">{{ detail.sourceAction || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.businessType }}</el-descriptions-item>
        <el-descriptions-item label="业务ID">{{ detail.businessId }}</el-descriptions-item>
        <el-descriptions-item label="发送人">{{ detail.senderUserName }}</el-descriptions-item>
        <el-descriptions-item label="接收人">{{ detail.receiverUserName || detail.receiverRoleKey || detail.receiverDeptName }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">{{ detail.messageContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务卡片" :span="2">
          <pre class="payload">{{ detail.businessPayload || '-' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">关闭</el-button>
          <el-button v-if="detail.readFlag !== '1'" @click="handleRead(detail as MailboxMessageVO)">标记已读</el-button>
          <el-button v-if="detail.businessUrl" type="primary" @click="goBusiness(detail.businessUrl)">打开业务</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MailboxCenter" lang="ts">
import { archiveMailboxMessage, getMailboxMessage, listMailboxMessages, markMailboxMessageRead } from '@/api/mailbox';
import { MailboxMessageQuery, MailboxMessageVO } from '@/api/mailbox/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const loading = ref(false);
const showSearch = ref(true);
const messageList = ref<MailboxMessageVO[]>([]);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const detail = ref<Partial<MailboxMessageVO>>({});

const dialog = reactive<DialogOption>({
  visible: false,
  title: '信箱消息详情'
});

const data = reactive<PageData<{}, MailboxMessageQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessType: '',
    businessId: '',
    batchId: '',
    messageType: '',
    messageTitle: '',
    sourceAction: '',
    readFlag: '',
    archiveFlag: ''
  },
  rules: {}
});

const { queryParams } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listMailboxMessages(queryParams.value);
  messageList.value = res.rows;
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

const handleDetail = async (row: MailboxMessageVO) => {
  const { data } = await getMailboxMessage(row.id);
  detail.value = data;
  dialog.visible = true;
};

const handleRead = async (row: MailboxMessageVO) => {
  await markMailboxMessageRead(row.id);
  proxy?.$modal.msgSuccess('已标记为已读');
  if (detail.value.id === row.id) {
    detail.value.readFlag = '1';
  }
  await getList();
};

const handleArchive = async (row: MailboxMessageVO) => {
  await proxy?.$modal.confirm('是否确认归档该信箱消息？');
  await archiveMailboxMessage(row.id);
  proxy?.$modal.msgSuccess('归档成功');
  await getList();
};

const goBusiness = (url: string) => {
  dialog.visible = false;
  router.push(url);
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.mailbox-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.payload {
  max-height: 120px;
  padding: 8px;
  margin: 0;
  overflow: auto;
  white-space: pre-wrap;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
</style>
