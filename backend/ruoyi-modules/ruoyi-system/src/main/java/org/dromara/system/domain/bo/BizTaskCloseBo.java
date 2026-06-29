package org.dromara.system.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Todo task close request.
 */
@Data
public class BizTaskCloseBo {

    @Size(max = 500, message = "关闭说明长度不能超过{max}个字符")
    private String finishComment;
}
