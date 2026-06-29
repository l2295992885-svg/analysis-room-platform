package org.dromara.system.controller.business;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.DailyViolationFeedback;
import org.dromara.system.domain.DailyViolationFlowLog;
import org.dromara.system.domain.DailyViolationImportBatch;
import org.dromara.system.domain.DailyViolationImportRow;
import org.dromara.system.domain.DailyViolationRecord;
import org.dromara.system.domain.DailyViolationResult;
import org.dromara.system.domain.bo.DailyViolationActionBo;
import org.dromara.system.domain.bo.DailyViolationImportRowBo;
import org.dromara.system.domain.bo.DailyViolationImportSubmitBo;
import org.dromara.system.domain.bo.DailyViolationRecordBo;
import org.dromara.system.domain.bo.DailyViolationResultCorrectBo;
import org.dromara.system.domain.bo.DailyViolationResultBo;
import org.dromara.system.domain.vo.BizFileBindVo;
import org.dromara.system.service.IDailyViolationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/violation/daily")
public class DailyViolationController extends BaseController {

    private final IDailyViolationService dailyViolationService;

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/records")
    public TableDataInfo<DailyViolationRecord> list(DailyViolationRecordBo bo, PageQuery pageQuery) {
        return dailyViolationService.queryRecords(bo, pageQuery);
    }

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/records/{recordId}")
    public R<DailyViolationRecord> getInfo(@PathVariable Long recordId) {
        return R.ok(dailyViolationService.queryRecord(recordId));
    }

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/records/{recordId}/logs")
    public R<List<DailyViolationFlowLog>> logs(@PathVariable Long recordId) {
        return R.ok(dailyViolationService.queryFlowLogs(recordId));
    }

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/records/{recordId}/feedbacks")
    public R<List<DailyViolationFeedback>> feedbacks(@PathVariable Long recordId) {
        return R.ok(dailyViolationService.queryFeedbacks(recordId));
    }

    @SaCheckPermission("violation:daily:add")
    @Log(title = "每日LKJ违标公示新增", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/records")
    public R<Void> add(@RequestBody DailyViolationRecordBo bo) {
        return toAjax(dailyViolationService.createRecord(bo));
    }

    @SaCheckPermission("violation:daily:edit")
    @Log(title = "每日LKJ违标公示编辑", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/records/{recordId}")
    public R<Void> edit(@PathVariable Long recordId, @RequestBody DailyViolationRecordBo bo) {
        return toAjax(dailyViolationService.updateRecord(recordId, bo));
    }

    @SaCheckPermission("violation:daily:submit")
    @Log(title = "每日LKJ违标公示提交班长", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/records/{recordId}/submit")
    public R<Void> submit(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.submitRecord(recordId, bo));
    }

    @SaCheckPermission("violation:daily:leader-audit")
    @Log(title = "每日LKJ违标公示班长审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/leader-approve")
    public R<Void> leaderApprove(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.leaderApprove(recordId, bo));
    }

    @SaCheckPermission("violation:daily:return")
    @Log(title = "每日LKJ违标公示班长退回", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/leader-return")
    public R<Void> leaderReturn(@PathVariable Long recordId, @RequestBody DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.leaderReturn(recordId, bo));
    }

    @SaCheckPermission("violation:daily:director-audit")
    @Log(title = "每日LKJ违标公示主任审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/director-approve")
    public R<Void> directorApprove(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.directorApprove(recordId, bo));
    }

    @SaCheckPermission("violation:daily:return")
    @Log(title = "每日LKJ违标公示主任退回", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/director-return")
    public R<Void> directorReturn(@PathVariable Long recordId, @RequestBody DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.directorReturn(recordId, bo));
    }

    @SaCheckPermission("violation:daily:dispatch-workshop")
    @Log(title = "每日LKJ违标公示下发车间", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/dispatch-workshop")
    public R<Void> dispatchWorkshop(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.dispatchWorkshop(recordId, bo));
    }

    @SaCheckPermission("violation:daily:dispatch-team")
    @Log(title = "每日LKJ违标公示下发车队", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/dispatch-team")
    public R<Void> dispatchTeam(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.dispatchTeam(recordId, bo));
    }

