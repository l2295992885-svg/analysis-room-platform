package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;
import org.dromara.system.domain.BizFileBind;

/**
 * Business attachment binding query and form object.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BizFileBind.class, reverseConvertGenerate = false)
public class BizFileBindBo extends TenantEntity {

    private Long id;

    private Long ossId;

    private String businessType;

    private String businessId;

    private String businessAction;

    private String attachmentType;

    private String permissionScope;

    private String originalName;

    private String fileSuffix;

    private String status;

    private String remark;
}
