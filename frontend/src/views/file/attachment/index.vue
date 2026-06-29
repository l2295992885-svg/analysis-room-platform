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
            <el-form-item label="原始文件名" prop="originalName">
              <el-input v-model="queryParams.originalName" placeholder="请输入原始文件名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="附件类型" prop="attachmentType">
              <el-input v-model="queryParams.attachmentType" placeholder="请输入附件类型" clearable @keyup.enter="handleQuery" />
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
          <el-col :span="1.5">
            <el-button v-hasPermi="['file:attachment:upload']" type="primary" plain icon="Upload" @click="handleUpload">上传附件</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['file:attachment:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">
              删除绑定
            </el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" :data="attachmentList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="业务类型" align="center" prop="businessType" min-width="150" show-overflow-tooltip />
        <el-table-column label="业务ID" align="center" prop="businessId" min-width="120" show-overflow-tooltip />
        <el-table-column label="业务动作" align="center" prop="businessAction" min-width="120" show-overflow-tooltip />
        <el-table-column label="附件类型" align="center" prop="attachmentType" min-width="120" show-overflow-tooltip />
        <el-table-column label="原始文件名" align="center" prop="originalName" min-width="180" show-overflow-tooltip />
        <el-table-column label="后缀" align="center" prop="fileSuffix" width="90" />
        <el-table-column label="大小" align="center" width="110">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="权限范围" align="center" prop="permissionScope" width="120" />
        <el-table-column label="上传人" align="center" prop="uploadUserName" width="120" />
        <el-table-column label="上传部门" align="center" prop="uploadDeptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="上传时间" align="center" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="130">
          <template #default="scope">
            <el-tooltip content="下载" placement="top">
              <el-button v-hasPermi="['file:attachment:download']" link type="primary" icon="Download" @click="handleDownload(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除绑定" placement="top">
              <el-button v-hasPermi="['file:attachment:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" title="上传业务附件" width="560px" append-to-body>
      <el-form ref="uploadFormRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="业务类型" prop="businessType">
              <el-input v-model="form.businessType" placeholder="如 DAILY_LKJ_VIOLATION" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务ID" prop="businessId">
              <el-input v-model="form.businessId" placeholder="业务记录或批次ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务动作" prop="businessAction">
              <el-input v-model="form.businessAction" placeholder="如 FEEDBACK" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="附件类型" prop="attachmentType">
              <el-input v-model="form.attachmentType" placeholder="如 EVIDENCE" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限范围" prop="permissionScope">
              <el-select v-model="form.permissionScope" placeholder="请选择权限范围">
                <el-option label="业务范围" value="BUSINESS" />
                <el-option label="本人" value="OWNER" />
                <el-option label="部门" value="DEPT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件文件" prop="file">
              <el-upload ref="uploadRef" drag :limit="1" :auto-upload="false" :on-change="handleFileChange" :on-remove="handleFileRemove">
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">拖入文件，或点击选择</div>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button :loading="buttonLoading" type="primary" @click="submitUpload">确定上传</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FileAttachment" lang="ts">
import { delFileAttachment, listFileAttachment, uploadFileAttachment } from '@/api/file/attachment';
import { FileAttachmentQuery, FileAttachmentUploadForm, FileAttachmentVO } from '@/api/file/attachment/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const attachmentList = ref<FileAttachmentVO[]>([]);
const loading = ref(false);
const buttonLoading = ref(false);
const showSearch = ref(true);
const ids = ref<Array<string | number>>([]);
const multiple = ref(true);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const uploadFormRef = ref<ElFormInstance>();
const uploadRef = ref<ElUploadInstance>();

const initFormData: FileAttachmentUploadForm = {
  businessType: '',
  businessId: '',
  businessAction: '',
  attachmentType: '',
  permissionScope: 'BUSINESS',
  remark: '',
  file: undefined
};

const data = reactive<PageData<FileAttachmentUploadForm, FileAttachmentQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessType: '',
    businessId: '',
    businessAction: '',
    attachmentType: '',
    permissionScope: '',
    originalName: '',
    fileSuffix: '',
    status: ''
  },
  rules: {
    businessType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
    businessId: [{ required: true, message: '业务ID不能为空', trigger: 'blur' }],
    permissionScope: [{ required: true, message: '权限范围不能为空', trigger: 'change' }],
    file: [{ required: true, message: '附件文件不能为空', trigger: 'change' }]
  }
});

const dialog = reactive<DialogOption>({
  visible: false,
  title: '上传业务附件'
});

const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listFileAttachment(queryParams.value);
  attachmentList.value = res.rows;
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

const handleSelectionChange = (selection: FileAttachmentVO[]) => {
  ids.value = selection.map((item) => item.id);
  multiple.value = !selection.length;
};

const resetForm = () => {
  form.value = { ...initFormData };
  uploadRef.value?.clearFiles();
  uploadFormRef.value?.resetFields();
};

const handleUpload = () => {
  resetForm();
  dialog.visible = true;
};

const handleFileChange = (file: any) => {
  form.value.file = file.raw;
  uploadFormRef.value?.validateField('file');
};

const handleFileRemove = () => {
  form.value.file = undefined;
};

const submitUpload = () => {
  uploadFormRef.value?.validate(async (valid: boolean) => {
    if (!valid || !form.value.file) {
      return;
    }
    buttonLoading.value = true;
    const formData = new FormData();
    formData.append('file', form.value.file);
    formData.append('businessType', form.value.businessType);
    formData.append('businessId', form.value.businessId);
    if (form.value.businessAction) formData.append('businessAction', form.value.businessAction);
    if (form.value.attachmentType) formData.append('attachmentType', form.value.attachmentType);
    if (form.value.permissionScope) formData.append('permissionScope', form.value.permissionScope);
    if (form.value.remark) formData.append('remark', form.value.remark);
    try {
      await uploadFileAttachment(formData);
      proxy?.$modal.msgSuccess('上传成功');
      dialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const handleDownload = (row: FileAttachmentVO) => {
  proxy?.$download.attachment(row.id);
};

const handleDelete = async (row?: FileAttachmentVO) => {
  const attachmentIds = row?.id || ids.value;
  await proxy?.$modal.confirm('是否确认删除业务附件绑定编号为"' + attachmentIds + '"的数据项？底层文件不会被物理删除。');
  loading.value = true;
  await delFileAttachment(attachmentIds).finally(() => (loading.value = false));
  await getList();
  proxy?.$modal.msgSuccess('删除成功');
};

const formatFileSize = (size?: number) => {
  if (!size) return '-';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
};

onMounted(() => {
  getList();
});
</script>
