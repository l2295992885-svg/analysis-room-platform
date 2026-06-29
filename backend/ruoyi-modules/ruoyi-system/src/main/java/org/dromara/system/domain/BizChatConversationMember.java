package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * Lightweight chat conversation member.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_chat_conversation_member")
public class BizChatConversationMember extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private Long conversationId;

    private Long memberUserId;

    private String memberUserName;

    private Long memberDeptId;

    private String memberDeptName;

    private String memberRole;

    private Long lastReadMessageId;

    private Integer unreadCount;

    @TableLogic
    private String delFlag;

    private String remark;
}