    @SaCheckPermission("violation:daily:dispatch-guide-group")
    @Log(title = "每日LKJ违标公示下发指导组", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/dispatch-guide-group")
    public R<Void> dispatchGuideGroup(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.dispatchGuideGroup(recordId, bo));
    }

    @SaCheckPermission("violation:daily:guide-confirm")
    @Log(title = "每日LKJ违标公示指导组确认无误", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/guide-confirm")
    public R<Void> guideConfirm(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.guideConfirm(recordId, bo));
    }

    @SaCheckPermission("violation:daily:feedback")
    @Log(title = "每日LKJ违标公示指导组反馈不属实", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/guide-reject")
    public R<Void> guideReject(@PathVariable Long recordId, @RequestBody DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.guideReject(recordId, bo));
    }

    @SaCheckPermission("violation:daily:review")
    @Log(title = "每日LKJ违标公示返回复核", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/return-recheck")
    public R<Void> returnRecheck(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.returnRecheck(recordId, bo));
    }

    @SaCheckPermission("violation:daily:confirm")
    @Log(title = "每日LKJ违标公示最终确认", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/final-confirm")
    public R<Void> finalConfirm(@PathVariable Long recordId, @RequestBody DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.finalConfirm(recordId, bo));
    }

    @SaCheckPermission("violation:daily:archive")
    @Log(title = "每日LKJ违标公示入结果库", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/archive")
    public R<Void> archive(@PathVariable Long recordId, @RequestBody(required = false) DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.archive(recordId, bo));
    }

    @SaCheckPermission("violation:daily:cancel")
    @Log(title = "每日LKJ违标公示撤销不计入", businessType = BusinessType.UPDATE)
    @PostMapping("/records/{recordId}/cancel")
    public R<Void> cancel(@PathVariable Long recordId, @RequestBody DailyViolationActionBo bo) {
        return toAjax(dailyViolationService.cancel(recordId, bo));
    }

    @SaCheckPermission("violation:daily:result:view")
    @GetMapping("/results")
    public TableDataInfo<DailyViolationResult> resultList(DailyViolationResultBo bo, PageQuery pageQuery) {
        return dailyViolationService.queryResults(bo, pageQuery);
    }

    @SaCheckPermission("violation:daily:result:view")
    @GetMapping("/results/{resultId}")
    public R<DailyViolationResult> resultInfo(@PathVariable Long resultId) {
        return R.ok(dailyViolationService.queryResult(resultId));
    }

    @SaCheckPermission("violation:daily:result:version:view")
    @GetMapping("/results/{resultId}/versions")
    public R<List<DailyViolationResult>> resultVersions(@PathVariable Long resultId) {
        return R.ok(dailyViolationService.queryResultVersions(resultId));
    }

    @SaCheckPermission("violation:daily:result:version:view")
    @GetMapping("/results/{resultId}/versions/compare")
    public R<Map<String, Object>> compareResultVersions(@PathVariable Long resultId,
                                                        @RequestParam Integer sourceVersion,
                                                        @RequestParam Integer targetVersion) {
        return R.ok(dailyViolationService.compareResultVersions(resultId, sourceVersion, targetVersion));
    }

