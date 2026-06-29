package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.BaseOrgUnit;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BaseOrgUnit.class, reverseConvertGenerate = false)
public class BaseOrgUnitBo extends BaseEntity {

    private Long id;

    private Long parentId;

    @NotBlank(message = "组织编码不能为空")
    @Size(max = 64, message = "组织编码长度不能超过{max}个字符")
    private String orgCode;

    @NotBlank(message = "组织名称不能为空")
    @Size(max = 100, message = "组织名称长度不能超过{max}个字符")
    private String orgName;

    @NotBlank(message = "组织类型不能为空")
    @Size(max = 32, message = "组织类型长度不能超过{max}个字符")
    private String orgType;

    @Size(max = 64, message = "负责人长度不能超过{max}个字符")
    private String leaderName;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    private String status;

    private String remark;
}
