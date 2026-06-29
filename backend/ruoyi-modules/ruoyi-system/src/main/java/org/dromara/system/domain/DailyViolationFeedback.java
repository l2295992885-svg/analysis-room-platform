package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_violation_feedback")
public class DailyViolationFeedback extends TenantEntity {

    @TableId(value = "feedback_id")
    private Long feedbackId;

    private Long recordId;
    private Long batchId;
    private String reasonType;
    private String reasonDescription;
    private Long feedbackDeptId;
    private String feedbackDeptNameSnapshot;
    private Long feedbackUserId;
    private String feedbackUserNameSnapshot;
    private String attachmentRefs;
    private String feedbackStatus;

    @TableLogic
    private String delFlag;

    private String remark;
}
