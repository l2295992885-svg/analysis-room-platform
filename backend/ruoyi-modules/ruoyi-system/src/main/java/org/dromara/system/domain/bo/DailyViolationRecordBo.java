package org.dromara.system.domain.bo;

import lombok.Data;

import java.util.Date;

@Data
public class DailyViolationRecordBo {

    private Long batchId;
    private Date reportDate;
    private Date violationDateStart;
    private Date violationDateEnd;
    private Date violationDate;
    private Long responsibleDeptId;
    private String responsibleDeptName;
    private String employeeNo;
    private String employeeName;
    private String violationCode;
    private String currentStatus;
    private String validationStatus;

    private String violationTime;
    private String sequenceNo;
    private String violationNatureSnapshot;
    private String violationCategorySnapshot;
    private String violationTypeSnapshot;
    private String proposedAssessmentContent;
    private Long responsiblePersonId;
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
    private Long workshopId;
    private String workshopName;
    private Long teamId;
    private String teamName;
    private Long guideGroupId;
    private String guideGroupName;
    private String remark;
}
