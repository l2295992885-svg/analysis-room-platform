package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.BizChatMessage;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Lightweight chat message view object.
 */
@Data
@AutoMapper(target = BizChatMessage.class)
public class BizChatMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

    private Long conversationId;

    private String messageType;

    private String messageContent;

    private String businessType;

    private String businessId;

    private String businessTitle;

    private String businessUrl;

    private String businessPayload;

    private Long senderUserId;

    private String senderUserName;

    private Long senderDeptId;

    private String senderDeptName;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
