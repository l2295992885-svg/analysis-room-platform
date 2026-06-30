<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="会话标题" prop="conversationTitle">
              <el-input v-model="queryParams.conversationTitle" placeholder="请输入会话标题" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务类型" prop="businessType">
              <el-input v-model="queryParams.businessType" placeholder="请输入业务类型" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="业务ID" prop="businessId">
              <el-input v-model="queryParams.businessId" placeholder="请输入业务ID" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-row :gutter="12">
      <el-col :span="9">
        <el-card shadow="hover">
          <template #header>
            <el-row :gutter="10" class="mb8">
              <el-col :span="12">
                <span class="panel-title">协同会话</span>
              </el-col>
              <right-toolbar v-model:show-search="showSearch" @query-table="getConversations" />
            </el-row>
          </template>

          <el-table v-loading="conversationLoading" :data="conversationList" border highlight-current-row @current-change="handleConversationChange">
            <el-table-column label="会话标题" align="left" prop="conversationTitle" min-width="180" show-overflow-tooltip />
            <el-table-column label="业务ID" align="center" prop="businessId" width="120" show-overflow-tooltip />
            <el-table-column label="状态" align="center" prop="conversationStatus" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.conversationStatus === 'ACTIVE' ? 'success' : 'info'">{{ scope.row.conversationStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后消息" align="left" prop="lastMessageContent" min-width="180" show-overflow-tooltip />
          </el-table>

          <pagination
            v-show="conversationTotal > 0"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            :total="conversationTotal"
            @pagination="getConversations"
          />
        </el-card>
      </el-col>

      <el-col :span="15">
        <el-card shadow="hover">
          <template #header>
            <div class="chat-header">
              <div>
                <span class="panel-title">{{ currentConversation?.conversationTitle || '请选择会话' }}</span>
                <span v-if="currentConversation?.businessTitle" class="business-hint">{{ currentConversation.businessTitle }}</span>
              </div>
              <el-button
                v-hasPermi="['chat:share']"
                type="primary"
                plain
                icon="Share"
                :disabled="!currentConversation"
                @click="openCardDialog"
              >
                发送业务卡片
              </el-button>
            </div>
          </template>

          <div v-loading="messageLoading" class="message-panel">
            <el-empty v-if="!currentConversation" description="请选择左侧协同会话" />
            <el-empty v-else-if="messageList.length === 0" description="暂无消息" />
            <div v-else class="message-list">
              <div v-for="item in messageList" :key="item.id" class="message-item" :class="{ 'is-card': item.messageType === 'BUSINESS_CARD' }">
                <div class="message-meta">
                  <span>{{ item.senderUserName || '-' }}</span>
                  <span>{{ item.senderDeptName || '-' }}</span>
                  <span>{{ proxy.parseTime(item.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
                </div>
                <div v-if="item.messageType === 'BUSINESS_CARD'" class="business-card">
                  <div>
                    <div class="card-title">{{ item.businessTitle }}</div>
                    <div class="card-subtitle">{{ item.businessType }} / {{ item.businessId }}</div>
                    <p>{{ item.messageContent }}</p>
                  </div>
                  <el-button v-hasPermi="['chat:view']" type="primary" link icon="Position" @click="handleOpenCard(item)">打开</el-button>
                </div>
                <div v-else class="text-message">{{ item.messageContent }}</div>
              </div>
            </div>
          </div>

          <div class="send-box">
            <el-input
              v-model="sendForm.messageContent"
              type="textarea"
              :rows="3"
              maxlength="2000"
              show-word-limit
              placeholder="输入协同提醒内容。聊天只作提醒，不代表业务授权。"
              :disabled="!currentConversation"
            />
            <el-button v-hasPermi="['chat:view']" type="primary" icon="Promotion" :disabled="!currentConversation" @click="handleSendText">
              发送
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="cardDialog.visible" title="发送业务卡片" width="680px" append-to-body>
      <el-alert title="业务卡片只是提醒和跳转入口，不代表接收人拥有查看、审核、下发或反馈权限。" type="warning" :closable="false" class="mb-[12px]" />
      <el-form ref="cardFormRef" :model="cardForm" :rules="cardRules" label-width="110px">
        <el-form-item label="业务类型" prop="businessType">
          <el-input v-model="cardForm.businessType" placeholder="例如 DAILY_LKJ_VIOLATION" />
        </el-form-item>
        <el-form-item label="业务ID" prop="businessId">
          <el-input v-model="cardForm.businessId" placeholder="请输入业务ID" />
        </el-form-item>
        <el-form-item label="业务标题" prop="businessTitle">
          <el-input v-model="cardForm.businessTitle" placeholder="请输入业务标题" />
        </el-form-item>
        <el-form-item label="跳转地址" prop="businessUrl">
          <el-input v-model="cardForm.businessUrl" placeholder="例如 /index" />
        </el-form-item>
        <el-form-item label="说明" prop="messageContent">
          <el-input v-model="cardForm.messageContent" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="快照JSON" prop="businessPayload">
          <el-input v-model="cardForm.businessPayload" type="textarea" :rows="4" maxlength="4000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cardDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSendCard">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ChatCenter" lang="ts">
import { listChatConversations, listChatMessages, openBusinessCard, sendBusinessCard, sendChatMessage } from '@/api/chat';
import { ChatBusinessCardForm, ChatConversationQuery, ChatConversationVO, ChatMessageVO } from '@/api/chat/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const router = useRouter();

const showSearch = ref(true);
const conversationLoading = ref(false);
const messageLoading = ref(false);
const conversationList = ref<ChatConversationVO[]>([]);
const messageList = ref<ChatMessageVO[]>([]);
const conversationTotal = ref(0);
const currentConversation = ref<ChatConversationVO>();
const queryFormRef = ref<ElFormInstance>();
const cardFormRef = ref<ElFormInstance>();

const sendForm = reactive({
  messageContent: ''
});

const cardDialog = reactive<DialogOption>({
  visible: false,
  title: '发送业务卡片'
});

const cardForm = reactive<ChatBusinessCardForm>({
  conversationId: undefined,
  businessType: '',
  businessId: '',
  businessTitle: '',
  businessUrl: '/index',
  businessPayload: '',
  messageContent: ''
});

const cardRules = {
  businessType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
  businessId: [{ required: true, message: '业务ID不能为空', trigger: 'blur' }],
  businessTitle: [{ required: true, message: '业务标题不能为空', trigger: 'blur' }]
};

const data = reactive<PageData<{}, ChatConversationQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    conversationTitle: '',
    businessType: '',
    businessId: '',
    conversationStatus: ''
  },
  rules: {}
});

