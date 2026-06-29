package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * Common todo task.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_task")
public class BizTask extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String businessType;

    private String businessId;

    private String batchId;

    private String taskTitle;

    private String taskContent;

    private String taskStatus;

    private String currentNode;

    private String priority;

    private String businessUrl;

    private Long receiverUserId;

    private String receiverUserName;

    private Long receiverDeptId;

    private String receiverDeptName;

    private String receiverRoleKey;

    private Long senderUserId;

    private String senderUserName;

    private Long senderDeptId;

    private String senderDeptName;

    private Date dueTime;

    private Date finishTime;

    private Long finishUserId;

    private String finishUserName;

    private String finishComment;

    @TableLogic
    private String delFlag;

    private String remark;
}
