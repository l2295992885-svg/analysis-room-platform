<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="违标编码" prop="violationCode">
              <el-input v-model="queryParams.violationCode" placeholder="请输入违标编码" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="违标名称" prop="violationName">
              <el-input v-model="queryParams.violationName" placeholder="请输入违标名称" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="性质" prop="nature">
              <el-input v-model="queryParams.nature" placeholder="请输入性质" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="类别" prop="category">
              <el-input v-model="queryParams.category" placeholder="请输入类别" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="类型" prop="violationType">
              <el-input v-model="queryParams.violationType" placeholder="请输入类型" clearable @keyup.enter="handleQuery" />
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
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:edit']" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()">修改</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:remove']" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:import']" type="info" plain icon="Upload" @click="handleImport">导入</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:import']" type="info" plain icon="Download" @click="importTemplate">下载模板</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['base:violationCode:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button></el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="codeList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="违标编码" align="center" prop="violationCode" min-width="130" />
        <el-table-column label="违标名称" align="center" prop="violationName" min-width="180" show-overflow-tooltip />
        <el-table-column label="性质" align="center" prop="nature" min-width="100" />
        <el-table-column label="类别" align="center" prop="category" min-width="100" />
        <el-table-column label="类型" align="center" prop="violationType" min-width="120" />
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="120" fixed="right">
          <template #default="scope">
            <el-button v-hasPermi="['base:violationCode:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" />
            <el-button v-hasPermi="['base:violationCode:remove']" link type="primary" icon="Delete" @click="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="违标编码" prop="violationCode"><el-input v-model="form.violationCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="违标名称" prop="violationName"><el-input v-model="form.violationName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性质" prop="nature"><el-input v-model="form.nature" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类别" prop="category"><el-input v-model="form.category" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型" prop="violationType"><el-input v-model="form.violationType" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="说明" prop="description"><el-input v-model="form.description" type="textarea" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="upload.open" title="导入违标编码" width="420px" append-to-body>
      <el-upload ref="uploadRef" drag :limit="1" accept=".xlsx,.xls" :auto-upload="false" :on-change="handleFileChange">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">拖入 Excel 文件，或点击选择</div>
      </el-upload>
      <el-checkbox v-model="upload.updateSupport" class="mt-3">存在相同违标编码时更新</el-checkbox>
      <template #footer>
        <el-button type="primary" @click="submitImport">导入</el-button>
        <el-button @click="upload.open = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BaseViolationCode" lang="ts">
import type { UploadFile } from 'element-plus';
import { addViolationCode, delViolationCode, getViolationCode, importViolationCode, listViolationCode, updateViolationCode } from '@/api/base/violationCode';
import { ViolationCodeForm, ViolationCodeQuery, ViolationCodeVO } from '@/api/base/violationCode/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_normal_disable } = toRefs<any>(proxy?.useDict('sys_normal_disable'));
const codeList = ref<ViolationCodeVO[]>([]);
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
const initFormData: ViolationCodeForm = { id: undefined, violationCode: '', violationName: '', nature: '', category: '', violationType: '', description: '', status: '0', remark: '' };
const data = reactive<PageData<ViolationCodeForm, ViolationCodeQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, violationCode: '', violationName: '', nature: '', category: '', violationType: '', status: '' },
  rules: {
    violationCode: [{ required: true, message: '违标编码不能为空', trigger: 'blur' }],
    violationName: [{ required: true, message: '违标名称不能为空', trigger: 'blur' }],
    nature: [{ required: true, message: '性质不能为空', trigger: 'blur' }],
    category: [{ required: true, message: '类别不能为空', trigger: 'blur' }],
    violationType: [{ required: true, message: '类型不能为空', trigger: 'blur' }]
  }
});
const { queryParams, form, rules } = toRefs(data);

const getList = async () => { loading.value = true; const res = await listViolationCode(queryParams.value); codeList.value = res.rows; total.value = res.total; loading.value = false; };
const reset = () => { form.value = { ...initFormData }; formRef.value?.resetFields(); };
const handleQuery = () => { queryParams.value.pageNum = 1; getList(); };
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery(); };
const handleSelectionChange = (selection: ViolationCodeVO[]) => { ids.value = selection.map((item) => item.id); single.value = selection.length !== 1; multiple.value = !selection.length; };
const handleAdd = () => { reset(); dialog.visible = true; dialog.title = '新增违标编码'; };
const handleUpdate = async (row?: ViolationCodeVO) => { reset(); const res = await getViolationCode(row?.id || ids.value[0]); Object.assign(form.value, res.data); dialog.visible = true; dialog.title = '修改违标编码'; };
const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;
    form.value.id ? await updateViolationCode(form.value) : await addViolationCode(form.value);
    proxy?.$modal.msgSuccess('保存成功');
    dialog.visible = false;
    getList();
  });
};
const handleDelete = async (row?: ViolationCodeVO) => { const targetIds = row?.id || ids.value; await proxy?.$modal.confirm(`确认删除违标编码编号为 "${targetIds}" 的数据项？`); await delViolationCode(targetIds); proxy?.$modal.msgSuccess('删除成功'); getList(); };
const handleExport = () => { proxy?.download('base/violationCode/export', queryParams.value, `violation_code_${new Date().getTime()}.xlsx`); };
const importTemplate = () => { proxy?.download('base/violationCode/importTemplate', {}, `violation_code_template_${new Date().getTime()}.xlsx`); };
const handleImport = () => { upload.open = true; upload.updateSupport = false; importFile.value = undefined; uploadRef.value?.clearFiles(); };
const handleFileChange = (file: UploadFile) => { importFile.value = file.raw; };
const submitImport = async () => {
  if (!importFile.value) { proxy?.$modal.msgError('请选择要导入的 Excel 文件'); return; }
  const formData = new FormData();
  formData.append('file', importFile.value);
  await importViolationCode(formData, upload.updateSupport);
  proxy?.$modal.msgSuccess('导入成功');
  upload.open = false;
  getList();
};
const cancel = () => { dialog.visible = false; reset(); };
onMounted(() => getList());
</script>
