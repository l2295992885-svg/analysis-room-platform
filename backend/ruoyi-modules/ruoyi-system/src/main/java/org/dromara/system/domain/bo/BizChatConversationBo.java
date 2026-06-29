package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;
import org.dromara.system.domain.BizChatConversation;

/**
 * Lightweight chat conversation query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizChatConversation.class, reverseConvertGenerate = false)
public class BizChatConversationBo extends TenantEntity {

    private Long id;

    private String conversationType;

    @Size(max = 200, message = "会话标题长度不能超过{max}个字符")
    private String conversationTitle;

    private String businessType;

    private String businessId;

    private String conversationStatus;
}
