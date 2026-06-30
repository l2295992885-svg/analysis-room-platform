package org.dromara.system.domain.bo;

import lombok.Data;

import java.util.Date;

@Data
public class DailyViolationImportRowBo {

    private String employeeNo;
    private String responsiblePersonName;
    private String responsibleDeptName;
    private String violationCode;
    private String violationNature;
    private String violationCategory;
    private String violationType;
    private Date parsedViolationDate;
    private String parsedViolationTime;
    private String parsedLocomotive;
    private String parsedTrainNo;
    private String location;
    private String confirmStatus;
    private String confirmRemark;
}
