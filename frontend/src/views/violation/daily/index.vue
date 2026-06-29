<template>
  <div class="p-2 daily-violation-page">
    <el-card shadow="hover" class="mb-[10px]">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="违标记录" name="records" />
        <el-tab-pane label="结果库" name="results" />
      </el-tabs>
    </el-card>

    <el-card v-if="activeTab === 'records'" shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="提报日期" prop="reportDate">
          <el-date-picker v-model="queryParams.reportDate" value-format="YYYY-MM-DD" type="date" clearable />
        </el-form-item>
        <el-form-item label="违标编码" prop="violationCode">
          <el-input v-model="queryParams.violationCode" placeholder="违标编码" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="queryParams.employeeNo" placeholder="工号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="currentStatus">
          <el-select v-model="queryParams.currentStatus" placeholder="全部状态" clearable style="width: 220px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-else shadow="hover" class="mb-[10px]">
      <el-form ref="resultQueryFormRef" :model="resultQuery" :inline="true">
        <el-form-item label="提报日期" prop="reportDate">
          <el-date-picker v-model="resultQuery.reportDate" value-format="YYYY-MM-DD" type="date" clearable />
        </el-form-item>
        <el-form-item label="违标编码" prop="violationCode">
          <el-input v-model="resultQuery.violationCode" placeholder="违标编码" clearable @keyup.enter="handleResultQuery" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="resultQuery.employeeNo" placeholder="工号" clearable @keyup.enter="handleResultQuery" />
        </el-form-item>
        <el-form-item label="计入结果" prop="included">
          <el-select v-model="resultQuery.included" placeholder="全部" clearable style="width: 120px">
            <el-option label="计入" value="1" />
            <el-option label="不计入" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleResultQuery">查询</el-button>
          <el-button icon="Refresh" @click="resetResultQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="activeTab === 'records'" shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:add']" type="primary" plain icon="Plus" @click="handleAdd">新增草稿</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:import']" type="info" plain icon="Upload" @click="handleImport">Excel导入</el-button></el-col>
          <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button></el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="recordList">
        <el-table-column label="序号" type="index" width="56" align="center" />
        <el-table-column label="提报日期" prop="reportDate" width="112" />
        <el-table-column label="违章日期" prop="violationDate" width="112" />
        <el-table-column label="违标编码" prop="violationCode" width="112" />
        <el-table-column label="性质" prop="violationNatureSnapshot" width="120" show-overflow-tooltip />
        <el-table-column label="责任部门" prop="responsibleDeptNameSnapshot" width="130" show-overflow-tooltip />
        <el-table-column label="工号" prop="employeeNo" width="100" />
        <el-table-column label="责任人" prop="employeeNameSnapshot" width="100" />
        <el-table-column label="机车" prop="locomotive" width="120" />
        <el-table-column label="车次" prop="trainNo" width="100" />
        <el-table-column label="拟考核内容" prop="proposedAssessmentContent" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" prop="currentStatus" width="170">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.currentStatus)">{{ statusText(scope.row.currentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="470">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="openDetail(scope.row)">详情</el-button>
            <el-button v-hasPermi="['violation:daily:view']" link type="primary" icon="Tickets" @click="openLogs(scope.row)">日志</el-button>
            <el-button v-hasPermi="['violation:daily:view']" link type="primary" icon="ChatLineRound" @click="openFeedbacks(scope.row)">反馈</el-button>
            <el-button v-hasPermi="['violation:daily:attachment:view']" link type="primary" icon="Paperclip" @click="openAttachments(scope.row)">附件</el-button>
            <el-button v-if="scope.row.currentStatus === 'DRAFT'" v-hasPermi="['violation:daily:edit']" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.currentStatus === 'DRAFT'" v-hasPermi="['violation:daily:submit']" link type="primary" @click="quickAction(scope.row, 'submit')">提交</el-button>
            <el-button v-if="scope.row.currentStatus === 'ANALYST_SUBMITTED' || scope.row.currentStatus === 'LEADER_PENDING'" v-hasPermi="['violation:daily:leader-audit']" link type="primary" @click="quickAction(scope.row, 'leader-approve')">班长通过</el-button>
            <el-button v-if="scope.row.currentStatus === 'DIRECTOR_PENDING'" v-hasPermi="['violation:daily:director-audit']" link type="primary" @click="quickAction(scope.row, 'director-approve')">主任通过</el-button>
            <el-dropdown trigger="click" @command="(command) => openAction(scope.row, String(command))">
              <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="leader-return">班长退回</el-dropdown-item>
                  <el-dropdown-item command="director-return">主任退回</el-dropdown-item>
                  <el-dropdown-item command="dispatch-workshop">下发车间</el-dropdown-item>
                  <el-dropdown-item command="dispatch-team">下发车队</el-dropdown-item>
                  <el-dropdown-item command="dispatch-guide-group">下发指导组</el-dropdown-item>
                  <el-dropdown-item command="guide-confirm">指导组确认</el-dropdown-item>
                  <el-dropdown-item command="guide-reject">反馈不属实</el-dropdown-item>
                  <el-dropdown-item command="return-recheck">返回复核</el-dropdown-item>
                  <el-dropdown-item command="final-confirm">最终确认</el-dropdown-item>
                  <el-dropdown-item command="archive">入结果库</el-dropdown-item>
                  <el-dropdown-item command="cancel">撤销不计入</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-card v-else shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:result:export']" type="warning" plain icon="Download" @click="handleResultExport">导出结果库</el-button></el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getResultList" />
        </el-row>
      </template>

      <el-table v-loading="resultLoading" border :data="resultList">
        <el-table-column label="结果ID" prop="resultId" width="170" show-overflow-tooltip />
        <el-table-column label="业务记录ID" prop="recordId" width="170" show-overflow-tooltip />
        <el-table-column label="版本" prop="resultVersion" width="80" align="center" />
        <el-table-column label="结果状态" prop="resultStatus" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.resultStatus === 'ACTIVE' ? 'success' : 'info'">{{ scope.row.resultStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否计入" prop="included" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.included === '1' ? 'success' : 'danger'">{{ scope.row.included === '1' ? '计入' : '不计入' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入库人" prop="archivedUserName" width="120" />
        <el-table-column label="入库时间" prop="archivedTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.archivedTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更正来源" prop="correctedFromResultId" width="160" show-overflow-tooltip />
        <el-table-column label="更正原因" prop="correctReason" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="360">
          <template #default="scope">
            <el-button v-hasPermi="['violation:daily:result:view']" link type="primary" icon="View" @click="openResultDetail(scope.row)">详情</el-button>
            <el-button link type="primary" icon="Document" @click="openResultSnapshot(scope.row)">入库快照</el-button>
            <el-button v-hasPermi="['violation:daily:result:version:view']" link type="primary" icon="Clock" @click="openResultVersions(scope.row)">版本</el-button>
            <el-button v-hasPermi="['violation:daily:result:correct']" link type="primary" icon="EditPen" @click="openResultCorrect(scope.row)">更正</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="resultTotal > 0" v-model:page="resultQuery.pageNum" v-model:limit="resultQuery.pageSize" :total="resultTotal" @pagination="getResultList" />
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="formDialog.title" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="提报日期"><el-date-picker v-model="form.reportDate" value-format="YYYY-MM-DD" type="date" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="违章日期"><el-date-picker v-model="form.violationDate" value-format="YYYY-MM-DD" type="date" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="违章时间"><el-input v-model="form.violationTime" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="违标编码"><el-input v-model="form.violationCode" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="工号"><el-input v-model="form.employeeNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="责任人"><el-input v-model="form.employeeName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="责任部门"><el-input v-model="form.responsibleDeptName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="机车"><el-input v-model="form.locomotive" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="车次"><el-input v-model="form.trainNo" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="拟考核内容"><el-input v-model="form.proposedAssessmentContent" type="textarea" :rows="4" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="formDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialog.visible" title="Excel导入每日违标登记簿" width="560px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="提报日期">
          <el-date-picker v-model="upload.reportDate" value-format="YYYY-MM-DD" type="date" clearable />
        </el-form-item>
        <el-form-item label="业务年份">
          <el-input-number v-model="upload.businessYear" :min="2020" :max="2099" />
        </el-form-item>
        <el-form-item label="Sheet">
          <el-input v-model="upload.sheetName" placeholder="可选，默认自动识别" />
        </el-form-item>
        <el-upload ref="uploadRef" drag :limit="1" accept=".xls,.xlsx" :auto-upload="false" :on-change="handleFileChange">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖入 .xls/.xlsx 文件，或点击选择</div>
        </el-upload>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitImport">上传并预览</el-button>
        <el-button @click="uploadDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewDialog.visible" title="导入预览" width="94%" append-to-body>
      <el-alert v-if="currentImport" class="mb-3" type="info" show-icon :closable="false">
        <template #title>
          批次 {{ currentImport.importBatchId }}：共 {{ currentImport.totalRows }} 条，VALID {{ currentImport.validRows }} 条，NEED_CONFIRM {{ currentImport.warningRows }} 条，INVALID {{ currentImport.invalidRows }} 条
        </template>
      </el-alert>
      <el-row :gutter="10" class="mb-3">
        <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:preview']" icon="Refresh" @click="validateImport">重新校验</el-button></el-col>
        <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:submit']" type="primary" icon="Check" :disabled="!selectedImportRows.length" @click="submitSelectedRows">提交选中</el-button></el-col>
        <el-col :span="1.5"><el-button v-hasPermi="['violation:daily:export']" type="warning" icon="Download" @click="downloadErrorReport">错误报告</el-button></el-col>
      </el-row>
      <el-table v-loading="previewLoading" border :data="importRows" max-height="520" :row-class-name="importRowClassName" @selection-change="handlePreviewSelection">
        <el-table-column type="selection" width="48" :selectable="rowSelectable" />
        <el-table-column label="行号" prop="rowNo" width="70" />
        <el-table-column label="状态" width="130">
          <template #default="scope"><el-tag :type="validationTag(scope.row.validationStatus)">{{ scope.row.validationStatus }}</el-tag></template>
        </el-table-column>
        <el-table-column label="确认" prop="confirmStatus" width="120" />
        <el-table-column label="编码" prop="violationCode" width="100" />
        <el-table-column label="工号" prop="employeeNo" width="100" />
        <el-table-column label="责任人" prop="responsiblePersonName" width="100" />
        <el-table-column label="候选责任人" prop="candidatePersonNames" width="130" show-overflow-tooltip />
        <el-table-column label="违章日期" prop="parsedViolationDate" width="112" />
        <el-table-column label="机车" prop="parsedLocomotive" width="120" />
        <el-table-column label="车次" prop="parsedTrainNo" width="100" />
        <el-table-column label="拟考核内容" prop="proposedAssessmentContent" min-width="360" show-overflow-tooltip />
        <el-table-column label="校验信息" prop="validationMessage" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="scope"><el-button link type="primary" icon="Edit" @click="openPreviewEdit(scope.row)">确认</el-button></template>
        </el-table-column>
      </el-table>
      <pagination v-show="importRowsTotal > 0" v-model:page="importQuery.pageNum" v-model:limit="importQuery.pageSize" :total="importRowsTotal" @pagination="getImportRows" />
    </el-dialog>

    <el-dialog v-model="previewEditDialog.visible" title="预览行确认" width="720px" append-to-body>
      <el-form :model="previewForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="工号"><el-input v-model="previewForm.employeeNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="责任人"><el-input v-model="previewForm.responsiblePersonName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="责任部门"><el-input v-model="previewForm.responsibleDeptName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="违章日期"><el-date-picker v-model="previewForm.parsedViolationDate" value-format="YYYY-MM-DD" type="date" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="违标编码"><el-input v-model="previewForm.violationCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="确认状态"><el-select v-model="previewForm.confirmStatus"><el-option label="已确认" value="CONFIRMED" /><el-option label="未确认" value="UNCONFIRMED" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="确认说明"><el-input v-model="previewForm.confirmRemark" type="textarea" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPreviewEdit">保存并校验</el-button>
        <el-button @click="previewEditDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="actionDialog.visible" :title="actionDialog.title" width="620px" append-to-body>
      <el-form :model="actionForm" label-width="120px">
        <el-form-item v-if="requiresOrgName" :label="orgLabel"><el-input v-model="actionOrgName" /></el-form-item>
        <el-form-item v-if="actionDialog.action === 'guide-reject'" label="原因类型"><el-select v-model="actionForm.reasonType" placeholder="请选择"><el-option v-for="item in reasonOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
        <el-form-item v-if="actionDialog.action === 'guide-reject'" label="原因说明"><el-input v-model="actionForm.reasonDescription" type="textarea" :rows="3" /></el-form-item>
        <el-form-item v-if="actionDialog.action === 'final-confirm'" label="最终决定"><el-select v-model="actionForm.finalDecision"><el-option label="维持" value="MAINTAIN" /><el-option label="撤销不计入" value="CANCEL_EXCLUDED" /><el-option label="退回复核" value="RECHECK" /></el-select></el-form-item>
        <el-form-item v-if="actionDialog.action === 'final-confirm'" label="最终意见"><el-input v-model="actionForm.finalOpinion" type="textarea" :rows="3" /></el-form-item>
        <el-form-item v-if="actionDialog.action === 'cancel'" label="撤销原因"><el-input v-model="actionForm.cancelReason" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理意见"><el-input v-model="actionForm.opinion" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAction">确定</el-button>
        <el-button @click="actionDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logDialog.visible" title="流程日志" width="92%" append-to-body>
      <el-table v-loading="logLoading" border :data="flowLogList" max-height="520">
        <el-table-column label="动作" prop="actionCode" width="180" show-overflow-tooltip />
        <el-table-column label="前状态" prop="beforeStatus" width="170">
          <template #default="scope">{{ statusText(scope.row.beforeStatus) }}</template>
        </el-table-column>
        <el-table-column label="后状态" prop="afterStatus" width="170">
          <template #default="scope">{{ statusText(scope.row.afterStatus) }}</template>
        </el-table-column>
        <el-table-column label="操作人" prop="operatorNameSnapshot" width="120" />
        <el-table-column label="操作部门" prop="operatorDeptSnapshot" width="140" show-overflow-tooltip />
        <el-table-column label="意见/说明" prop="opinion" min-width="220" show-overflow-tooltip />
        <el-table-column label="附件引用" prop="attachmentRefs" width="160" show-overflow-tooltip />
        <el-table-column label="TraceId" prop="traceId" width="180" show-overflow-tooltip />
        <el-table-column label="时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="feedbackDialog.visible" title="不属实反馈" width="860px" append-to-body>
      <el-table v-loading="feedbackLoading" border :data="feedbackList">
        <el-table-column label="原因类型" prop="reasonType" width="140" />
        <el-table-column label="说明" prop="reasonDescription" min-width="240" show-overflow-tooltip />
        <el-table-column label="反馈部门" prop="feedbackDeptNameSnapshot" width="150" show-overflow-tooltip />
        <el-table-column label="反馈人" prop="feedbackUserNameSnapshot" width="120" />
        <el-table-column label="状态" prop="feedbackStatus" width="110" />
        <el-table-column label="附件引用" prop="attachmentRefs" width="170" show-overflow-tooltip />
        <el-table-column label="反馈时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="attachmentDialog.visible" title="业务附件" width="820px" append-to-body>
      <el-form label-width="96px" class="mb-3">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="附件类型">
              <el-select v-model="attachmentForm.attachmentType">
                <el-option label="证据附件" value="EVIDENCE" />
                <el-option label="反馈附件" value="FEEDBACK" />
                <el-option label="复核附件" value="RECHECK" />
                <el-option label="其他附件" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注">
              <el-input v-model="attachmentForm.remark" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-upload ref="attachmentUploadRef" drag :limit="1" :auto-upload="false" :on-change="handleAttachmentFileChange" :on-remove="handleAttachmentFileRemove">
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">拖入附件，或点击选择</div>
            </el-upload>
          </el-col>
        </el-row>
        <div class="mt-3">
          <el-button v-hasPermi="['violation:daily:attachment:upload']" type="primary" icon="Upload" :loading="attachmentUploading" @click="submitAttachmentUpload">上传附件</el-button>
        </div>
      </el-form>

      <el-table v-loading="attachmentLoading" border :data="attachmentList">
        <el-table-column label="文件名" prop="originalName" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" prop="attachmentType" width="110" />
        <el-table-column label="大小" width="100">
          <template #default="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="上传人" prop="uploadUserName" width="120" />
        <el-table-column label="上传时间" prop="createTime" width="170">
          <template #default="scope">
            <span>{{ proxy.parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="90">
          <template #default="scope">
            <el-button v-hasPermi="['violation:daily:attachment:download']" link type="primary" icon="Download" @click="handleAttachmentDownload(scope.row)">下载</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="resultVersionDialog.visible" title="结果库版本" width="900px" append-to-body>
      <el-row :gutter="10" class="mb-3">
        <el-col :span="6">
          <el-select v-model="versionCompare.sourceVersion" placeholder="源版本">
            <el-option v-for="item in resultVersionList" :key="`s-${item.resultVersion}`" :label="`版本 ${item.resultVersion}`" :value="item.resultVersion" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="versionCompare.targetVersion" placeholder="目标版本">
            <el-option v-for="item in resultVersionList" :key="`t-${item.resultVersion}`" :label="`版本 ${item.resultVersion}`" :value="item.resultVersion" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" icon="Operation" @click="loadVersionCompare">对比</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="resultVersionLoading" border :data="resultVersionList">
        <el-table-column label="版本" prop="resultVersion" width="80" align="center" />
        <el-table-column label="结果状态" prop="resultStatus" width="140" />
        <el-table-column label="是否计入" prop="included" width="100">
          <template #default="scope">{{ scope.row.included === '1' ? '计入' : '不计入' }}</template>
        </el-table-column>
        <el-table-column label="更正来源" prop="correctedFromResultId" width="160" show-overflow-tooltip />
        <el-table-column label="更正原因" prop="correctReason" min-width="180" show-overflow-tooltip />
        <el-table-column label="入库时间" prop="archivedTime" width="170">
          <template #default="scope">{{ proxy.parseTime(scope.row.archivedTime, '{y}-{m}-{d} {h}:{i}') }}</template>
        </el-table-column>
      </el-table>
      <el-table v-if="versionDiffList.length" class="mt-3" border :data="versionDiffList">
        <el-table-column label="字段" prop="field" width="200" />
        <el-table-column label="源版本值" prop="sourceValue" show-overflow-tooltip />
        <el-table-column label="目标版本值" prop="targetValue" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <el-dialog v-model="resultCorrectDialog.visible" title="结果库更正" width="560px" append-to-body>
      <el-form :model="resultCorrectForm" label-width="100px">
        <el-form-item label="是否计入">
          <el-select v-model="resultCorrectForm.included">
            <el-option label="计入" value="1" />
            <el-option label="不计入" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果状态">
          <el-input v-model="resultCorrectForm.resultStatus" />
        </el-form-item>
        <el-form-item label="更正原因">
          <el-input v-model="resultCorrectForm.correctReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitResultCorrect">提交更正</el-button>
        <el-button @click="resultCorrectDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DailyViolation" lang="ts">
import type { UploadFile } from 'element-plus';
import {
  addDailyViolationRecord,
  compareDailyViolationResultVersions,
  correctDailyViolationResult,
  downloadDailyViolationAttachment,
  getDailyViolationImport,
  getDailyViolationRecord,
  getDailyViolationResult,
  importDailyViolationExcel,
  listDailyViolationAttachments,
  listDailyViolationFeedbacks,
  listDailyViolationImportRows,
  listDailyViolationLogs,
  listDailyViolationRecords,
  listDailyViolationResultVersions,
  listDailyViolationResults,
  runDailyViolationAction,
  submitDailyViolationImportRows,
  uploadDailyViolationAttachment,
  updateDailyViolationImportRow,
  updateDailyViolationRecord,
  validateDailyViolationImport
} from '@/api/violation/daily';
import {
  DailyViolationActionForm,
  DailyViolationAttachmentVO,
  DailyViolationFeedbackVO,
  DailyViolationFlowLogVO,
  DailyViolationImportBatchVO,
  DailyViolationImportRowVO,
  DailyViolationRecordForm,
  DailyViolationRecordQuery,
  DailyViolationRecordVO,
  DailyViolationResultCompareVO,
  DailyViolationResultCorrectForm,
  DailyViolationResultQuery,
  DailyViolationResultVO
} from '@/api/violation/daily/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const activeTab = ref<'records' | 'results'>('records');
const loading = ref(false);
const resultLoading = ref(false);
const previewLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const resultTotal = ref(0);
const recordList = ref<DailyViolationRecordVO[]>([]);
const resultList = ref<DailyViolationResultVO[]>([]);
const flowLogList = ref<DailyViolationFlowLogVO[]>([]);
const feedbackList = ref<DailyViolationFeedbackVO[]>([]);
const queryFormRef = ref<ElFormInstance>();
const resultQueryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();
const uploadRef = ref<ElUploadInstance>();
const attachmentUploadRef = ref<ElUploadInstance>();
const importFile = ref<File>();
const attachmentFile = ref<File>();
const selectedImportRows = ref<DailyViolationImportRowVO[]>([]);
const currentImport = ref<DailyViolationImportBatchVO>();
const importRows = ref<DailyViolationImportRowVO[]>([]);
const importRowsTotal = ref(0);
const currentRecordId = ref<string | number>();
const currentPreviewRowId = ref<string | number>();
const attachmentList = ref<DailyViolationAttachmentVO[]>([]);
const resultVersionList = ref<DailyViolationResultVO[]>([]);
const versionDiffList = ref<DailyViolationResultCompareVO['diffs']>([]);
const logLoading = ref(false);
const feedbackLoading = ref(false);
const attachmentLoading = ref(false);
const attachmentUploading = ref(false);
const resultVersionLoading = ref(false);

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '分析员已提交', value: 'ANALYST_SUBMITTED' },
  { label: '主任待审核', value: 'DIRECTOR_PENDING' },
  { label: '主任待下发车间', value: 'DIRECTOR_APPROVED_PENDING_DISPATCH' },
  { label: '主任已下发车间', value: 'DIRECTOR_DISPATCHED_WORKSHOP' },
  { label: '车队待确认', value: 'TEAM_PENDING' },
  { label: '指导组待确认', value: 'GUIDE_PENDING' },
  { label: '指导组确认无误', value: 'GUIDE_CONFIRMED' },
  { label: '指导组反馈不属实', value: 'GUIDE_REJECTED' },
  { label: '返回主任待复核', value: 'RETURNED_DIRECTOR_RECHECK' },
  { label: '主任最终确认', value: 'FINAL_CONFIRMED' },
  { label: '已入结果库', value: 'ARCHIVED' },
  { label: '已撤销不计入', value: 'CANCELLED_EXCLUDED' }
];
const reasonOptions = ['责任人不符', '工号不符', '时间不符', '地点不符', '机车/车次不符', '违标编码不符', '事实描述不符', '重复提报', '其他'];
const queryParams = reactive<DailyViolationRecordQuery>({ pageNum: 1, pageSize: 10 });
const resultQuery = reactive<DailyViolationResultQuery>({ pageNum: 1, pageSize: 10 });
const importQuery = reactive<PageQuery & { validationStatus?: string; confirmStatus?: string }>({ pageNum: 1, pageSize: 10 });
const formDialog = reactive<DialogOption>({ visible: false, title: '' });
const uploadDialog = reactive<DialogOption>({ visible: false, title: '' });
const previewDialog = reactive<DialogOption>({ visible: false, title: '' });
const previewEditDialog = reactive<DialogOption>({ visible: false, title: '' });
const actionDialog = reactive<DialogOption & { action?: string }>({ visible: false, title: '', action: '' });
const logDialog = reactive<DialogOption>({ visible: false, title: '' });
const feedbackDialog = reactive<DialogOption>({ visible: false, title: '' });
const attachmentDialog = reactive<DialogOption>({ visible: false, title: '' });
const resultVersionDialog = reactive<DialogOption>({ visible: false, title: '' });
const resultCorrectDialog = reactive<DialogOption>({ visible: false, title: '' });
const upload = reactive({ reportDate: '', businessYear: new Date().getFullYear(), sheetName: '' });
const attachmentForm = reactive({ businessAction: 'DAILY_VIOLATION_ATTACHMENT', attachmentType: 'EVIDENCE', remark: '' });
const resultCorrectForm = ref<DailyViolationResultCorrectForm>({ correctReason: '' });
const currentResultId = ref<string | number>();
const versionCompare = reactive<{ sourceVersion?: number; targetVersion?: number }>({});
const form = ref<DailyViolationRecordForm>({});
const previewForm = ref<Partial<DailyViolationImportRowVO>>({});
const actionForm = ref<DailyViolationActionForm>({});

const getList = async () => {
  loading.value = true;
  const res = await listDailyViolationRecords(queryParams);
  recordList.value = res.rows || [];
  total.value = res.total || 0;
  loading.value = false;
};

const getResultList = async () => {
  resultLoading.value = true;
  const res = await listDailyViolationResults(resultQuery);
  resultList.value = res.rows || [];
  resultTotal.value = res.total || 0;
  resultLoading.value = false;
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleResultQuery = () => {
  resultQuery.pageNum = 1;
  getResultList();
};

const resetResultQuery = () => {
  resultQueryFormRef.value?.resetFields();
  handleResultQuery();
};

const handleAdd = () => {
  form.value = { reportDate: today() };
  currentRecordId.value = undefined;
  formDialog.title = '新增每日违标草稿';
  formDialog.visible = true;
};

const handleUpdate = async (row: DailyViolationRecordVO) => {
  const res = await getDailyViolationRecord(row.recordId);
  currentRecordId.value = row.recordId;
  form.value = {
    reportDate: res.data.reportDate,
    violationDate: res.data.violationDate,
    violationTime: res.data.violationTime,
    violationCode: res.data.violationCode,
    proposedAssessmentContent: res.data.proposedAssessmentContent,
    responsibleDeptName: res.data.responsibleDeptNameSnapshot,
    employeeNo: res.data.employeeNo,
    employeeName: res.data.employeeNameSnapshot,
    locomotive: res.data.locomotive,
    trainNo: res.data.trainNo,
    location: res.data.location
  };
  formDialog.title = '编辑每日违标草稿';
  formDialog.visible = true;
};

const submitForm = async () => {
  if (currentRecordId.value) {
    await updateDailyViolationRecord(currentRecordId.value, form.value);
  } else {
    await addDailyViolationRecord(form.value);
  }
  proxy?.$modal.msgSuccess('保存成功');
  formDialog.visible = false;
  getList();
};

const handleImport = () => {
  upload.reportDate = '';
  upload.businessYear = new Date().getFullYear();
  upload.sheetName = '';
  importFile.value = undefined;
  uploadRef.value?.clearFiles();
  uploadDialog.visible = true;
};

const handleFileChange = (file: UploadFile) => {
  importFile.value = file.raw;
};

const submitImport = async () => {
  if (!importFile.value) {
    proxy?.$modal.msgError('请选择 Excel 文件');
    return;
  }
  const data = new FormData();
  data.append('file', importFile.value);
  if (upload.reportDate) data.append('reportDate', upload.reportDate);
  if (upload.businessYear) data.append('businessYear', String(upload.businessYear));
  if (upload.sheetName) data.append('sheetName', upload.sheetName);
  const res = await importDailyViolationExcel(data);
  currentImport.value = res.data;
  uploadDialog.visible = false;
  previewDialog.visible = true;
  getImportRows();
};

const getImportRows = async () => {
  if (!currentImport.value) return;
  previewLoading.value = true;
  const res = await listDailyViolationImportRows(currentImport.value.importBatchId, importQuery);
  importRows.value = res.rows || [];
  importRowsTotal.value = res.total || 0;
  selectedImportRows.value = [];
  previewLoading.value = false;
};

const openPreviewEdit = (row: DailyViolationImportRowVO) => {
  currentPreviewRowId.value = row.rowId;
  previewForm.value = { ...row, confirmStatus: row.confirmStatus === 'CONFIRMED' ? 'CONFIRMED' : 'CONFIRMED' };
  previewEditDialog.visible = true;
};

const submitPreviewEdit = async () => {
  if (!currentImport.value || !currentPreviewRowId.value) return;
  await updateDailyViolationImportRow(currentImport.value.importBatchId, currentPreviewRowId.value, previewForm.value);
  proxy?.$modal.msgSuccess('已保存并重新校验');
  previewEditDialog.visible = false;
  currentImport.value = (await getDailyViolationImport(currentImport.value.importBatchId)).data;
  getImportRows();
};

const validateImport = async () => {
  if (!currentImport.value) return;
  currentImport.value = (await validateDailyViolationImport(currentImport.value.importBatchId)).data;
  getImportRows();
};

const submitSelectedRows = async () => {
  if (!currentImport.value) return;
  await submitDailyViolationImportRows(currentImport.value.importBatchId, selectedImportRows.value.map((item) => item.rowId));
  proxy?.$modal.msgSuccess('提交成功');
  previewDialog.visible = false;
  getList();
};

const handlePreviewSelection = (selection: DailyViolationImportRowVO[]) => {
  selectedImportRows.value = selection;
};

const rowSelectable = (row: DailyViolationImportRowVO) => {
  return row.validationStatus === 'VALID' || (row.validationStatus === 'NEED_CONFIRM' && row.confirmStatus === 'CONFIRMED');
};

const quickAction = async (row: DailyViolationRecordVO, action: string) => {
  await runDailyViolationAction(row.recordId, action, {});
  proxy?.$modal.msgSuccess('操作成功');
  getList();
};

const openAction = (row: DailyViolationRecordVO, action: string) => {
  currentRecordId.value = row.recordId;
  actionDialog.action = action;
  actionDialog.title = actionTitle(action);
  actionForm.value = {};
  actionDialog.visible = true;
};

const submitAction = async () => {
  if (!currentRecordId.value || !actionDialog.action) return;
  if (actionDialog.action === 'dispatch-workshop') actionForm.value.workshopName = actionOrgName.value;
  if (actionDialog.action === 'dispatch-team') actionForm.value.teamName = actionOrgName.value;
  if (actionDialog.action === 'dispatch-guide-group') actionForm.value.guideGroupName = actionOrgName.value;
  await runDailyViolationAction(currentRecordId.value, actionDialog.action, actionForm.value);
  proxy?.$modal.msgSuccess('操作成功');
  actionDialog.visible = false;
  getList();
};

const actionOrgName = computed({
  get: () => actionForm.value.workshopName || actionForm.value.teamName || actionForm.value.guideGroupName || '',
  set: (value: string) => {
    if (actionDialog.action === 'dispatch-workshop') actionForm.value.workshopName = value;
    if (actionDialog.action === 'dispatch-team') actionForm.value.teamName = value;
    if (actionDialog.action === 'dispatch-guide-group') actionForm.value.guideGroupName = value;
  }
});

const requiresOrgName = computed(() => ['dispatch-workshop', 'dispatch-team', 'dispatch-guide-group'].includes(actionDialog.action || ''));
const orgLabel = computed(() => {
  if (actionDialog.action === 'dispatch-team') return '车队名称';
  if (actionDialog.action === 'dispatch-guide-group') return '指导组名称';
  return '车间名称';
});

const openDetail = async (row: DailyViolationRecordVO) => {
  const res = await getDailyViolationRecord(row.recordId);
  proxy?.$modal.alert(`<pre style="white-space:pre-wrap;text-align:left">${JSON.stringify(res.data, null, 2)}</pre>`, '记录详情', { dangerouslyUseHTMLString: true });
};

const openLogs = async (row: DailyViolationRecordVO) => {
  currentRecordId.value = row.recordId;
  logDialog.visible = true;
  logLoading.value = true;
  const res = await listDailyViolationLogs(row.recordId);
  flowLogList.value = res.data || [];
  logLoading.value = false;
};

const openFeedbacks = async (row: DailyViolationRecordVO) => {
  currentRecordId.value = row.recordId;
  feedbackDialog.visible = true;
  feedbackLoading.value = true;
  const res = await listDailyViolationFeedbacks(row.recordId);
  feedbackList.value = res.data || [];
  feedbackLoading.value = false;
};

const openResultDetail = async (row: DailyViolationResultVO) => {
  const [resultRes, recordRes] = await Promise.all([getDailyViolationResult(row.resultId), getDailyViolationRecord(row.recordId)]);
  proxy?.$modal.alert(`<pre style="white-space:pre-wrap;text-align:left">${JSON.stringify({ result: resultRes.data, record: recordRes.data }, null, 2)}</pre>`, '结果库详情', { dangerouslyUseHTMLString: true });
};

const openResultSnapshot = (row: DailyViolationResultVO) => {
  const snapshot = parseJson(row.resultSnapshot);
  proxy?.$modal.alert(`<pre style="white-space:pre-wrap;text-align:left">${JSON.stringify(snapshot, null, 2)}</pre>`, '入库快照', { dangerouslyUseHTMLString: true });
};

const openResultVersions = async (row: DailyViolationResultVO) => {
  currentResultId.value = row.resultId;
  resultVersionDialog.visible = true;
  versionDiffList.value = [];
  resultVersionLoading.value = true;
  try {
    const res = await listDailyViolationResultVersions(row.resultId);
    resultVersionList.value = res.data || [];
    versionCompare.sourceVersion = resultVersionList.value[0]?.resultVersion;
    versionCompare.targetVersion = resultVersionList.value[resultVersionList.value.length - 1]?.resultVersion;
  } finally {
    resultVersionLoading.value = false;
  }
};

const loadVersionCompare = async () => {
  if (!currentResultId.value || !versionCompare.sourceVersion || !versionCompare.targetVersion) return;
  const res = await compareDailyViolationResultVersions(currentResultId.value, versionCompare.sourceVersion, versionCompare.targetVersion);
  versionDiffList.value = res.data?.diffs || [];
};

const openResultCorrect = (row: DailyViolationResultVO) => {
  currentResultId.value = row.resultId;
  resultCorrectForm.value = {
    included: row.included,
    resultStatus: row.resultStatus,
    correctReason: ''
  };
  resultCorrectDialog.visible = true;
};

const submitResultCorrect = async () => {
  if (!currentResultId.value) return;
  if (!resultCorrectForm.value.correctReason) {
    proxy?.$modal.msgError('请填写更正原因');
    return;
  }
  await correctDailyViolationResult(currentResultId.value, resultCorrectForm.value);
  proxy?.$modal.msgSuccess('更正已追加为新版本');
  resultCorrectDialog.visible = false;
  getResultList();
};

const openAttachments = async (row: DailyViolationRecordVO) => {
  currentRecordId.value = row.recordId;
  attachmentForm.businessAction = 'DAILY_VIOLATION_ATTACHMENT';
  attachmentForm.attachmentType = 'EVIDENCE';
  attachmentForm.remark = '';
  attachmentFile.value = undefined;
  attachmentUploadRef.value?.clearFiles();
  attachmentDialog.visible = true;
  await getAttachments();
};

const getAttachments = async () => {
  if (!currentRecordId.value) return;
  attachmentLoading.value = true;
  const res = await listDailyViolationAttachments(currentRecordId.value);
  attachmentList.value = res.data || [];
  attachmentLoading.value = false;
};

const handleAttachmentFileChange = (file: UploadFile) => {
  attachmentFile.value = file.raw;
};

const handleAttachmentFileRemove = () => {
  attachmentFile.value = undefined;
};

const submitAttachmentUpload = async () => {
  if (!currentRecordId.value) return;
  if (!attachmentFile.value) {
    proxy?.$modal.msgError('请选择附件文件');
    return;
  }
  attachmentUploading.value = true;
  const data = new FormData();
  data.append('file', attachmentFile.value);
  data.append('businessAction', attachmentForm.businessAction);
  data.append('attachmentType', attachmentForm.attachmentType);
  if (attachmentForm.remark) data.append('remark', attachmentForm.remark);
  try {
    await uploadDailyViolationAttachment(currentRecordId.value, data);
    proxy?.$modal.msgSuccess('附件上传成功');
    attachmentFile.value = undefined;
    attachmentUploadRef.value?.clearFiles();
    await getAttachments();
  } finally {
    attachmentUploading.value = false;
  }
};

const handleAttachmentDownload = async (row: DailyViolationAttachmentVO) => {
  if (!currentRecordId.value) return;
  const data = await downloadDailyViolationAttachment(currentRecordId.value, row.id);
  const url = window.URL.createObjectURL(new Blob([data], { type: row.contentType || 'application/octet-stream' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = row.originalName || `daily_violation_attachment_${row.id}`;
  link.click();
  window.URL.revokeObjectURL(url);
};

const handleExport = () => {
  proxy?.download('violation/daily/records/export', queryParams, `daily_violation_${Date.now()}.xlsx`);
};

const handleResultExport = () => {
  proxy?.download('violation/daily/results/export', resultQuery, `daily_violation_results_${Date.now()}.xlsx`);
};

const downloadErrorReport = () => {
  if (!currentImport.value) return;
  proxy?.download(`violation/daily/imports/${currentImport.value.importBatchId}/error-report`, {}, `daily_violation_errors_${Date.now()}.xlsx`);
};

const statusText = (value: string) => statusOptions.find((item) => item.value === value)?.label || value;
const statusTag = (value: string) => (['ARCHIVED', 'FINAL_CONFIRMED', 'GUIDE_CONFIRMED'].includes(value) ? 'success' : value === 'CANCELLED_EXCLUDED' || value === 'GUIDE_REJECTED' ? 'danger' : 'info');
const validationTag = (value: string) => (value === 'VALID' ? 'success' : value === 'NEED_CONFIRM' ? 'warning' : 'danger');
const importRowClassName = ({ row }: { row: DailyViolationImportRowVO }) => (row.validationStatus === 'INVALID' ? 'row-invalid' : row.validationStatus === 'NEED_CONFIRM' ? 'row-warning' : '');
const formatFileSize = (size?: number) => {
  if (!size) return '-';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
};
const parseJson = (value?: string) => {
  if (!value) return {};
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
};
const actionTitle = (action: string) => {
  const map: Record<string, string> = {
    'leader-return': '班长退回',
    'director-return': '主任退回',
    'dispatch-workshop': '主任下发车间',
    'dispatch-team': '车间下发车队',
    'dispatch-guide-group': '车队下发指导组',
    'guide-confirm': '指导组确认无误',
    'guide-reject': '指导组反馈不属实',
    'return-recheck': '返回主任复核',
    'final-confirm': '主任最终确认',
    archive: '入结果库',
    cancel: '撤销不计入'
  };
  return map[action] || action;
};
const today = () => new Date().toISOString().slice(0, 10);

watch(activeTab, (tab) => {
  if (tab === 'results' && !resultList.value.length) {
    getResultList();
  }
});

onMounted(() => getList());
</script>

<style lang="scss" scoped>
.daily-violation-page {
  :deep(.row-invalid) {
    background: #fff1f2;
  }

  :deep(.row-warning) {
    background: #fffbeb;
  }
}
</style>
