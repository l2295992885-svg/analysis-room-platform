package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("daily_violation_result")
public class DailyViolationResult {

    @TableId(value = "result_id")
    private Long resultId;

    private String tenantId;
    private Long recordId;
    private Long batchId;
    private Integer resultVersion;
    private String resultStatus;
    private String included;
    private String resultSnapshot;
    private Long archivedBy;
    private String archivedUserName;
    private Date archivedTime;
    private Long correctedFromResultId;
    private String correctReason;

    @TableLogic
    private String delFlag;

    private Date createTime;
    private Date updateTime;
}
