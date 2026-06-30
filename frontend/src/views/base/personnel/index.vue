<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="工号" prop="jobNo">
              <el-input v-model="queryParams.jobNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="姓名" prop="personName">
              <el-input v-model="queryParams.personName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="责任部门" prop="deptName">
              <el-input v-model="queryParams.deptName" placeholder="请输入责任部门" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="车间" prop="workshop">
              <el-input v-model="queryParams.workshop" placeholder="请输入车间" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
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
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:import']" type="info" plain icon="Upload" @click="handleImport">导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:import']" type="info" plain icon="Download" @click="importTemplate">下载模板</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['base:personnel:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="personnelList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="工号" align="center" prop="jobNo" min-width="110" />
        <el-table-column label="姓名" align="center" prop="personName" min-width="100" />
        <el-table-column label="责任部门" align="center" prop="deptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="车间" align="center" prop="workshop" min-width="120" show-overflow-tooltip />
        <el-table-column label="车队" align="center" prop="teamName" min-width="120" show-overflow-tooltip />
        <el-table-column label="指导组" align="center" prop="guideGroup" min-width="120" show-overflow-tooltip />
        <el-table-column label="岗位" align="center" prop="positionName" min-width="100" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['base:personnel:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['base:personnel:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="720px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工号" prop="jobNo">
              <el-input v-model="form.jobNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="personName">
              <el-input v-model="form.personName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任部门" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入责任部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统部门ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车间" prop="workshop">
              <el-input v-model="form.workshop" placeholder="请输入车间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车队" prop="teamName">
              <el-input v-model="form.teamName" placeholder="请输入车队" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="指导组" prop="guideGroup">
              <el-input v-model="form.guideGroup" placeholder="请输入指导组" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="positionName">
              <el-input v-model="form.positionName" placeholder="请输入岗位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="upload.open" title="导入人员基础数据" width="420px" append-to-body>
      <el-upload ref="uploadRef" drag :limit="1" accept=".xlsx,.xls" :auto-upload="false" :on-change="handleFileChange">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖入 Excel 文件，或点击选择</div>
      </el-upload>
      <el-checkbox v-model="upload.updateSupport" class="mt-3">存在相同工号时更新</el-checkbox>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitImport">导入</el-button>
          <el-button @click="upload.open = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BasePersonnel" lang="ts">
import type { UploadFile } from 'element-plus';
import { addPersonnel, delPersonnel, getPersonnel, importPersonnel, listPersonnel, updatePersonnel } from '@/api/base/personnel';
import { PersonnelForm, PersonnelQuery, PersonnelVO } from '@/api/base/personnel/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));

const personnelList = ref<PersonnelVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref<Array<number | string>>([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const formRef = ref<ElFormInstance>();
const queryFormRef = ref<ElFormInstance>();
const uploadRef = ref<ElUploadInstance>();
const importFile = ref<File>();

const dialog = reactive<DialogOption>({ visible: false, title: '' });
const upload = reactive({ open: false, updateSupport: false });

const initFormData: PersonnelForm = {
  id: undefined,
  jobNo: '',
  personName: '',
  deptId: undefined,
  deptName: '',
  workshop: '',
  teamName: '',
  guideGroup: '',
  positionName: '',
  phone: '',
  status: '0',
  remark: ''
};

const data = reactive<PageData<PersonnelForm, PersonnelQuery>>({
  form: { ...initFormData },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    jobNo: '',
    personName: '',
    deptName: '',
    workshop: '',
    teamName: '',
    guideGroup: '',
    status: ''
  },
  rules: {
    jobNo: [{ required: true, message: '工号不能为空', trigger: 'blur' }],
    personName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }]
  }
});

const { queryParams, form, rules } = toRefs(data);

const getList = async () => {
  loading.value = true;
  const res = await listPersonnel(queryParams.value);
  personnelList.value = res.rows;
  total.value = res.total;
  loading.value = false;
};

const reset = () => {
  form.value = { ...initFormData };
  formRef.value?.resetFields();
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleSelectionChange = (selection: PersonnelVO[]) => {
  ids.value = selection.map((item) => item.id);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
};

const handleAdd = () => {
  reset();
  dialog.visible = true;
  dialog.title = '新增人员基础数据';
};

const handleUpdate = async (row?: PersonnelVO) => {
  reset();
  const id = row?.id || ids.value[0];
  const res = await getPersonnel(id);
  Object.assign(form.value, res.data);
  dialog.visible = true;
  dialog.title = '修改人员基础数据';
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    form.value.id ? await updatePersonnel(form.value) : await addPersonnel(form.value);
    proxy?.$modal.msgSuccess('保存成功');
    dialog.visible = false;
    getList();
  });
};

const handleDelete = async (row?: PersonnelVO) => {
  const targetIds = row?.id || ids.value;
  await proxy?.$modal.confirm(`确认删除人员基础数据编号为 "${targetIds}" 的数据项？`);
  await delPersonnel(targetIds);
  proxy?.$modal.msgSuccess('删除成功');
  getList();
};

const handleExport = () => {
  proxy?.download('base/personnel/export', queryParams.value, `personnel_${new Date().getTime()}.xlsx`);
};

const importTemplate = () => {
  proxy?.download('base/personnel/importTemplate', {}, `personnel_template_${new Date().getTime()}.xlsx`);
};

const handleImport = () => {
  upload.open = true;
  upload.updateSupport = false;
  importFile.value = undefined;
  uploadRef.value?.clearFiles();
};

const handleFileChange = (file: UploadFile) => {
  importFile.value = file.raw;
};

const submitImport = async () => {
  if (!importFile.value) {
    proxy?.$modal.msgError('请选择要导入的 Excel 文件');
    return;
  }
  const formData = new FormData();
  formData.append('file', importFile.value);
  await importPersonnel(formData, upload.updateSupport);
  proxy?.$modal.msgSuccess('导入成功');
  upload.open = false;
  getList();
};

const cancel = () => {
  dialog.visible = false;
  reset();
};

onMounted(() => {
  getList();
});
</script>
