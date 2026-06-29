package org.dromara.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Lightweight chat text message send object.
 */
@Data
public class BizChatMessageSendBo {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容长度不能超过{max}个字符")
    private String messageContent;
}
