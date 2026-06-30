package org.dromara.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Lightweight chat business card send object.
 */
@Data
public class BizChatBusinessCardBo {

    @NotNull(message = "会话ID不能为空")
    private Long conversationId;

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 64, message = "业务类型长度不能超过{max}个字符")
    private String businessType;

    @NotBlank(message = "业务ID不能为空")
    @Size(max = 64, message = "业务ID长度不能超过{max}个字符")
    private String businessId;

    @NotBlank(message = "业务标题不能为空")
    @Size(max = 200, message = "业务标题长度不能超过{max}个字符")
    private String businessTitle;

    @Size(max = 500, message = "业务跳转地址长度不能超过{max}个字符")
    private String businessUrl;

    @Size(max = 4000, message = "业务卡片快照长度不能超过{max}个字符")
    private String businessPayload;

    @Size(max = 2000, message = "消息内容长度不能超过{max}个字符")
    private String messageContent;
}
