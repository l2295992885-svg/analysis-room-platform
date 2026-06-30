package org.dromara.system.domain.bo;

import lombok.Data;

import java.util.List;

@Data
public class DailyViolationActionBo {

    private String opinion;
    private Long workshopId;
    private String workshopName;
    private Long teamId;
    private String teamName;
    private Long guideGroupId;
    private String guideGroupName;
    private String reasonType;
    private String reasonDescription;
    private List<Long> attachmentIds;
    private String finalOpinion;
    private String finalDecision;
    private String cancelReason;
}
