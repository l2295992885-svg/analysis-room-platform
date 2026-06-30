package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("daily_violation_flow_log")
public class DailyViolationFlowLog {

    @TableId(value = "flow_id")
    private Long flowId;

    private String tenantId;
    private Long recordId;
    private Long batchId;
    private String actionCode;
    private String beforeStatus;
    private String afterStatus;
    private Long operatorId;
    private String operatorNameSnapshot;
    private String operatorDeptSnapshot;
    private String operatorRoleSnapshot;
    private String opinion;
    private String attachmentRefs;
    private String changedFieldsJson;
    private String traceId;
    private Date createTime;
}
