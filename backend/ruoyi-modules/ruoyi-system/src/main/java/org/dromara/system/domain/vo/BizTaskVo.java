package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.BizTask;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Common todo task view object.
 */
@Data
@AutoMapper(target = BizTask.class)
public class BizTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

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

    private String remark;

    private Date createTime;

    private Date updateTime;
}