const { queryParams } = toRefs(data);

const getConversations = async () => {
  conversationLoading.value = true;
  const res = await listChatConversations(queryParams.value);
  conversationList.value = res.rows;
  conversationTotal.value = res.total;
  conversationLoading.value = false;
  if (!currentConversation.value && conversationList.value.length > 0) {
    currentConversation.value = conversationList.value[0];
    await getMessages();
  }
};

const getMessages = async () => {
  if (!currentConversation.value) {
    messageList.value = [];
    return;
  }
  messageLoading.value = true;
  const res = await listChatMessages(currentConversation.value.id, { pageNum: 1, pageSize: 50 });
  messageList.value = res.rows;
  messageLoading.value = false;
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  currentConversation.value = undefined;
  messageList.value = [];
  getConversations();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleConversationChange = async (row?: ChatConversationVO) => {
  if (!row) {
    return;
  }
  currentConversation.value = row;
  await getMessages();
};

const handleSendText = async () => {
  if (!currentConversation.value) {
    return;
  }
  if (!sendForm.messageContent.trim()) {
    proxy?.$modal.msgWarning('请输入消息内容');
    return;
  }
  await sendChatMessage(currentConversation.value.id, { messageContent: sendForm.messageContent.trim() });
  sendForm.messageContent = '';
  await getMessages();
  await getConversations();
};

const openCardDialog = () => {
  if (!currentConversation.value) {
    return;
  }
  cardForm.conversationId = currentConversation.value.id;
  cardForm.businessType = currentConversation.value.businessType || 'DAILY_LKJ_VIOLATION';
  cardForm.businessId = currentConversation.value.businessId || '';
  cardForm.businessTitle = currentConversation.value.businessTitle || currentConversation.value.conversationTitle;
  cardForm.businessUrl = currentConversation.value.businessUrl || '/index';
  cardForm.messageContent = `业务卡片：${cardForm.businessTitle}`;
  cardForm.businessPayload = JSON.stringify(
    {
      businessType: cardForm.businessType,
      businessId: cardForm.businessId,
      note: '业务卡片只作提醒，不代表授权'
    },
    null,
    2
  );
  cardDialog.visible = true;
};

const handleSendCard = async () => {
  await cardFormRef.value?.validate();
  await sendBusinessCard(cardForm);
  proxy?.$modal.msgSuccess('业务卡片已发送');
  cardDialog.visible = false;
  await getMessages();
  await getConversations();
};

const handleOpenCard = async (row: ChatMessageVO) => {
  const { data } = await openBusinessCard(row.id);
  if (data.businessUrl) {
    router.push(data.businessUrl);
  } else {
    proxy?.$modal.msgWarning('该业务卡片没有配置跳转地址');
  }
};

onMounted(() => {
  getConversations();
});
</script>

<style lang="scss" scoped>
.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.chat-header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.business-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.message-panel {
  min-height: 430px;
  max-height: 520px;
  overflow: auto;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
}

.message-item {
  max-width: 86%;
  padding: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;

  &.is-card {
    max-width: 96%;
  }
}

.message-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.text-message {
  line-height: 1.7;
  color: #1f2937;
  white-space: pre-wrap;
}

.business-card {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;

  p {
    margin: 8px 0 0;
    line-height: 1.6;
    color: #334155;
  }
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1d4ed8;
}

.card-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.send-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  margin-top: 12px;
}
</style>
