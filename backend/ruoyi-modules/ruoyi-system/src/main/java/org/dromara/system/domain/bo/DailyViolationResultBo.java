package org.dromara.system.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class DailyViolationResultBo extends DailyViolationRecordBo {

    private Long resultId;
    private Long recordId;
    private Integer resultVersion;
    private String resultStatus;
    private String included;
    private Date archivedTimeStart;
    private Date archivedTimeEnd;
}
