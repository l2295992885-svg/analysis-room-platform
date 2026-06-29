package org.dromara.system.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IDailyViolationService {

    TableDataInfo<DailyViolationRecord> queryRecords(DailyViolationRecordBo bo, PageQuery pageQuery);

    List<DailyViolationRecord> queryRecordList(DailyViolationRecordBo bo);

    DailyViolationRecord queryRecord(Long recordId);

    boolean createRecord(DailyViolationRecordBo bo);

    boolean updateRecord(Long recordId, DailyViolationRecordBo bo);

    boolean submitRecord(Long recordId, DailyViolationActionBo bo);

    boolean leaderApprove(Long recordId, DailyViolationActionBo bo);

    boolean leaderReturn(Long recordId, DailyViolationActionBo bo);

    boolean directorApprove(Long recordId, DailyViolationActionBo bo);

    boolean directorReturn(Long recordId, DailyViolationActionBo bo);

    boolean dispatchWorkshop(Long recordId, DailyViolationActionBo bo);

    boolean dispatchTeam(Long recordId, DailyViolationActionBo bo);

    boolean dispatchGuideGroup(Long recordId, DailyViolationActionBo bo);

    boolean guideConfirm(Long recordId, DailyViolationActionBo bo);

    boolean guideReject(Long recordId, DailyViolationActionBo bo);

    boolean returnRecheck(Long recordId, DailyViolationActionBo bo);

    boolean finalConfirm(Long recordId, DailyViolationActionBo bo);

    boolean archive(Long recordId, DailyViolationActionBo bo);

    boolean cancel(Long recordId, DailyViolationActionBo bo);

    TableDataInfo<DailyViolationResult> queryResults(DailyViolationResultBo bo, PageQuery pageQuery);

    List<DailyViolationResult> queryResultList(DailyViolationResultBo bo);

    DailyViolationResult queryResult(Long resultId);

    List<DailyViolationResult> queryResultVersions(Long resultId);

    Map<String, Object> compareResultVersions(Long resultId, Integer sourceVersion, Integer targetVersion);

    DailyViolationResult correctResult(Long resultId, DailyViolationResultCorrectBo bo);

    void exportResults(DailyViolationResultBo bo, HttpServletResponse response);

    List<BizFileBindVo> queryAttachments(Long recordId);

    BizFileBindVo uploadAttachment(Long recordId, MultipartFile file, String businessAction, String attachmentType, String remark);

    void downloadAttachment(Long recordId, Long attachmentId, HttpServletResponse response) throws IOException;

    DailyViolationImportBatch importExcel(MultipartFile file, Date reportDate, Integer businessYear, String sheetName);

    DailyViolationImportBatch queryImportBatch(Long importBatchId);

    TableDataInfo<DailyViolationImportRow> queryImportRows(Long importBatchId, String validationStatus, String confirmStatus, PageQuery pageQuery);

    DailyViolationImportRow updateImportRow(Long importBatchId, Long rowId, DailyViolationImportRowBo bo);

    DailyViolationImportBatch validateImportBatch(Long importBatchId);

    List<Long> submitImportRows(Long importBatchId, DailyViolationImportSubmitBo bo);

    List<DailyViolationFlowLog> queryFlowLogs(Long recordId);

    List<DailyViolationFeedback> queryFeedbacks(Long recordId);

    void exportRecords(DailyViolationRecordBo bo, HttpServletResponse response);

    void exportErrorReport(Long importBatchId, HttpServletResponse response);
}
