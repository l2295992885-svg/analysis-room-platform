package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;
import org.dromara.system.domain.BizTask;

/**
 * Common todo task query object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizTask.class, reverseConvertGenerate = false)
public class BizTaskBo extends TenantEntity {

    private Long id;

    private String businessType;

    private String businessId;

    private String batchId;

    @Size(max = 200, message = "待办标题长度不能超过{max}个字符")
    private String taskTitle;

    private String taskStatus;

    private String currentNode;

    private String priority;

    private Long receiverUserId;

    private Long receiverDeptId;

    private String receiverRoleKey;

    private Long senderUserId;
}
