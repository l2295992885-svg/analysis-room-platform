package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.BizMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Common mailbox message view object.
 */
@Data
@AutoMapper(target = BizMessage.class)
public class BizMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

    private String businessType;

    private String businessId;

    private String batchId;

    private String messageType;

    private String messageTitle;

    private String messageContent;

    private String sourceAction;

    private String businessUrl;

    private String businessPayload;

    private Long senderUserId;

    private String senderUserName;

    private Long senderDeptId;

    private String senderDeptName;

    private Long receiverUserId;

    private String receiverUserName;

    private Long receiverDeptId;

    private String receiverDeptName;

    private String receiverRoleKey;

    private String readFlag;

    private Date readTime;

    private String archiveFlag;

    private Date archiveTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
