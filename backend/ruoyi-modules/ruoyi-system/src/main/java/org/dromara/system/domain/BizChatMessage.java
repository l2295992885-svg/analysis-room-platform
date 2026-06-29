package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * Lightweight chat message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_chat_message")
public class BizChatMessage extends TenantEntity {

    @TableId(value = "id")
    private Long id;

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

    @TableLogic
    private String delFlag;

    private String remark;
}