    @SaCheckPermission("violation:daily:result:correct")
    @Log(title = "每日LKJ违标公示结果库更正", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/results/{resultId}/correct")
    public R<DailyViolationResult> correctResult(@PathVariable Long resultId,
                                                 @RequestBody DailyViolationResultCorrectBo bo) {
        return R.ok(dailyViolationService.correctResult(resultId, bo));
    }

    @SaCheckPermission("violation:daily:result:export")
    @Log(title = "每日LKJ违标公示结果库导出", businessType = BusinessType.EXPORT)
    @PostMapping("/results/export")
    public void exportResults(DailyViolationResultBo bo, HttpServletResponse response) {
        dailyViolationService.exportResults(bo, response);
    }

    @SaCheckPermission("violation:daily:attachment:view")
    @GetMapping("/records/{recordId}/attachments")
    public R<List<BizFileBindVo>> attachments(@PathVariable Long recordId) {
        return R.ok(dailyViolationService.queryAttachments(recordId));
    }

    @SaCheckPermission("violation:daily:attachment:upload")
    @Log(title = "每日LKJ违标公示附件上传", businessType = BusinessType.INSERT)
    @PostMapping(value = "/records/{recordId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<BizFileBindVo> uploadAttachment(@PathVariable Long recordId,
                                             @RequestPart("file") MultipartFile file,
                                             @RequestParam(value = "businessAction", required = false) String businessAction,
                                             @RequestParam(value = "attachmentType", required = false) String attachmentType,
                                             @RequestParam(value = "remark", required = false) String remark) {
        return R.ok(dailyViolationService.uploadAttachment(recordId, file, businessAction, attachmentType, remark));
    }

    @SaCheckPermission("violation:daily:attachment:download")
    @Log(title = "每日LKJ违标公示附件下载", businessType = BusinessType.EXPORT)
    @GetMapping("/records/{recordId}/attachments/{attachmentId}/download")
    public void downloadAttachment(@PathVariable Long recordId,
                                   @PathVariable Long attachmentId,
                                   HttpServletResponse response) throws IOException {
        dailyViolationService.downloadAttachment(recordId, attachmentId, response);
    }

    @SaCheckPermission("violation:daily:export")
    @Log(title = "每日LKJ违标公示导出", businessType = BusinessType.EXPORT)
    @PostMapping("/records/export")
    public void export(DailyViolationRecordBo bo, HttpServletResponse response) {
        dailyViolationService.exportRecords(bo, response);
    }

    @SaCheckPermission("violation:daily:import")
    @Log(title = "每日LKJ违标公示Excel导入", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<DailyViolationImportBatch> importExcel(@RequestPart("file") MultipartFile file,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date reportDate,
                                                    @RequestParam(required = false) Integer businessYear,
                                                    @RequestParam(required = false) String sheetName) {
        return R.ok(dailyViolationService.importExcel(file, reportDate, businessYear, sheetName));
    }

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/imports/{importBatchId}")
    public R<DailyViolationImportBatch> importInfo(@PathVariable Long importBatchId) {
        return R.ok(dailyViolationService.queryImportBatch(importBatchId));
    }

    @SaCheckPermission("violation:daily:view")
    @GetMapping("/imports/{importBatchId}/rows")
    public TableDataInfo<DailyViolationImportRow> importRows(@PathVariable Long importBatchId,
                                                             @RequestParam(required = false) String validationStatus,
                                                             @RequestParam(required = false) String confirmStatus,
                                                             PageQuery pageQuery) {
        return dailyViolationService.queryImportRows(importBatchId, validationStatus, confirmStatus, pageQuery);
    }

    @SaCheckPermission("violation:daily:preview")
    @Log(title = "每日LKJ违标公示预览行确认", businessType = BusinessType.UPDATE)
    @PutMapping("/imports/{importBatchId}/rows/{rowId}")
    public R<DailyViolationImportRow> updateImportRow(@PathVariable Long importBatchId,
                                                      @PathVariable Long rowId,
                                                      @RequestBody DailyViolationImportRowBo bo) {
        return R.ok(dailyViolationService.updateImportRow(importBatchId, rowId, bo));
    }

    @SaCheckPermission("violation:daily:preview")
    @Log(title = "每日LKJ违标公示重新校验", businessType = BusinessType.UPDATE)
    @PostMapping("/imports/{importBatchId}/validate")
    public R<DailyViolationImportBatch> validateImport(@PathVariable Long importBatchId) {
        return R.ok(dailyViolationService.validateImportBatch(importBatchId));
    }

    @SaCheckPermission("violation:daily:export")
    @Log(title = "每日LKJ违标公示导入错误报告", businessType = BusinessType.EXPORT)
    @PostMapping("/imports/{importBatchId}/error-report")
    public void errorReport(@PathVariable Long importBatchId, HttpServletResponse response) {
        dailyViolationService.exportErrorReport(importBatchId, response);
    }

    @SaCheckPermission("violation:daily:submit")
    @Log(title = "每日LKJ违标公示勾选提交", businessType = BusinessType.UPDATE)
    @PostMapping("/imports/{importBatchId}/submit")
    public R<List<Long>> submitImport(@PathVariable Long importBatchId, @RequestBody DailyViolationImportSubmitBo bo) {
        return R.ok(dailyViolationService.submitImportRows(importBatchId, bo));
    }
}
