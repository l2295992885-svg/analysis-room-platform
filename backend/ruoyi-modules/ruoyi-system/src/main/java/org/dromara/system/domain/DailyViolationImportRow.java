package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_violation_import_row")
public class DailyViolationImportRow extends TenantEntity {

    @TableId(value = "row_id")
    private Long rowId;

    private Long importBatchId;
    private Integer rowNo;
    private String rawJson;
    private String sequenceNo;
    private String violationCode;
    private String proposedAssessmentContent;
    private String violationNature;
    private String violationCategory;
    private String violationType;
    private String responsibleDeptName;
    private String responsiblePersonName;
    private String handlingAssessment;
    private String issuingDept;
    private String employeeNo;
    private String timeSegment;
    private String partyMemberFlag;
    private String ticketNo;
    private String guideDriver;
    private String falseReason;
    private String location;
    private String guideGroup;
    private String abcdAssignment;
    private Date parsedViolationDate;
    private String parsedViolationTime;
    private String parsedLocomotive;
    private String parsedTrainNo;
    private String candidatePersonNames;
    private String validationStatus;
    private String validationMessage;
    private String confirmStatus;
    private String confirmRemark;
    private Long generatedRecordId;

    @TableLogic
    private String delFlag;

    private String remark;
}
