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
@TableName("daily_violation_record")
public class DailyViolationRecord extends TenantEntity {

    @TableId(value = "record_id")
    private Long recordId;

    private Long batchId;
    private Date reportDate;
    private Date violationDate;
    private String violationTime;
    private String sequenceNo;
    private String violationCode;
    private String violationNatureSnapshot;
    private String violationCategorySnapshot;
    private String violationTypeSnapshot;
    private String proposedAssessmentContent;
    private Long responsibleDeptId;
    private String responsibleDeptNameSnapshot;
    private Long responsiblePersonId;
    private String employeeNo;
    private String employeeNameSnapshot;
    private String locomotive;
    private String trainNo;
    private String location;
    private String timeSegment;
    private String ticketNo;
    private String guideDriver;
    private String guideGroup;
    private String partyMemberFlag;
    private String abcdAssignment;
    private String handlingAssessment;
    private String issuingDept;
    private String currentStatus;
    private Integer version;
    private String sourceType;
    private Long importBatchId;
    private Long importRowId;
    private Long workshopId;
    private String workshopName;
    private Long teamId;
    private String teamName;
    private Long guideGroupId;
    private String guideGroupName;
    private String previewConfirmed;
    private String validationStatus;
    private String validationMessage;
    private String personnelSnapshot;
    private String orgSnapshot;
    private String violationCodeSnapshot;
    private String finalOpinion;
    private String finalDecision;
    private String cancelReason;

    @TableLogic
    private String delFlag;

    private String remark;
}
