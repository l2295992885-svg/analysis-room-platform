package org.dromara.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.domain.model.LoginUser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BasePersonnel;
import org.dromara.system.domain.BaseViolationCode;
import org.dromara.system.domain.BizMessage;
import org.dromara.system.domain.BizTask;
import org.dromara.system.domain.DailyViolationBatch;
import org.dromara.system.domain.DailyViolationFeedback;
import org.dromara.system.domain.DailyViolationFlowLog;
import org.dromara.system.domain.DailyViolationImportBatch;
import org.dromara.system.domain.DailyViolationImportError;
import org.dromara.system.domain.DailyViolationImportRow;
import org.dromara.system.domain.DailyViolationRecord;
import org.dromara.system.domain.DailyViolationResult;
import org.dromara.system.domain.bo.DailyViolationActionBo;
import org.dromara.system.domain.bo.DailyViolationImportRowBo;
import org.dromara.system.domain.bo.DailyViolationImportSubmitBo;
import org.dromara.system.domain.bo.DailyViolationRecordBo;
import org.dromara.system.domain.bo.DailyViolationResultCorrectBo;
import org.dromara.system.domain.bo.DailyViolationResultBo;
import org.dromara.system.domain.bo.BizFileBindBo;
import org.dromara.system.domain.vo.BizFileBindVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.mapper.BasePersonnelMapper;
import org.dromara.system.mapper.BaseViolationCodeMapper;
import org.dromara.system.mapper.BizMessageMapper;
import org.dromara.system.mapper.BizTaskMapper;
import org.dromara.system.mapper.DailyViolationBatchMapper;
import org.dromara.system.mapper.DailyViolationFeedbackMapper;
import org.dromara.system.mapper.DailyViolationFlowLogMapper;
import org.dromara.system.mapper.DailyViolationImportBatchMapper;
import org.dromara.system.mapper.DailyViolationImportErrorMapper;
import org.dromara.system.mapper.DailyViolationImportRowMapper;
import org.dromara.system.mapper.DailyViolationRecordMapper;
import org.dromara.system.mapper.DailyViolationResultMapper;
import org.dromara.system.service.IDailyViolationService;
import org.dromara.system.service.IBizFileBindService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class DailyViolationServiceImpl implements IDailyViolationService {

    private static final String BUSINESS_TYPE = "DAILY_VIOLATION";
    private static final String FLAG_NO = "0";
    private static final String FLAG_YES = "1";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_ANALYST_SUBMITTED = "ANALYST_SUBMITTED";
    private static final String STATUS_LEADER_PENDING = "LEADER_PENDING";
    private static final String STATUS_DIRECTOR_PENDING = "DIRECTOR_PENDING";
    private static final String STATUS_DIRECTOR_APPROVED_PENDING_DISPATCH = "DIRECTOR_APPROVED_PENDING_DISPATCH";
    private static final String STATUS_DIRECTOR_DISPATCHED_WORKSHOP = "DIRECTOR_DISPATCHED_WORKSHOP";
    private static final String STATUS_TEAM_PENDING = "TEAM_PENDING";
    private static final String STATUS_GUIDE_PENDING = "GUIDE_PENDING";
    private static final String STATUS_GUIDE_CONFIRMED = "GUIDE_CONFIRMED";
    private static final String STATUS_GUIDE_REJECTED = "GUIDE_REJECTED";
    private static final String STATUS_RETURNED_DIRECTOR_RECHECK = "RETURNED_DIRECTOR_RECHECK";
    private static final String STATUS_FINAL_CONFIRMED = "FINAL_CONFIRMED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";
    private static final String STATUS_CANCELLED_EXCLUDED = "CANCELLED_EXCLUDED";
    private static final String ROLE_ANALYST = "analyst_test";
    private static final String ROLE_LEADER = "leader_test";
    private static final String ROLE_DIRECTOR = "director_test";
    private static final String ROLE_WORKSHOP = "workshop_test";
    private static final String ROLE_TEAM = "team_test";
    private static final String ROLE_GUIDE = "guide_test";
    private static final String PERMISSION_OVERRIDE = "violation:daily:override";
    private static final String VALID = "VALID";
    private static final String NEED_CONFIRM = "NEED_CONFIRM";
    private static final String INVALID = "INVALID";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String UNCONFIRMED = "UNCONFIRMED";
    private static final Pattern TITLE_DATE = Pattern.compile("(\\d{1,2})月(\\d{1,2})日");
    private static final Pattern TIME_TEXT = Pattern.compile("\\d{1,2}时\\d{1,2}分(?:\\d{1,2}秒)?(?:[-至~]\\d{1,2}时\\d{1,2}分(?:\\d{1,2}秒)?)?");
    private static final Pattern LOCOMOTIVE_TEXT = Pattern.compile("(HXD\\w*-\\d+|FXN\\w*-\\d+|DF\\w*-\\d+|HXN\\w*-\\d+)");
    private static final Pattern TRAIN_TEXT = Pattern.compile("([A-Z]?\\d{3,6})次");
    private static final Pattern PERSON_TEXT = Pattern.compile("(?:司机|副司机|二位司机)([\\u4e00-\\u9fa5]{2,4})");
    private static final String[] EXPORT_HEADERS = {
        "序号", "违标编码", "拟考核内容", "性质", "类别", "类型", "责任部门", "责任人", "处理考核",
        "下票部门", "工号", "时段", "党员", "票号", "指导司机", "情况不属实原因", "地点", "指导组", "ABCD配班"
    };

    private final DailyViolationRecordMapper recordMapper;
    private final DailyViolationBatchMapper batchMapper;
    private final DailyViolationImportBatchMapper importBatchMapper;
    private final DailyViolationImportRowMapper importRowMapper;
    private final DailyViolationImportErrorMapper importErrorMapper;
    private final DailyViolationFlowLogMapper flowLogMapper;
    private final DailyViolationFeedbackMapper feedbackMapper;
    private final DailyViolationResultMapper resultMapper;
    private final BaseViolationCodeMapper violationCodeMapper;
    private final BasePersonnelMapper personnelMapper;
    private final BizTaskMapper taskMapper;
    private final BizMessageMapper messageMapper;
    private final ISysOssService ossService;
    private final IBizFileBindService fileBindService;

    @Override
    public TableDataInfo<DailyViolationRecord> queryRecords(DailyViolationRecordBo bo, PageQuery pageQuery) {
        Page<DailyViolationRecord> page = recordMapper.selectPage(pageQuery.build(), buildRecordWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DailyViolationRecord> queryRecordList(DailyViolationRecordBo bo) {
        return recordMapper.selectList(buildRecordWrapper(bo));
    }

    @Override
    public DailyViolationRecord queryRecord(Long recordId) {
        DailyViolationRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new ServiceException("daily violation record not found");
        }
        checkRecordReadable(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRecord(DailyViolationRecordBo bo) {
        DailyViolationRecord record = new DailyViolationRecord();
        copyBo(record, bo);
        String changedFieldsJson = normalizeManualRecord(record);
        record.setRecordId(IdGeneratorUtil.nextLongId());
        record.setBatchId(ensureManualBatch(record));
        record.setCurrentStatus(STATUS_DRAFT);
        record.setVersion(1);
        record.setSourceType("MANUAL");
        record.setPreviewConfirmed(FLAG_YES);
        record.setDelFlag(FLAG_NO);
        fillCreate(record);
        boolean ok = recordMapper.insert(record) > 0;
        writeFlowLog(record, "CREATE", null, STATUS_DRAFT, "manual create", null, changedFieldsJson);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecord(Long recordId, DailyViolationRecordBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "EDIT");
        if (!STATUS_DRAFT.equals(record.getCurrentStatus())) {
            forbidden("non draft records cannot edit fact fields");
        }
        String before = JsonUtils.toJsonString(snapshot(record));
        copyBo(record, bo);
        String changedFieldsJson = normalizeManualRecord(record);
        record.setUpdateBy(LoginHelper.getUserId());
        record.setUpdateTime(now());
        boolean ok = recordMapper.updateById(record) > 0;
        writeFlowLog(record, "UPDATE", STATUS_DRAFT, STATUS_DRAFT, "manual update", null,
            StringUtils.blankToDefault(changedFieldsJson, before));
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRecord(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "SUBMIT");
        if (!STATUS_DRAFT.equals(record.getCurrentStatus())) {
            throw new ServiceException("only draft records can be submitted");
        }
        if (INVALID.equals(record.getValidationStatus())) {
            throw new ServiceException("invalid record cannot be submitted");
        }
        return transition(record, "SUBMIT_TO_LEADER", List.of(STATUS_DRAFT), STATUS_ANALYST_SUBMITTED,
            safeOpinion(bo), "leader_test", "班长待审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaderApprove(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "LEADER_AUDIT");
        return transition(record, "LEADER_APPROVE",
            List.of(STATUS_ANALYST_SUBMITTED, STATUS_LEADER_PENDING), STATUS_DIRECTOR_PENDING,
            safeOpinion(bo), "director_test", "主任待审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean leaderReturn(Long recordId, DailyViolationActionBo bo) {
        requireText(bo == null ? null : bo.getOpinion(), "return opinion is required");
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "LEADER_AUDIT");
        return transition(record, "LEADER_RETURN",
            List.of(STATUS_ANALYST_SUBMITTED, STATUS_LEADER_PENDING), STATUS_DRAFT,
            bo.getOpinion(), "analyst_test", "退回分析员修改");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean directorApprove(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "DIRECTOR_AUDIT");
        return transition(record, "DIRECTOR_APPROVE",
            List.of(STATUS_DIRECTOR_PENDING), STATUS_DIRECTOR_APPROVED_PENDING_DISPATCH,
            safeOpinion(bo), "director_test", "主任待下发车间");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean directorReturn(Long recordId, DailyViolationActionBo bo) {
        requireText(bo == null ? null : bo.getOpinion(), "return opinion is required");
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "DIRECTOR_AUDIT");
        return transition(record, "DIRECTOR_RETURN",
            List.of(STATUS_DIRECTOR_PENDING), STATUS_DRAFT,
            bo.getOpinion(), "analyst_test", "主任退回修改");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispatchWorkshop(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "DISPATCH_WORKSHOP");
        if (bo != null) {
            record.setWorkshopId(bo.getWorkshopId());
            record.setWorkshopName(bo.getWorkshopName());
        }
        return transition(record, "DISPATCH_WORKSHOP",
            List.of(STATUS_DIRECTOR_APPROVED_PENDING_DISPATCH), STATUS_DIRECTOR_DISPATCHED_WORKSHOP,
            safeOpinion(bo), "workshop_test", "车间待确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispatchTeam(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "DISPATCH_TEAM");
        if (bo != null) {
            record.setTeamId(bo.getTeamId());
            record.setTeamName(bo.getTeamName());
        }
        return transition(record, "DISPATCH_TEAM",
            List.of(STATUS_DIRECTOR_DISPATCHED_WORKSHOP), STATUS_TEAM_PENDING,
            safeOpinion(bo), "team_test", "车队待确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dispatchGuideGroup(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "DISPATCH_GUIDE_GROUP");
        if (bo != null) {
            record.setGuideGroupId(bo.getGuideGroupId());
            record.setGuideGroupName(bo.getGuideGroupName());
        }
        return transition(record, "DISPATCH_GUIDE_GROUP",
            List.of(STATUS_TEAM_PENDING), STATUS_GUIDE_PENDING,
            safeOpinion(bo), "guide_test", "指导组待确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean guideConfirm(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "GUIDE_CONFIRM");
        return transition(record, "GUIDE_CONFIRM",
            List.of(STATUS_GUIDE_PENDING), STATUS_GUIDE_CONFIRMED,
            safeOpinion(bo), "director_test", "指导组确认无误");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean guideReject(Long recordId, DailyViolationActionBo bo) {
        requireText(bo == null ? null : bo.getReasonType(), "reason type is required");
        requireText(bo == null ? null : bo.getReasonDescription(), "reason description is required");
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "GUIDE_FEEDBACK");
        DailyViolationFeedback feedback = new DailyViolationFeedback();
        feedback.setFeedbackId(IdGeneratorUtil.nextLongId());
        feedback.setTenantId(tenantId());
        feedback.setRecordId(record.getRecordId());
        feedback.setBatchId(record.getBatchId());
        feedback.setReasonType(bo.getReasonType());
        feedback.setReasonDescription(bo.getReasonDescription());
        feedback.setFeedbackDeptId(LoginHelper.getDeptId());
        feedback.setFeedbackDeptNameSnapshot(LoginHelper.getDeptName());
        feedback.setFeedbackUserId(LoginHelper.getUserId());
        feedback.setFeedbackUserNameSnapshot(LoginHelper.getUsername());
        feedback.setAttachmentRefs(JsonUtils.toJsonString(bo.getAttachmentIds()));
        feedback.setFeedbackStatus("PENDING");
        feedback.setDelFlag(FLAG_NO);
        fillCreate(feedback);
        feedbackMapper.insert(feedback);
        return transition(record, "GUIDE_REJECT",
            List.of(STATUS_GUIDE_PENDING), STATUS_GUIDE_REJECTED,
            safeOpinion(bo), "director_test", "指导组反馈不属实");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnRecheck(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "RETURN_RECHECK");
        return transition(record, "RETURN_RECHECK",
            List.of(STATUS_GUIDE_CONFIRMED, STATUS_GUIDE_REJECTED), STATUS_RETURNED_DIRECTOR_RECHECK,
            safeOpinion(bo), "director_test", "主任复核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finalConfirm(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "FINAL_CONFIRM");
        requireText(bo == null ? null : bo.getFinalDecision(), "final decision is required");
        record.setFinalOpinion(bo.getFinalOpinion());
        record.setFinalDecision(bo.getFinalDecision());
        return transition(record, "FINAL_CONFIRM",
            List.of(STATUS_RETURNED_DIRECTOR_RECHECK), STATUS_FINAL_CONFIRMED,
            bo.getFinalOpinion(), "director_test", "主任最终确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archive(Long recordId, DailyViolationActionBo bo) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "ARCHIVE");
        long finalLogs = flowLogMapper.selectCount(Wrappers.lambdaQuery(DailyViolationFlowLog.class)
            .eq(DailyViolationFlowLog::getRecordId, recordId)
            .eq(DailyViolationFlowLog::getActionCode, "FINAL_CONFIRM"));
        if (finalLogs <= 0) {
            throw new ServiceException("complete flow log is required before archive");
        }
        boolean ok = transition(record, "ARCHIVE",
            List.of(STATUS_FINAL_CONFIRMED), STATUS_ARCHIVED,
            safeOpinion(bo), null, "已入结果库");
        insertResult(record, FLAG_YES, STATUS_ARCHIVED, null, null);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long recordId, DailyViolationActionBo bo) {
        requireText(bo == null ? null : bo.getCancelReason(), "cancel reason is required");
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "CANCEL");
        record.setCancelReason(bo.getCancelReason());
        boolean ok = transition(record, "CANCEL",
            List.of(STATUS_FINAL_CONFIRMED), STATUS_CANCELLED_EXCLUDED,
            bo.getCancelReason(), null, "撤销不计入");
        insertResult(record, FLAG_NO, STATUS_CANCELLED_EXCLUDED, bo.getCancelReason(), null);
        return ok;
    }

    @Override
    public TableDataInfo<DailyViolationResult> queryResults(DailyViolationResultBo bo, PageQuery pageQuery) {
        List<Long> recordIds = resolveRecordIdsForResultQuery(bo);
        if (recordIds != null && recordIds.isEmpty()) {
            return TableDataInfo.build(List.of(), pageQuery.build());
        }
        Page<DailyViolationResult> page = resultMapper.selectPage(pageQuery.build(), buildResultWrapper(bo, recordIds));
        return TableDataInfo.build(page);
    }

    @Override
    public List<DailyViolationResult> queryResultList(DailyViolationResultBo bo) {
        List<Long> recordIds = resolveRecordIdsForResultQuery(bo);
        if (recordIds != null && recordIds.isEmpty()) {
            return List.of();
        }
        return resultMapper.selectList(buildResultWrapper(bo, recordIds));
    }

    @Override
    public DailyViolationResult queryResult(Long resultId) {
        DailyViolationResult result = selectResultEntity(resultId);
        DailyViolationRecord record = queryRecord(result.getRecordId());
        checkRecordActionScope(record, "RESULT_VIEW");
        return result;
    }

    @Override
    public List<DailyViolationResult> queryResultVersions(Long resultId) {
        DailyViolationResult result = queryResult(resultId);
        return resultMapper.selectList(Wrappers.lambdaQuery(DailyViolationResult.class)
            .eq(DailyViolationResult::getRecordId, result.getRecordId())
            .orderByAsc(DailyViolationResult::getResultVersion));
    }

    @Override
    public Map<String, Object> compareResultVersions(Long resultId, Integer sourceVersion, Integer targetVersion) {
        DailyViolationResult result = queryResult(resultId);
        if (sourceVersion == null || targetVersion == null) {
            throw new ServiceException("sourceVersion and targetVersion are required");
        }
        DailyViolationResult source = selectResultVersion(result.getRecordId(), sourceVersion);
        DailyViolationResult target = selectResultVersion(result.getRecordId(), targetVersion);
        Map<String, Object> sourceSnapshot = parseSnapshot(source.getResultSnapshot());
        Map<String, Object> targetSnapshot = parseSnapshot(target.getResultSnapshot());
        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(sourceSnapshot.keySet());
        keys.addAll(targetSnapshot.keySet());
        List<Map<String, Object>> diffs = new ArrayList<>();
        for (String key : keys) {
            Object before = sourceSnapshot.get(key);
            Object after = targetSnapshot.get(key);
            if (!ObjectUtil.equals(before, after)) {
                Map<String, Object> diff = new LinkedHashMap<>();
                diff.put("field", key);
                diff.put("sourceValue", before);
                diff.put("targetValue", after);
                diffs.add(diff);
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recordId", result.getRecordId());
        payload.put("sourceVersion", sourceVersion);
        payload.put("targetVersion", targetVersion);
        payload.put("diffs", diffs);
        return payload;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyViolationResult correctResult(Long resultId, DailyViolationResultCorrectBo bo) {
        requireText(bo == null ? null : bo.getCorrectReason(), "correct reason is required");
        DailyViolationResult oldResult = queryResult(resultId);
        DailyViolationRecord record = queryRecord(oldResult.getRecordId());
        checkRecordActionScope(record, "RESULT_CORRECT");
        String included = StringUtils.defaultIfBlank(bo.getIncluded(), oldResult.getIncluded());
        String status = StringUtils.defaultIfBlank(bo.getResultStatus(), oldResult.getResultStatus());
        DailyViolationResult result = insertResult(record, included, status, bo.getCorrectReason(), oldResult.getResultId());
        Map<String, Object> changedFields = new LinkedHashMap<>();
        changedFields.put("correctedFromResultId", oldResult.getResultId());
        changedFields.put("sourceVersion", oldResult.getResultVersion());
        changedFields.put("targetVersion", result.getResultVersion());
        Map<String, Object> includedChange = new LinkedHashMap<>();
        includedChange.put("before", oldResult.getIncluded());
        includedChange.put("after", included);
        changedFields.put("included", includedChange);
        Map<String, Object> statusChange = new LinkedHashMap<>();
        statusChange.put("before", oldResult.getResultStatus());
        statusChange.put("after", status);
        changedFields.put("resultStatus", statusChange);
        writeFlowLog(record, "RESULT_CORRECT", record.getCurrentStatus(), record.getCurrentStatus(),
            bo.getCorrectReason(), null, JsonUtils.toJsonString(changedFields));
        return result;
    }

    @Override
    public void exportResults(DailyViolationResultBo bo, HttpServletResponse response) {
        List<DailyViolationResult> results = queryResultList(bo);
        Set<Long> recordIds = new LinkedHashSet<>();
        for (DailyViolationResult result : results) {
            if (result.getRecordId() != null) {
                recordIds.add(result.getRecordId());
            }
        }
        List<DailyViolationRecord> records = recordIds.isEmpty() ? List.of() : recordMapper.selectBatchIds(recordIds);
        Map<Long, DailyViolationRecord> recordMap = new LinkedHashMap<>();
        for (DailyViolationRecord record : records) {
            recordMap.put(record.getRecordId(), record);
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("结果库");
            writeTitleAndHeader(workbook, sheet, "每日LKJ音视频违标结果库导出");
            int index = 2;
            for (DailyViolationResult result : results) {
                DailyViolationRecord record = recordMap.get(result.getRecordId());
                if (record != null) {
                    Row row = sheet.createRow(index++);
                    writeRecordRow(row, record, index - 2);
                }
            }
            writeWorkbook(response, workbook, "daily_violation_results.xlsx");
        } catch (Exception e) {
            throw new ServiceException("result export failed: " + e.getMessage());
        }
    }

    @Override
    public List<BizFileBindVo> queryAttachments(Long recordId) {
        DailyViolationRecord record = queryRecord(recordId);
        BizFileBindBo bo = dailyAttachmentBo(recordId);
        List<BizFileBindVo> attachments = fileBindService.queryList(bo);
        writeFlowLog(record, "VIEW_ATTACHMENT", record.getCurrentStatus(), record.getCurrentStatus(),
            "view attachments", null, null);
        return attachments;
    }

    @Override
    public BizFileBindVo uploadAttachment(Long recordId, MultipartFile file, String businessAction, String attachmentType, String remark) {
        DailyViolationRecord record = queryRecord(recordId);
        checkRecordActionScope(record, "UPLOAD_ATTACHMENT");
        BizFileBindBo bo = dailyAttachmentBo(recordId);
        bo.setBusinessAction(StringUtils.defaultIfBlank(businessAction, "DAILY_VIOLATION_ATTACHMENT"));
        bo.setAttachmentType(StringUtils.defaultIfBlank(attachmentType, "EVIDENCE"));
        bo.setPermissionScope("DAILY_VIOLATION");
        bo.setRemark(remark);
        BizFileBindVo attachment = fileBindService.uploadAndBind(file, bo);
        writeFlowLog(record, "UPLOAD_ATTACHMENT", record.getCurrentStatus(), record.getCurrentStatus(),
            StringUtils.defaultIfBlank(remark, "upload attachment"), String.valueOf(attachment.getId()), null);
        return attachment;
    }

    @Override
    public void downloadAttachment(Long recordId, Long attachmentId, HttpServletResponse response) throws IOException {
        DailyViolationRecord record = queryRecord(recordId);
        BizFileBindVo attachment = fileBindService.queryById(attachmentId);
        if (attachment == null
            || !StringUtils.equals(BUSINESS_TYPE, attachment.getBusinessType())
            || !StringUtils.equals(String.valueOf(recordId), attachment.getBusinessId())) {
            throw new ServiceException("attachment does not belong to this daily violation record");
        }
        writeFlowLog(record, "DOWNLOAD_ATTACHMENT", record.getCurrentStatus(), record.getCurrentStatus(),
            "download attachment", String.valueOf(attachmentId), null);
        fileBindService.download(attachmentId, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyViolationImportBatch importExcel(MultipartFile file, Date reportDate, Integer businessYear, String sheetName) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("import file is required");
        }
        SysOssVo oss = ossService.upload(file);
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            DataFormatter formatter = new DataFormatter();
            SheetMatch sheetMatch = findDailyViolationSheet(workbook, formatter, sheetName);
            if (sheetMatch == null) {
                throw new ServiceException("daily violation sheet not found");
            }
            Date resolvedReportDate = reportDate == null ? parseReportDate(sheetMatch.title, businessYear) : reportDate;
            DailyViolationImportBatch batch = new DailyViolationImportBatch();
            batch.setImportBatchId(IdGeneratorUtil.nextLongId());
            batch.setTenantId(tenantId());
            batch.setReportDate(resolvedReportDate);
            batch.setOriginalFileId(oss == null ? null : oss.getOssId());
            batch.setOriginalFileName(file.getOriginalFilename());
            batch.setSheetName(sheetMatch.sheet.getSheetName());
            batch.setTitleText(sheetMatch.title);
            batch.setHeaderRowIndex(sheetMatch.headerRowIndex + 1);
            batch.setImportStatus("PARSED");
            batch.setImportedBy(LoginHelper.getUserId());
            batch.setImportedUserName(LoginHelper.getUsername());
            batch.setImportedTime(now());
            batch.setDelFlag(FLAG_NO);
            fillCreate(batch);
            importBatchMapper.insert(batch);

            Map<String, Integer> header = readHeader(sheetMatch.sheet.getRow(sheetMatch.headerRowIndex), formatter);
            for (int rowIndex = sheetMatch.headerRowIndex + 1; rowIndex <= sheetMatch.sheet.getLastRowNum(); rowIndex++) {
                Row row = sheetMatch.sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }
                DailyViolationImportRow importRow = parseImportRow(row, header, formatter, batch, rowIndex + 1, businessYear);
                validateImportRow(importRow, true);
                importRowMapper.insert(importRow);
            }
            refreshImportBatchCounts(batch.getImportBatchId());
            return queryImportBatch(batch.getImportBatchId());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("daily violation import failed: " + e.getMessage());
        }
    }

    @Override
    public DailyViolationImportBatch queryImportBatch(Long importBatchId) {
        DailyViolationImportBatch batch = importBatchMapper.selectById(importBatchId);
        if (batch == null) {
            throw new ServiceException("import batch not found");
        }
        checkImportBatchScope(batch);
        return batch;
    }

    @Override
    public TableDataInfo<DailyViolationImportRow> queryImportRows(Long importBatchId, String validationStatus, String confirmStatus, PageQuery pageQuery) {
        queryImportBatch(importBatchId);
        Page<DailyViolationImportRow> page = importRowMapper.selectPage(pageQuery.build(), Wrappers.lambdaQuery(DailyViolationImportRow.class)
            .eq(DailyViolationImportRow::getImportBatchId, importBatchId)
            .eq(StringUtils.isNotBlank(validationStatus), DailyViolationImportRow::getValidationStatus, validationStatus)
            .eq(StringUtils.isNotBlank(confirmStatus), DailyViolationImportRow::getConfirmStatus, confirmStatus)
            .orderByAsc(DailyViolationImportRow::getRowNo));
        return TableDataInfo.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyViolationImportRow updateImportRow(Long importBatchId, Long rowId, DailyViolationImportRowBo bo) {
        DailyViolationImportRow row = importRowMapper.selectById(rowId);
        if (row == null || !importBatchId.equals(row.getImportBatchId())) {
            throw new ServiceException("import row not found");
        }
        queryImportBatch(importBatchId);
        copyImportRowBo(row, bo);
        row.setUpdateBy(LoginHelper.getUserId());
        row.setUpdateTime(now());
        importErrorMapper.delete(Wrappers.lambdaQuery(DailyViolationImportError.class).eq(DailyViolationImportError::getRowId, rowId));
        validateImportRow(row, true);
        importRowMapper.updateById(row);
        refreshImportBatchCounts(importBatchId);
        return importRowMapper.selectById(rowId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyViolationImportBatch validateImportBatch(Long importBatchId) {
        queryImportBatch(importBatchId);
        List<DailyViolationImportRow> rows = importRowMapper.selectList(Wrappers.lambdaQuery(DailyViolationImportRow.class)
            .eq(DailyViolationImportRow::getImportBatchId, importBatchId)
            .isNull(DailyViolationImportRow::getGeneratedRecordId)
            .orderByAsc(DailyViolationImportRow::getRowNo));
        for (DailyViolationImportRow row : rows) {
            importErrorMapper.delete(Wrappers.lambdaQuery(DailyViolationImportError.class).eq(DailyViolationImportError::getRowId, row.getRowId()));
            validateImportRow(row, true);
            importRowMapper.updateById(row);
        }
        refreshImportBatchCounts(importBatchId);
        return queryImportBatch(importBatchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> submitImportRows(Long importBatchId, DailyViolationImportSubmitBo bo) {
        DailyViolationImportBatch importBatch = queryImportBatch(importBatchId);
        List<Long> rowIds = bo == null ? null : bo.getRowIds();
        LambdaQueryWrapper<DailyViolationImportRow> wrapper = Wrappers.lambdaQuery(DailyViolationImportRow.class)
            .eq(DailyViolationImportRow::getImportBatchId, importBatchId)
            .isNull(DailyViolationImportRow::getGeneratedRecordId)
            .orderByAsc(DailyViolationImportRow::getRowNo);
        if (rowIds != null && !rowIds.isEmpty()) {
            wrapper.in(DailyViolationImportRow::getRowId, rowIds);
        }
        List<DailyViolationImportRow> rows = importRowMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            throw new ServiceException("no rows selected");
        }
        DailyViolationBatch batch = createSubmitBatch(importBatch, rows.size());
        List<Long> recordIds = new ArrayList<>();
        for (DailyViolationImportRow row : rows) {
            if (INVALID.equals(row.getValidationStatus())) {
                throw new ServiceException("invalid row cannot be submitted: row " + row.getRowNo());
            }
            if (NEED_CONFIRM.equals(row.getValidationStatus()) && !CONFIRMED.equals(row.getConfirmStatus())) {
                throw new ServiceException("need confirm row cannot be submitted: row " + row.getRowNo());
            }
            String changedFieldsJson = checkImportCodeMetadataOverride(row);
            DailyViolationRecord record = recordFromImportRow(row, importBatch, batch);
            recordMapper.insert(record);
            row.setGeneratedRecordId(record.getRecordId());
            row.setUpdateBy(LoginHelper.getUserId());
            row.setUpdateTime(now());
            importRowMapper.updateById(row);
            writeFlowLog(record, "SUBMIT_TO_LEADER", STATUS_DRAFT, STATUS_ANALYST_SUBMITTED, "submit from excel import", null, changedFieldsJson);
            createTaskAndMessage(record, "leader_test", "班长待审核", "Excel import submitted");
            recordIds.add(record.getRecordId());
        }
        importBatch.setImportStatus("SUBMITTED");
        importBatch.setUpdateBy(LoginHelper.getUserId());
        importBatch.setUpdateTime(now());
        importBatchMapper.updateById(importBatch);
        refreshImportBatchCounts(importBatchId);
        batch.setSubmittedRows(recordIds.size());
        batchMapper.updateById(batch);
        return recordIds;
    }

    @Override
    public List<DailyViolationFlowLog> queryFlowLogs(Long recordId) {
        queryRecord(recordId);
        return flowLogMapper.selectList(Wrappers.lambdaQuery(DailyViolationFlowLog.class)
            .eq(DailyViolationFlowLog::getRecordId, recordId)
            .orderByAsc(DailyViolationFlowLog::getCreateTime));
    }

    @Override
    public List<DailyViolationFeedback> queryFeedbacks(Long recordId) {
        queryRecord(recordId);
        return feedbackMapper.selectList(Wrappers.lambdaQuery(DailyViolationFeedback.class)
            .eq(DailyViolationFeedback::getRecordId, recordId)
            .orderByDesc(DailyViolationFeedback::getCreateTime));
    }

    @Override
    public void exportRecords(DailyViolationRecordBo bo, HttpServletResponse response) {
        List<DailyViolationRecord> rows = queryRecordList(bo);
        String title = "货运车间违标问题登记簿";
        if (bo != null && bo.getReportDate() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(bo.getReportDate());
            title = title + "（" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日）";
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("LKJ音视频违标公示");
            writeTitleAndHeader(workbook, sheet, title);
            int index = 2;
            for (DailyViolationRecord record : rows) {
                Row row = sheet.createRow(index++);
                writeRecordRow(row, record, index - 2);
            }
            writeWorkbook(response, workbook, "daily_violation_export.xlsx");
        } catch (Exception e) {
            throw new ServiceException("export failed: " + e.getMessage());
        }
    }

    @Override
    public void exportErrorReport(Long importBatchId, HttpServletResponse response) {
        DailyViolationImportBatch batch = queryImportBatch(importBatchId);
        List<DailyViolationImportError> errors = importErrorMapper.selectList(Wrappers.lambdaQuery(DailyViolationImportError.class)
            .eq(DailyViolationImportError::getImportBatchId, importBatchId)
            .orderByAsc(DailyViolationImportError::getRowNo));
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("导入错误报告");
            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue("每日LKJ违标导入错误报告：" + batch.getOriginalFileName());
            Row header = sheet.createRow(1);
            String[] heads = {"Excel行号", "字段", "错误码", "错误信息", "原始值", "建议", "级别"};
            for (int i = 0; i < heads.length; i++) {
                header.createCell(i).setCellValue(heads[i]);
            }
            int index = 2;
            for (DailyViolationImportError error : errors) {
                Row row = sheet.createRow(index++);
                row.createCell(0).setCellValue(nvl(error.getRowNo()));
                row.createCell(1).setCellValue(nvl(error.getFieldName()));
                row.createCell(2).setCellValue(nvl(error.getErrorCode()));
                row.createCell(3).setCellValue(nvl(error.getErrorMessage()));
                row.createCell(4).setCellValue(nvl(error.getRawValue()));
                row.createCell(5).setCellValue(nvl(error.getSuggestion()));
                row.createCell(6).setCellValue(nvl(error.getSeverity()));
            }
            writeWorkbook(response, workbook, "daily_violation_import_errors.xlsx");
        } catch (Exception e) {
            throw new ServiceException("error report export failed: " + e.getMessage());
        }
    }

    private LambdaQueryWrapper<DailyViolationRecord> buildRecordWrapper(DailyViolationRecordBo bo) {
        DailyViolationRecordBo query = bo == null ? new DailyViolationRecordBo() : bo;
        LambdaQueryWrapper<DailyViolationRecord> wrapper = Wrappers.lambdaQuery(DailyViolationRecord.class)
            .eq(query.getBatchId() != null, DailyViolationRecord::getBatchId, query.getBatchId())
            .eq(query.getReportDate() != null, DailyViolationRecord::getReportDate, query.getReportDate())
            .ge(query.getViolationDateStart() != null, DailyViolationRecord::getViolationDate, query.getViolationDateStart())
            .le(query.getViolationDateEnd() != null, DailyViolationRecord::getViolationDate, query.getViolationDateEnd())
            .eq(query.getViolationDate() != null, DailyViolationRecord::getViolationDate, query.getViolationDate())
            .eq(query.getResponsibleDeptId() != null, DailyViolationRecord::getResponsibleDeptId, query.getResponsibleDeptId())
            .like(StringUtils.isNotBlank(query.getResponsibleDeptName()), DailyViolationRecord::getResponsibleDeptNameSnapshot, query.getResponsibleDeptName())
            .eq(StringUtils.isNotBlank(query.getEmployeeNo()), DailyViolationRecord::getEmployeeNo, query.getEmployeeNo())
            .like(StringUtils.isNotBlank(query.getEmployeeName()), DailyViolationRecord::getEmployeeNameSnapshot, query.getEmployeeName())
            .eq(StringUtils.isNotBlank(query.getViolationCode()), DailyViolationRecord::getViolationCode, query.getViolationCode())
            .eq(StringUtils.isNotBlank(query.getCurrentStatus()), DailyViolationRecord::getCurrentStatus, query.getCurrentStatus())
            .eq(StringUtils.isNotBlank(query.getValidationStatus()), DailyViolationRecord::getValidationStatus, query.getValidationStatus());
        appendRecordDataScope(wrapper);
        return wrapper.orderByDesc(DailyViolationRecord::getCreateTime);
    }

    private LambdaQueryWrapper<DailyViolationResult> buildResultWrapper(DailyViolationResultBo bo, List<Long> recordIds) {
        DailyViolationResultBo query = bo == null ? new DailyViolationResultBo() : bo;
        return Wrappers.lambdaQuery(DailyViolationResult.class)
            .eq(query.getResultId() != null, DailyViolationResult::getResultId, query.getResultId())
            .eq(query.getRecordId() != null, DailyViolationResult::getRecordId, query.getRecordId())
            .eq(query.getBatchId() != null, DailyViolationResult::getBatchId, query.getBatchId())
            .eq(query.getResultVersion() != null, DailyViolationResult::getResultVersion, query.getResultVersion())
            .eq(StringUtils.isNotBlank(query.getResultStatus()), DailyViolationResult::getResultStatus, query.getResultStatus())
            .eq(StringUtils.isNotBlank(query.getIncluded()), DailyViolationResult::getIncluded, query.getIncluded())
            .ge(query.getArchivedTimeStart() != null, DailyViolationResult::getArchivedTime, query.getArchivedTimeStart())
            .le(query.getArchivedTimeEnd() != null, DailyViolationResult::getArchivedTime, query.getArchivedTimeEnd())
            .in(recordIds != null && !recordIds.isEmpty(), DailyViolationResult::getRecordId, recordIds)
            .orderByDesc(DailyViolationResult::getArchivedTime);
    }

    private List<Long> resolveRecordIdsForResultQuery(DailyViolationResultBo bo) {
        return recordMapper.selectList(buildRecordWrapper(bo)).stream()
            .map(DailyViolationRecord::getRecordId)
            .toList();
    }

    private boolean hasResultRecordCriteria(DailyViolationResultBo bo) {
        return bo != null && (
            bo.getReportDate() != null
                || bo.getViolationDateStart() != null
                || bo.getViolationDateEnd() != null
                || bo.getViolationDate() != null
                || bo.getResponsibleDeptId() != null
                || StringUtils.isNotBlank(bo.getResponsibleDeptName())
                || StringUtils.isNotBlank(bo.getEmployeeNo())
                || StringUtils.isNotBlank(bo.getEmployeeName())
                || StringUtils.isNotBlank(bo.getViolationCode())
                || StringUtils.isNotBlank(bo.getCurrentStatus())
                || StringUtils.isNotBlank(bo.getValidationStatus())
        );
    }

    private BizFileBindBo dailyAttachmentBo(Long recordId) {
        BizFileBindBo bo = new BizFileBindBo();
        bo.setBusinessType(BUSINESS_TYPE);
        bo.setBusinessId(String.valueOf(recordId));
        return bo;
    }

    private void appendRecordDataScope(LambdaQueryWrapper<DailyViolationRecord> wrapper) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Set<String> roles = currentRoleKeys();
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        String deptName = LoginHelper.getDeptName();
        wrapper.and(scope -> {
            boolean appended = false;
            if (roles.contains(ROLE_DIRECTOR)) {
                scope.isNotNull(DailyViolationRecord::getRecordId);
                appended = true;
            }
            if (roles.contains(ROLE_ANALYST)) {
                if (appended) {
                    scope.or();
                }
                scope.eq(DailyViolationRecord::getCreateBy, userId);
                appended = true;
            }
            if (roles.contains(ROLE_LEADER)) {
                if (appended) {
                    scope.or();
                }
                if (deptId != null) {
                    scope.eq(DailyViolationRecord::getCreateDept, deptId);
                } else {
                    scope.isNotNull(DailyViolationRecord::getRecordId);
                }
                scope.in(DailyViolationRecord::getCurrentStatus, STATUS_ANALYST_SUBMITTED, STATUS_LEADER_PENDING,
                    STATUS_DIRECTOR_PENDING, STATUS_DIRECTOR_APPROVED_PENDING_DISPATCH);
                appended = true;
            }
            if (roles.contains(ROLE_WORKSHOP)) {
                appended = appendOrgScope(scope, appended, DailyViolationRecord::getWorkshopId,
                    DailyViolationRecord::getWorkshopName, deptId, deptName);
            }
            if (roles.contains(ROLE_TEAM)) {
                appended = appendOrgScope(scope, appended, DailyViolationRecord::getTeamId,
                    DailyViolationRecord::getTeamName, deptId, deptName);
            }
            if (roles.contains(ROLE_GUIDE)) {
                appended = appendOrgScope(scope, appended, DailyViolationRecord::getGuideGroupId,
                    DailyViolationRecord::getGuideGroupName, deptId, deptName);
            }
            if (!appended) {
                scope.eq(DailyViolationRecord::getRecordId, -1L);
            }
        });
    }

    private boolean appendOrgScope(LambdaQueryWrapper<DailyViolationRecord> scope, boolean appended,
                                   com.baomidou.mybatisplus.core.toolkit.support.SFunction<DailyViolationRecord, Long> idColumn,
                                   com.baomidou.mybatisplus.core.toolkit.support.SFunction<DailyViolationRecord, String> nameColumn,
                                   Long deptId, String deptName) {
        if (deptId != null) {
            if (appended) {
                scope.or();
            }
            scope.eq(idColumn, deptId);
            appended = true;
        }
        if (StringUtils.isNotBlank(deptName)) {
            if (appended) {
                scope.or();
            }
            scope.eq(nameColumn, deptName);
            appended = true;
        }
        return appended;
    }

    private void checkRecordReadable(DailyViolationRecord record) {
        if (!canReadRecord(record)) {
            forbidden("no permission to access this daily violation record");
        }
    }

    private boolean canReadRecord(DailyViolationRecord record) {
        if (record == null) {
            return false;
        }
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        Set<String> roles = currentRoleKeys();
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        String deptName = LoginHelper.getDeptName();
        if (roles.contains(ROLE_DIRECTOR)) {
            return true;
        }
        if (roles.contains(ROLE_ANALYST) && ObjectUtil.equals(record.getCreateBy(), userId)) {
            return true;
        }
        if (roles.contains(ROLE_LEADER)
            && (ObjectUtil.equals(record.getCreateDept(), deptId) || ObjectUtil.equals(record.getResponsibleDeptId(), deptId))) {
            return true;
        }
        return (roles.contains(ROLE_WORKSHOP) && matchOrg(record.getWorkshopId(), record.getWorkshopName(), deptId, deptName))
            || (roles.contains(ROLE_TEAM) && matchOrg(record.getTeamId(), record.getTeamName(), deptId, deptName))
            || (roles.contains(ROLE_GUIDE) && matchOrg(record.getGuideGroupId(), record.getGuideGroupName(), deptId, deptName));
    }

    private void checkRecordActionScope(DailyViolationRecord record, String action) {
        checkRecordReadable(record);
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Set<String> roles = currentRoleKeys();
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        String deptName = LoginHelper.getDeptName();
        boolean allowed = switch (action) {
            case "EDIT", "SUBMIT" -> roles.contains(ROLE_ANALYST) && ObjectUtil.equals(record.getCreateBy(), userId);
            case "LEADER_AUDIT" -> roles.contains(ROLE_LEADER)
                && (ObjectUtil.equals(record.getCreateDept(), deptId) || ObjectUtil.equals(record.getResponsibleDeptId(), deptId));
            case "DIRECTOR_AUDIT", "DISPATCH_WORKSHOP", "RETURN_RECHECK", "FINAL_CONFIRM", "ARCHIVE", "CANCEL" ->
                roles.contains(ROLE_DIRECTOR);
            case "DISPATCH_TEAM" -> roles.contains(ROLE_WORKSHOP)
                && matchOrg(record.getWorkshopId(), record.getWorkshopName(), deptId, deptName);
            case "DISPATCH_GUIDE_GROUP" -> roles.contains(ROLE_TEAM)
                && matchOrg(record.getTeamId(), record.getTeamName(), deptId, deptName);
            case "GUIDE_CONFIRM", "GUIDE_FEEDBACK", "UPLOAD_ATTACHMENT" -> roles.contains(ROLE_GUIDE)
                && matchOrg(record.getGuideGroupId(), record.getGuideGroupName(), deptId, deptName);
            case "RESULT_VIEW" -> canReadRecord(record);
            case "RESULT_CORRECT" -> StpUtil.hasPermission("violation:daily:result:correct") && roles.contains(ROLE_DIRECTOR);
            default -> false;
        };
        if (!allowed) {
            forbidden("no permission to operate this daily violation record");
        }
    }

    private boolean matchOrg(Long targetDeptId, String targetDeptName, Long currentDeptId, String currentDeptName) {
        return (targetDeptId != null && ObjectUtil.equals(targetDeptId, currentDeptId))
            || (StringUtils.isNotBlank(targetDeptName) && StringUtils.equals(targetDeptName, currentDeptName));
    }

    private void checkImportBatchScope(DailyViolationImportBatch batch) {
        if (LoginHelper.isSuperAdmin() || currentRoleKeys().contains(ROLE_DIRECTOR)) {
            return;
        }
        if (!ObjectUtil.equals(batch.getImportedBy(), LoginHelper.getUserId())
            && !ObjectUtil.equals(batch.getCreateBy(), LoginHelper.getUserId())) {
            forbidden("no permission to access this import batch");
        }
    }

    private void forbidden(String message) {
        throw new ServiceException(message, HttpStatus.FORBIDDEN);
    }

    private String normalizeManualRecord(DailyViolationRecord record) {
        if (record.getReportDate() == null) {
            throw new ServiceException("reportDate is required");
        }
        if (StringUtils.isBlank(record.getViolationCode())) {
            throw new ServiceException("violationCode is required");
        }
        if (StringUtils.isBlank(record.getProposedAssessmentContent())) {
            throw new ServiceException("proposedAssessmentContent is required");
        }
        BaseViolationCode code = requireViolationCode(record.getViolationCode());
        String changedFieldsJson = checkManualCodeMetadataOverride(record, code);
        applyCodeSnapshot(record, code);
        if (StringUtils.isNotBlank(record.getEmployeeNo())) {
            BasePersonnel personnel = requirePersonnel(record.getEmployeeNo());
            record.setResponsiblePersonId(personnel.getId());
            record.setEmployeeNameSnapshot(StringUtils.blankToDefault(record.getEmployeeNameSnapshot(), personnel.getPersonName()));
            record.setResponsibleDeptId(record.getResponsibleDeptId() == null ? personnel.getDeptId() : record.getResponsibleDeptId());
            record.setResponsibleDeptNameSnapshot(StringUtils.blankToDefault(record.getResponsibleDeptNameSnapshot(), personnel.getDeptName()));
            record.setWorkshopName(StringUtils.blankToDefault(record.getWorkshopName(), personnel.getWorkshop()));
            record.setTeamName(StringUtils.blankToDefault(record.getTeamName(), personnel.getTeamName()));
            record.setGuideGroupName(StringUtils.blankToDefault(record.getGuideGroupName(), personnel.getGuideGroup()));
            record.setPersonnelSnapshot(JsonUtils.toJsonString(snapshot(personnel)));
        }
        record.setOrgSnapshot(JsonUtils.toJsonString(snapshotOrg(record)));
        record.setValidationStatus(VALID);
        record.setValidationMessage("");
        return changedFieldsJson;
    }

    private String checkManualCodeMetadataOverride(DailyViolationRecord record, BaseViolationCode code) {
        Map<String, Object> changes = new LinkedHashMap<>();
        collectCodeMetadataMismatch(changes, "violationNatureSnapshot", record.getViolationNatureSnapshot(), code.getNature());
        collectCodeMetadataMismatch(changes, "violationCategorySnapshot", record.getViolationCategorySnapshot(), code.getCategory());
        collectCodeMetadataMismatch(changes, "violationTypeSnapshot", record.getViolationTypeSnapshot(), code.getViolationType());
        return handleCodeMetadataMismatch(changes);
    }

    private String checkImportCodeMetadataOverride(DailyViolationImportRow row) {
        BaseViolationCode code = requireViolationCode(row.getViolationCode());
        Map<String, Object> changes = new LinkedHashMap<>();
        collectCodeMetadataMismatch(changes, "violationNature", row.getViolationNature(), code.getNature());
        collectCodeMetadataMismatch(changes, "violationCategory", row.getViolationCategory(), code.getCategory());
        collectCodeMetadataMismatch(changes, "violationType", row.getViolationType(), code.getViolationType());
        return handleCodeMetadataMismatch(changes);
    }

    private void collectCodeMetadataMismatch(Map<String, Object> changes, String field, String supplied, String expected) {
        if (StringUtils.isNotBlank(supplied) && !StringUtils.equals(supplied, expected)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplied", supplied);
            item.put("canonical", expected);
            item.put("authorizedOverride", true);
            changes.put(field, item);
        }
    }

    private String handleCodeMetadataMismatch(Map<String, Object> changes) {
        if (changes.isEmpty()) {
            return null;
        }
        if (!hasPermission(PERMISSION_OVERRIDE)) {
            forbidden("violation code metadata mismatch requires " + PERMISSION_OVERRIDE);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "CODE_METADATA_OVERRIDE");
        payload.put("permission", PERMISSION_OVERRIDE);
        payload.put("fields", changes);
        return JsonUtils.toJsonString(payload);
    }

    private boolean hasPermission(String permission) {
        return LoginHelper.isSuperAdmin() || StpUtil.hasPermission(permission);
    }

    private void copyBo(DailyViolationRecord record, DailyViolationRecordBo bo) {
        record.setReportDate(bo.getReportDate());
        record.setViolationDate(bo.getViolationDate());
        record.setViolationTime(bo.getViolationTime());
        record.setSequenceNo(bo.getSequenceNo());
        record.setViolationCode(bo.getViolationCode());
        record.setViolationNatureSnapshot(bo.getViolationNatureSnapshot());
        record.setViolationCategorySnapshot(bo.getViolationCategorySnapshot());
        record.setViolationTypeSnapshot(bo.getViolationTypeSnapshot());
        record.setProposedAssessmentContent(bo.getProposedAssessmentContent());
        record.setResponsibleDeptId(bo.getResponsibleDeptId());
        record.setResponsibleDeptNameSnapshot(bo.getResponsibleDeptName());
        record.setEmployeeNo(bo.getEmployeeNo());
        record.setEmployeeNameSnapshot(bo.getEmployeeName());
        record.setResponsiblePersonId(bo.getResponsiblePersonId());
        record.setLocomotive(bo.getLocomotive());
        record.setTrainNo(bo.getTrainNo());
        record.setLocation(bo.getLocation());
        record.setTimeSegment(bo.getTimeSegment());
        record.setTicketNo(bo.getTicketNo());
        record.setGuideDriver(bo.getGuideDriver());
        record.setGuideGroup(bo.getGuideGroup());
        record.setPartyMemberFlag(bo.getPartyMemberFlag());
        record.setAbcdAssignment(bo.getAbcdAssignment());
        record.setHandlingAssessment(bo.getHandlingAssessment());
        record.setIssuingDept(bo.getIssuingDept());
        record.setWorkshopId(bo.getWorkshopId());
        record.setWorkshopName(bo.getWorkshopName());
        record.setTeamId(bo.getTeamId());
        record.setTeamName(bo.getTeamName());
        record.setGuideGroupId(bo.getGuideGroupId());
        record.setGuideGroupName(bo.getGuideGroupName());
        record.setRemark(bo.getRemark());
    }

    private boolean transition(DailyViolationRecord record, String action, List<String> allowedFrom, String target,
                               String opinion, String nextRole, String nextTitle) {
        String before = record.getCurrentStatus();
        if (!allowedFrom.contains(before)) {
            throw new ServiceException("invalid current status for action " + action + ": " + before);
        }
        closePendingTasks(record, action);
        record.setCurrentStatus(target);
        record.setVersion(record.getVersion() == null ? 1 : record.getVersion() + 1);
        record.setUpdateBy(LoginHelper.getUserId());
        record.setUpdateTime(now());
        boolean ok = recordMapper.updateById(record) > 0;
        writeFlowLog(record, action, before, target, opinion, null, null);
        if (StringUtils.isNotBlank(nextRole)) {
            createTaskAndMessage(record, nextRole, nextTitle, action);
        }
        return ok;
    }

    private void closePendingTasks(DailyViolationRecord record, String action) {
        List<BizTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(BizTask.class)
            .eq(BizTask::getBusinessType, BUSINESS_TYPE)
            .eq(BizTask::getBusinessId, String.valueOf(record.getRecordId()))
            .eq(BizTask::getTaskStatus, "PENDING"));
        for (BizTask task : tasks) {
            task.setTaskStatus("DONE");
            task.setFinishTime(now());
            task.setFinishUserId(LoginHelper.getUserId());
            task.setFinishUserName(LoginHelper.getUsername());
            task.setFinishComment(action);
            taskMapper.updateById(task);
        }
    }

    private void createTaskAndMessage(DailyViolationRecord record, String roleKey, String title, String action) {
        BizTask task = new BizTask();
        task.setId(IdGeneratorUtil.nextLongId());
        task.setTenantId(tenantId());
        task.setBusinessType(BUSINESS_TYPE);
        task.setBusinessId(String.valueOf(record.getRecordId()));
        task.setBatchId(record.getBatchId() == null ? null : String.valueOf(record.getBatchId()));
        task.setTaskTitle(title);
        task.setTaskContent(record.getViolationCode() + " " + StringUtils.blankToDefault(record.getEmployeeNameSnapshot(), ""));
        task.setTaskStatus("PENDING");
        task.setCurrentNode(record.getCurrentStatus());
        task.setPriority("NORMAL");
        task.setBusinessUrl("/violation/daily?recordId=" + record.getRecordId());
        task.setReceiverRoleKey(roleKey);
        task.setSenderUserId(LoginHelper.getUserId());
        task.setSenderUserName(LoginHelper.getUsername());
        task.setSenderDeptId(LoginHelper.getDeptId());
        task.setSenderDeptName(LoginHelper.getDeptName());
        task.setDelFlag(FLAG_NO);
        fillCreate(task);
        taskMapper.insert(task);

        BizMessage message = new BizMessage();
        message.setId(IdGeneratorUtil.nextLongId());
        message.setTenantId(tenantId());
        message.setBusinessType(BUSINESS_TYPE);
        message.setBusinessId(String.valueOf(record.getRecordId()));
        message.setBatchId(record.getBatchId() == null ? null : String.valueOf(record.getBatchId()));
        message.setMessageType("FLOW");
        message.setMessageTitle(title);
        message.setMessageContent(action + ": " + record.getViolationCode());
        message.setSourceAction(action);
        message.setBusinessUrl("/violation/daily?recordId=" + record.getRecordId());
        message.setBusinessPayload(JsonUtils.toJsonString(snapshot(record)));
        message.setSenderUserId(LoginHelper.getUserId());
        message.setSenderUserName(LoginHelper.getUsername());
        message.setSenderDeptId(LoginHelper.getDeptId());
        message.setSenderDeptName(LoginHelper.getDeptName());
        message.setReceiverRoleKey(roleKey);
        message.setReadFlag(FLAG_NO);
        message.setArchiveFlag(FLAG_NO);
        message.setDelFlag(FLAG_NO);
        fillCreate(message);
        messageMapper.insert(message);
    }

    private DailyViolationImportRow parseImportRow(Row row, Map<String, Integer> header, DataFormatter formatter,
                                                   DailyViolationImportBatch batch, int rowNo, Integer businessYear) {
        DailyViolationImportRow importRow = new DailyViolationImportRow();
        importRow.setRowId(IdGeneratorUtil.nextLongId());
        importRow.setTenantId(tenantId());
        importRow.setImportBatchId(batch.getImportBatchId());
        importRow.setRowNo(rowNo);
        importRow.setRawJson(JsonUtils.toJsonString(rawRow(row, header, formatter)));
        importRow.setSequenceNo(value(row, header, formatter, "序号"));
        importRow.setViolationCode(value(row, header, formatter, "违标编码"));
        importRow.setProposedAssessmentContent(value(row, header, formatter, "拟考核内容"));
        importRow.setViolationNature(value(row, header, formatter, "性质"));
        importRow.setViolationCategory(value(row, header, formatter, "类别"));
        importRow.setViolationType(value(row, header, formatter, "类型"));
        importRow.setResponsibleDeptName(value(row, header, formatter, "责任部门"));
        importRow.setResponsiblePersonName(value(row, header, formatter, "责任人"));
        importRow.setHandlingAssessment(value(row, header, formatter, "处理考核"));
        importRow.setIssuingDept(value(row, header, formatter, "下票部门"));
        importRow.setEmployeeNo(value(row, header, formatter, "工号"));
        importRow.setTimeSegment(value(row, header, formatter, "时段"));
        importRow.setPartyMemberFlag(value(row, header, formatter, "党员"));
        importRow.setTicketNo(value(row, header, formatter, "票号"));
        importRow.setGuideDriver(value(row, header, formatter, "指导司机"));
        importRow.setFalseReason(value(row, header, formatter, "情况不属实原因"));
        importRow.setLocation(value(row, header, formatter, "地点"));
        importRow.setGuideGroup(value(row, header, formatter, "指导组"));
        importRow.setAbcdAssignment(value(row, header, formatter, "ABCD配班"));
        enrichParsedText(importRow, businessYear == null ? currentYear() : businessYear);
        importRow.setConfirmStatus(UNCONFIRMED);
        importRow.setDelFlag(FLAG_NO);
        fillCreate(importRow);
        return importRow;
    }

    private void validateImportRow(DailyViolationImportRow row, boolean writeError) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        BaseViolationCode code = null;
        if (StringUtils.isBlank(row.getViolationCode())) {
            errors.add("violation code is blank");
            addImportError(row, "violationCode", "CODE_BLANK", "违标编码为空", row.getViolationCode(), "补充违标编码", "ERROR", writeError);
        } else {
            code = selectViolationCode(row.getViolationCode());
            if (code == null) {
                errors.add("violation code not found");
                addImportError(row, "violationCode", "CODE_NOT_FOUND", "违标编码不存在", row.getViolationCode(), "先维护基础违标编码", "ERROR", writeError);
            }
        }
        if (StringUtils.isBlank(row.getProposedAssessmentContent())) {
            errors.add("content is blank");
            addImportError(row, "proposedAssessmentContent", "CONTENT_BLANK", "拟考核内容为空", "", "补充拟考核内容", "ERROR", writeError);
        }
        if (code != null && (!StringUtils.equals(code.getNature(), row.getViolationNature())
            || !StringUtils.equals(code.getCategory(), row.getViolationCategory())
            || !StringUtils.equals(code.getViolationType(), row.getViolationType()))) {
            warnings.add("code metadata mismatch");
            addImportError(row, "violationCode", "CODE_META_MISMATCH", "编码与性质/类别/类型不匹配", row.getViolationCode(), "确认后按基础编码覆盖", "WARNING", writeError);
        }
        if (StringUtils.isNotBlank(row.getEmployeeNo())) {
            BasePersonnel personnel = selectPersonnel(row.getEmployeeNo());
            if (personnel == null) {
                errors.add("employee not found");
                addImportError(row, "employeeNo", "EMPLOYEE_NOT_FOUND", "工号不存在", row.getEmployeeNo(), "先导入或维护人员基础数据", "ERROR", writeError);
            } else {
                if (StringUtils.isBlank(row.getResponsiblePersonName())) {
                    row.setResponsiblePersonName(personnel.getPersonName());
                } else if (!StringUtils.equals(normalizeName(personnel), normalizeText(row.getResponsiblePersonName()))) {
                    warnings.add("employee name mismatch");
                    addImportError(row, "responsiblePersonName", "EMPLOYEE_NAME_MISMATCH", "工号与姓名不一致", row.getResponsiblePersonName(), "人工确认责任人", "WARNING", writeError);
                }
                if (StringUtils.isBlank(row.getResponsibleDeptName())) {
                    row.setResponsibleDeptName(personnel.getDeptName());
                }
            }
        } else if (StringUtils.isNotBlank(row.getCandidatePersonNames())) {
            warnings.add("candidate person requires confirm");
            addImportError(row, "employeeNo", "CANDIDATE_NEEDS_CONFIRM", "文本中解析到候选责任人但工号为空", row.getCandidatePersonNames(), "在预览中确认工号和责任人", "WARNING", writeError);
        }
        if (row.getParsedViolationDate() == null) {
            warnings.add("violation date not parsed");
            addImportError(row, "parsedViolationDate", "DATE_NOT_PARSED", "违章发生日期无法解析", "", "人工确认违章发生日期", "WARNING", writeError);
        }
        if (row.getParsedViolationDate() != null && StringUtils.isNotBlank(row.getViolationCode()) && StringUtils.isNotBlank(row.getEmployeeNo())) {
            long duplicate = recordMapper.selectCount(Wrappers.lambdaQuery(DailyViolationRecord.class)
                .eq(DailyViolationRecord::getViolationCode, row.getViolationCode())
                .eq(DailyViolationRecord::getEmployeeNo, row.getEmployeeNo())
                .eq(DailyViolationRecord::getViolationDate, row.getParsedViolationDate()));
            if (duplicate > 0) {
                warnings.add("possible duplicate");
                addImportError(row, "duplicate", "POSSIBLE_DUPLICATE", "疑似重复导入", row.getViolationCode(), "人工核对，不自动删除", "WARNING", writeError);
            }
        }
        if (!errors.isEmpty()) {
            row.setValidationStatus(INVALID);
            row.setValidationMessage(String.join("; ", errors));
        } else if (!warnings.isEmpty()) {
            row.setValidationStatus(NEED_CONFIRM);
            row.setValidationMessage(String.join("; ", warnings));
        } else {
            row.setValidationStatus(VALID);
            row.setValidationMessage("");
        }
    }

    private DailyViolationRecord recordFromImportRow(DailyViolationImportRow row, DailyViolationImportBatch importBatch, DailyViolationBatch batch) {
        DailyViolationRecord record = new DailyViolationRecord();
        record.setRecordId(IdGeneratorUtil.nextLongId());
        record.setTenantId(tenantId());
        record.setBatchId(batch.getBatchId());
        record.setReportDate(importBatch.getReportDate());
        record.setViolationDate(row.getParsedViolationDate());
        record.setViolationTime(row.getParsedViolationTime());
        record.setSequenceNo(row.getSequenceNo());
        record.setViolationCode(row.getViolationCode());
        BaseViolationCode code = requireViolationCode(row.getViolationCode());
        record.setViolationNatureSnapshot(code.getNature());
        record.setViolationCategorySnapshot(code.getCategory());
        record.setViolationTypeSnapshot(code.getViolationType());
        record.setProposedAssessmentContent(row.getProposedAssessmentContent());
        record.setResponsibleDeptNameSnapshot(row.getResponsibleDeptName());
        record.setEmployeeNo(row.getEmployeeNo());
        record.setEmployeeNameSnapshot(row.getResponsiblePersonName());
        record.setLocomotive(row.getParsedLocomotive());
        record.setTrainNo(row.getParsedTrainNo());
        record.setLocation(row.getLocation());
        record.setTimeSegment(row.getTimeSegment());
        record.setTicketNo(row.getTicketNo());
        record.setGuideDriver(row.getGuideDriver());
        record.setGuideGroup(row.getGuideGroup());
        record.setPartyMemberFlag(row.getPartyMemberFlag());
        record.setAbcdAssignment(row.getAbcdAssignment());
        record.setHandlingAssessment(row.getHandlingAssessment());
        record.setIssuingDept(row.getIssuingDept());
        record.setCurrentStatus(STATUS_ANALYST_SUBMITTED);
        record.setVersion(1);
        record.setSourceType("EXCEL_IMPORT");
        record.setImportBatchId(importBatch.getImportBatchId());
        record.setImportRowId(row.getRowId());
        record.setPreviewConfirmed(FLAG_YES);
        record.setValidationStatus(row.getValidationStatus());
        record.setValidationMessage(row.getValidationMessage());
        record.setViolationCodeSnapshot(JsonUtils.toJsonString(snapshot(code)));
        BasePersonnel personnel = StringUtils.isBlank(row.getEmployeeNo()) ? null : selectPersonnel(row.getEmployeeNo());
        if (personnel != null) {
            record.setResponsiblePersonId(personnel.getId());
            record.setResponsibleDeptId(personnel.getDeptId());
            record.setResponsibleDeptNameSnapshot(StringUtils.blankToDefault(record.getResponsibleDeptNameSnapshot(), personnel.getDeptName()));
            record.setWorkshopName(personnel.getWorkshop());
            record.setTeamName(personnel.getTeamName());
            record.setGuideGroupName(personnel.getGuideGroup());
            record.setPersonnelSnapshot(JsonUtils.toJsonString(snapshot(personnel)));
        }
        record.setOrgSnapshot(JsonUtils.toJsonString(snapshotOrg(record)));
        record.setDelFlag(FLAG_NO);
        fillCreate(record);
        return record;
    }

    private void enrichParsedText(DailyViolationImportRow row, int year) {
        String content = row.getProposedAssessmentContent();
        if (StringUtils.isBlank(content)) {
            return;
        }
        Matcher dateMatcher = TITLE_DATE.matcher(content);
        if (dateMatcher.find()) {
            row.setParsedViolationDate(dateFromMonthDay(year, Integer.parseInt(dateMatcher.group(1)), Integer.parseInt(dateMatcher.group(2))));
        }
        row.setParsedViolationTime(firstMatch(TIME_TEXT, content));
        row.setParsedLocomotive(firstMatch(LOCOMOTIVE_TEXT, content));
        Matcher trainMatcher = TRAIN_TEXT.matcher(content);
        if (trainMatcher.find()) {
            row.setParsedTrainNo(trainMatcher.group(1) + "次");
        }
        Matcher personMatcher = PERSON_TEXT.matcher(content);
        List<String> names = new ArrayList<>();
        while (personMatcher.find()) {
            names.add(personMatcher.group(1));
        }
        row.setCandidatePersonNames(String.join(",", names));
    }

    private void copyImportRowBo(DailyViolationImportRow row, DailyViolationImportRowBo bo) {
        if (bo == null) {
            return;
        }
        if (bo.getEmployeeNo() != null) {
            row.setEmployeeNo(bo.getEmployeeNo());
        }
        if (bo.getResponsiblePersonName() != null) {
            row.setResponsiblePersonName(bo.getResponsiblePersonName());
        }
        if (bo.getResponsibleDeptName() != null) {
            row.setResponsibleDeptName(bo.getResponsibleDeptName());
        }
        if (bo.getViolationCode() != null) {
            row.setViolationCode(bo.getViolationCode());
        }
        if (bo.getViolationNature() != null) {
            row.setViolationNature(bo.getViolationNature());
        }
        if (bo.getViolationCategory() != null) {
            row.setViolationCategory(bo.getViolationCategory());
        }
        if (bo.getViolationType() != null) {
            row.setViolationType(bo.getViolationType());
        }
        if (bo.getParsedViolationDate() != null) {
            row.setParsedViolationDate(bo.getParsedViolationDate());
        }
        if (bo.getParsedViolationTime() != null) {
            row.setParsedViolationTime(bo.getParsedViolationTime());
        }
        if (bo.getParsedLocomotive() != null) {
            row.setParsedLocomotive(bo.getParsedLocomotive());
        }
        if (bo.getParsedTrainNo() != null) {
            row.setParsedTrainNo(bo.getParsedTrainNo());
        }
        if (bo.getLocation() != null) {
            row.setLocation(bo.getLocation());
        }
        if (bo.getConfirmStatus() != null) {
            row.setConfirmStatus(bo.getConfirmStatus());
        }
        if (bo.getConfirmRemark() != null) {
            row.setConfirmRemark(bo.getConfirmRemark());
        }
    }

    private void addImportError(DailyViolationImportRow row, String field, String code, String message, String raw,
                                String suggestion, String severity, boolean writeError) {
        if (!writeError) {
            return;
        }
        DailyViolationImportError error = new DailyViolationImportError();
        error.setErrorId(IdGeneratorUtil.nextLongId());
        error.setTenantId(tenantId());
        error.setImportBatchId(row.getImportBatchId());
        error.setRowId(row.getRowId());
        error.setRowNo(row.getRowNo());
        error.setFieldName(field);
        error.setErrorCode(code);
        error.setErrorMessage(message);
        error.setRawValue(raw);
        error.setSuggestion(suggestion);
        error.setSeverity(severity);
        error.setCreateTime(now());
        importErrorMapper.insert(error);
    }

    private SheetMatch findDailyViolationSheet(Workbook workbook, DataFormatter formatter, String sheetName) {
        SheetMatch fallback = null;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (StringUtils.isNotBlank(sheetName) && !StringUtils.equals(sheet.getSheetName(), sheetName)) {
                continue;
            }
            Integer headerRowIndex = findDailyViolationHeaderRow(sheet, formatter);
            if (headerRowIndex == null) {
                continue;
            }
            String title = cell(sheet.getRow(Math.max(0, headerRowIndex - 1)), 0, formatter);
            SheetMatch match = new SheetMatch(sheet, headerRowIndex, title);
            if (StringUtils.isNotBlank(sheetName) || sheet.getSheetName().contains("LKJ") || title.contains("违标问题登记簿")) {
                return match;
            }
            if (fallback == null) {
                fallback = match;
            }
        }
        return fallback;
    }

    private Integer findDailyViolationHeaderRow(Sheet sheet, DataFormatter formatter) {
        int max = Math.min(sheet.getLastRowNum(), 20);
        for (int i = 0; i <= max; i++) {
            String joined = joinRow(sheet.getRow(i), formatter);
            if (joined.contains("序号") && joined.contains("违标编码") && joined.contains("拟考核内容")) {
                return i;
            }
        }
        return null;
    }

    private Map<String, Integer> readHeader(Row row, DataFormatter formatter) {
        Map<String, Integer> header = new LinkedHashMap<>();
        if (row == null) {
            return header;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String text = cell(row, i, formatter);
            if (StringUtils.isNotBlank(text)) {
                header.put(text.trim(), i);
            }
        }
        return header;
    }

    private Map<String, String> rawRow(Row row, Map<String, Integer> header, DataFormatter formatter) {
        Map<String, String> raw = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : header.entrySet()) {
            raw.put(entry.getKey(), cell(row, entry.getValue(), formatter));
        }
        return raw;
    }

    private String value(Row row, Map<String, Integer> header, DataFormatter formatter, String name) {
        Integer index = header.get(name);
        return index == null ? "" : cell(row, index, formatter);
    }

    private String cell(Row row, int index, DataFormatter formatter) {
        if (row == null || index < 0 || row.getCell(index) == null) {
            return "";
        }
        return formatter.formatCellValue(row.getCell(index)).trim();
    }

    private String joinRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            builder.append(cell(row, i, formatter));
        }
        return builder.toString();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        return StringUtils.isBlank(joinRow(row, formatter));
    }

    private void refreshImportBatchCounts(Long importBatchId) {
        DailyViolationImportBatch batch = queryImportBatch(importBatchId);
        List<DailyViolationImportRow> rows = importRowMapper.selectList(Wrappers.lambdaQuery(DailyViolationImportRow.class)
            .eq(DailyViolationImportRow::getImportBatchId, importBatchId));
        int valid = 0;
        int warning = 0;
        int invalid = 0;
        for (DailyViolationImportRow row : rows) {
            if (VALID.equals(row.getValidationStatus())) {
                valid++;
            } else if (NEED_CONFIRM.equals(row.getValidationStatus())) {
                warning++;
            } else if (INVALID.equals(row.getValidationStatus())) {
                invalid++;
            }
        }
        batch.setTotalRows(rows.size());
        batch.setValidRows(valid);
        batch.setWarningRows(warning);
        batch.setInvalidRows(invalid);
        batch.setUpdateBy(LoginHelper.getUserId());
        batch.setUpdateTime(now());
        importBatchMapper.updateById(batch);
    }

    private DailyViolationBatch createSubmitBatch(DailyViolationImportBatch importBatch, int rows) {
        DailyViolationBatch batch = new DailyViolationBatch();
        batch.setBatchId(IdGeneratorUtil.nextLongId());
        batch.setTenantId(tenantId());
        batch.setReportDate(importBatch.getReportDate());
        batch.setReportTitle(importBatch.getTitleText());
        batch.setSourceType("EXCEL_IMPORT");
        batch.setSourceFileId(importBatch.getOriginalFileId());
        batch.setBatchStatus(STATUS_ANALYST_SUBMITTED);
        batch.setTotalRows(rows);
        batch.setValidRows(0);
        batch.setWarningRows(0);
        batch.setInvalidRows(0);
        batch.setSubmittedRows(0);
        batch.setDelFlag(FLAG_NO);
        fillCreate(batch);
        batchMapper.insert(batch);
        return batch;
    }

    private Long ensureManualBatch(DailyViolationRecord record) {
        DailyViolationBatch batch = new DailyViolationBatch();
        batch.setBatchId(IdGeneratorUtil.nextLongId());
        batch.setTenantId(tenantId());
        batch.setReportDate(record.getReportDate());
        batch.setReportTitle("手工录入-" + formatDate(record.getReportDate()));
        batch.setSourceType("MANUAL");
        batch.setBatchStatus(STATUS_DRAFT);
        batch.setTotalRows(1);
        batch.setValidRows(1);
        batch.setWarningRows(0);
        batch.setInvalidRows(0);
        batch.setSubmittedRows(0);
        batch.setDelFlag(FLAG_NO);
        fillCreate(batch);
        batchMapper.insert(batch);
        return batch.getBatchId();
    }

    private DailyViolationResult insertResult(DailyViolationRecord record, String included, String status, String reason, Long correctedFromResultId) {
        Integer maxVersion = resultMapper.selectList(Wrappers.lambdaQuery(DailyViolationResult.class)
                .eq(DailyViolationResult::getRecordId, record.getRecordId()))
            .stream().map(DailyViolationResult::getResultVersion).filter(v -> v != null).max(Integer::compareTo).orElse(0);
        DailyViolationResult result = new DailyViolationResult();
        result.setResultId(IdGeneratorUtil.nextLongId());
        result.setTenantId(tenantId());
        result.setRecordId(record.getRecordId());
        result.setBatchId(record.getBatchId());
        result.setResultVersion(maxVersion + 1);
        result.setResultStatus(status);
        result.setIncluded(included);
        result.setResultSnapshot(JsonUtils.toJsonString(snapshot(record)));
        result.setArchivedBy(LoginHelper.getUserId());
        result.setArchivedUserName(LoginHelper.getUsername());
        result.setArchivedTime(now());
        result.setCorrectedFromResultId(correctedFromResultId);
        result.setCorrectReason(reason);
        result.setDelFlag(FLAG_NO);
        result.setCreateTime(now());
        resultMapper.insert(result);
        return result;
    }

    private DailyViolationResult selectResultEntity(Long resultId) {
        DailyViolationResult result = resultMapper.selectById(resultId);
        if (result == null) {
            throw new ServiceException("daily violation result not found");
        }
        return result;
    }

    private DailyViolationResult selectResultVersion(Long recordId, Integer version) {
        DailyViolationResult result = resultMapper.selectOne(Wrappers.lambdaQuery(DailyViolationResult.class)
            .eq(DailyViolationResult::getRecordId, recordId)
            .eq(DailyViolationResult::getResultVersion, version), false);
        if (result == null) {
            throw new ServiceException("daily violation result version not found");
        }
        return result;
    }

    private Map<String, Object> parseSnapshot(String snapshotJson) {
        if (StringUtils.isBlank(snapshotJson)) {
            return Collections.emptyMap();
        }
        return JsonUtils.parseMap(snapshotJson);
    }

    private void applyCodeSnapshot(DailyViolationRecord record, BaseViolationCode code) {
        record.setViolationNatureSnapshot(code.getNature());
        record.setViolationCategorySnapshot(code.getCategory());
        record.setViolationTypeSnapshot(code.getViolationType());
        record.setViolationCodeSnapshot(JsonUtils.toJsonString(snapshot(code)));
    }

    private BaseViolationCode requireViolationCode(String violationCode) {
        BaseViolationCode code = selectViolationCode(violationCode);
        if (code == null) {
            throw new ServiceException("violation code not found: " + violationCode);
        }
        return code;
    }

    private BaseViolationCode selectViolationCode(String violationCode) {
        return violationCodeMapper.selectOne(Wrappers.lambdaQuery(BaseViolationCode.class)
            .eq(BaseViolationCode::getViolationCode, violationCode)
            .eq(BaseViolationCode::getStatus, SystemConstants.NORMAL), false);
    }

    private BasePersonnel requirePersonnel(String employeeNo) {
        BasePersonnel personnel = selectPersonnel(employeeNo);
        if (personnel == null) {
            throw new ServiceException("employee not found: " + employeeNo);
        }
        return personnel;
    }

    private BasePersonnel selectPersonnel(String employeeNo) {
        return personnelMapper.selectOne(Wrappers.lambdaQuery(BasePersonnel.class)
            .eq(BasePersonnel::getJobNo, employeeNo)
            .eq(BasePersonnel::getStatus, SystemConstants.NORMAL), false);
    }

    private void writeFlowLog(DailyViolationRecord record, String action, String before, String after, String opinion,
                              String attachments, String changedFields) {
        DailyViolationFlowLog log = new DailyViolationFlowLog();
        log.setFlowId(IdGeneratorUtil.nextLongId());
        log.setTenantId(tenantId());
        log.setRecordId(record.getRecordId());
        log.setBatchId(record.getBatchId());
        log.setActionCode(action);
        log.setBeforeStatus(before);
        log.setAfterStatus(after);
        log.setOperatorId(LoginHelper.getUserId());
        log.setOperatorNameSnapshot(LoginHelper.getUsername());
        log.setOperatorDeptSnapshot(LoginHelper.getDeptName());
        log.setOperatorRoleSnapshot(currentRoles());
        log.setOpinion(opinion);
        log.setAttachmentRefs(attachments);
        log.setChangedFieldsJson(changedFields);
        log.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        log.setCreateTime(now());
        flowLogMapper.insert(log);
    }

    private void writeTitleAndHeader(Workbook workbook, Sheet sheet, String titleText) {
        Row title = sheet.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue(titleText);
        CellStyle titleStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        titleStyle.setFont(font);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, EXPORT_HEADERS.length - 1));
        Row header = sheet.createRow(1);
        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
            header.createCell(i).setCellValue(EXPORT_HEADERS[i]);
            sheet.setColumnWidth(i, 16 * 256);
        }
        sheet.setColumnWidth(2, 52 * 256);
    }

    private void writeRecordRow(Row row, DailyViolationRecord record, int index) {
        row.createCell(0).setCellValue(StringUtils.blankToDefault(record.getSequenceNo(), String.valueOf(index)));
        row.createCell(1).setCellValue(nvl(record.getViolationCode()));
        row.createCell(2).setCellValue(nvl(record.getProposedAssessmentContent()));
        row.createCell(3).setCellValue(nvl(record.getViolationNatureSnapshot()));
        row.createCell(4).setCellValue(nvl(record.getViolationCategorySnapshot()));
        row.createCell(5).setCellValue(nvl(record.getViolationTypeSnapshot()));
        row.createCell(6).setCellValue(nvl(record.getResponsibleDeptNameSnapshot()));
        row.createCell(7).setCellValue(nvl(record.getEmployeeNameSnapshot()));
        row.createCell(8).setCellValue(nvl(record.getHandlingAssessment()));
        row.createCell(9).setCellValue(nvl(record.getIssuingDept()));
        row.createCell(10).setCellValue(nvl(record.getEmployeeNo()));
        row.createCell(11).setCellValue(nvl(record.getTimeSegment()));
        row.createCell(12).setCellValue(nvl(record.getPartyMemberFlag()));
        row.createCell(13).setCellValue(nvl(record.getTicketNo()));
        row.createCell(14).setCellValue(nvl(record.getGuideDriver()));
        row.createCell(15).setCellValue("");
        row.createCell(16).setCellValue(nvl(record.getLocation()));
        row.createCell(17).setCellValue(nvl(record.getGuideGroup()));
        row.createCell(18).setCellValue(nvl(record.getAbcdAssignment()));
    }

    private void writeWorkbook(HttpServletResponse response, Workbook workbook, String fileName) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);
        workbook.write(response.getOutputStream());
    }

    private Map<String, Object> snapshot(Object object) {
        return JsonUtils.parseMap(JsonUtils.toJsonString(object));
    }

    private Map<String, Object> snapshotOrg(DailyViolationRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("responsibleDeptId", record.getResponsibleDeptId());
        map.put("responsibleDeptName", record.getResponsibleDeptNameSnapshot());
        map.put("workshopId", record.getWorkshopId());
        map.put("workshopName", record.getWorkshopName());
        map.put("teamId", record.getTeamId());
        map.put("teamName", record.getTeamName());
        map.put("guideGroupId", record.getGuideGroupId());
        map.put("guideGroupName", record.getGuideGroupName());
        return map;
    }

    private Date parseReportDate(String title, Integer businessYear) {
        Matcher matcher = TITLE_DATE.matcher(StringUtils.blankToDefault(title, ""));
        if (matcher.find()) {
            int year = businessYear == null ? currentYear() : businessYear;
            return dateFromMonthDay(year, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }
        return DateUtils.getNowDate();
    }

    private Date dateFromMonthDay(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 0, 0, 0);
        return calendar.getTime();
    }

    private String firstMatch(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group() : "";
    }

    private String normalizeName(BasePersonnel personnel) {
        return normalizeText(StringUtils.blankToDefault(personnel.getNormalizedName(), personnel.getPersonName()));
    }

    private String normalizeText(String text) {
        return StringUtils.blankToDefault(text, "").replaceFirst("^\\d+", "").trim();
    }

    private String safeOpinion(DailyViolationActionBo bo) {
        return bo == null ? null : bo.getOpinion();
    }

    private void requireText(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(message);
        }
    }

    private String currentRoles() {
        return LoginHelper.getLoginUser() == null ? "" : JsonUtils.toJsonString(LoginHelper.getLoginUser().getRolePermission());
    }

    private Set<String> currentRoleKeys() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || CollUtil.isEmpty(loginUser.getRolePermission())) {
            return Collections.emptySet();
        }
        return loginUser.getRolePermission();
    }

    private void fillCreate(org.dromara.common.mybatis.core.domain.BaseEntity entity) {
        entity.setCreateDept(LoginHelper.getDeptId());
        entity.setCreateBy(LoginHelper.getUserId());
        entity.setCreateTime(now());
    }

    private String tenantId() {
        return StringUtils.blankToDefault(LoginHelper.getTenantId(), "000000");
    }

    private Date now() {
        return DateUtils.getNowDate();
    }

    private int currentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private String formatDate(Date date) {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String nvl(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record SheetMatch(Sheet sheet, int headerRowIndex, String title) {
    }
}
