package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * Lightweight chat conversation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_chat_conversation")
public class BizChatConversation extends TenantEntity {

    @TableId(value = "id")
    private Long id;

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

    @TableLogic
    private String delFlag;

    private String remark;
}
