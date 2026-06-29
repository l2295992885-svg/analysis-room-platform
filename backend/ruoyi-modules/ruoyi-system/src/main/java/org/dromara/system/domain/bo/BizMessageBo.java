package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;
import org.dromara.system.domain.BizMessage;

/**
 * Common mailbox message query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizMessage.class, reverseConvertGenerate = false)
public class BizMessageBo extends TenantEntity {

    private Long id;

    private String businessType;

    private String businessId;

    private String batchId;

    private String messageType;

    @Size(max = 200, message = "消息标题长度不能超过{max}个字符")
    private String messageTitle;

    private String sourceAction;

    private Long receiverUserId;

    private Long receiverDeptId;

    private String receiverRoleKey;

    private String readFlag;

    private String archiveFlag;
}
