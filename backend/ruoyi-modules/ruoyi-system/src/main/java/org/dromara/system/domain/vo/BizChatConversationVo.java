package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.BizChatConversation;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Lightweight chat conversation view object.
 */
@Data
@AutoMapper(target = BizChatConversation.class)
public class BizChatConversationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

    private String conversationType;

    private String conversationTitle;

    private String businessType;

    private String businessId;

    private String businessTitle;

    private String businessUrl;

    private String conversationStatus;

    private Long lastMessageId;

    private String lastMessageContent;

    private Date lastMessageTime;

    private String remark;

    private Date createTime;

    private Date updateTime;
}
