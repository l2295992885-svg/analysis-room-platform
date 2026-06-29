package org.dromara.system.domain.bo;

import lombok.Data;

@Data
public class DailyViolationResultCorrectBo {

    private String included;

    private String resultStatus;

    private String correctReason;
}
