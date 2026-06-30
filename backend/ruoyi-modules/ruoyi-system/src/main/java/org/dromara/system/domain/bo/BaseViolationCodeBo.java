package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.BaseViolationCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BaseViolationCode.class, reverseConvertGenerate = false)
public class BaseViolationCodeBo extends BaseEntity {

    private Long id;

    @NotBlank(message = "违标编码不能为空")
    @Size(max = 64, message = "违标编码长度不能超过{max}个字符")
    private String violationCode;

    @NotBlank(message = "违标名称不能为空")
    @Size(max = 200, message = "违标名称长度不能超过{max}个字符")
    private String violationName;

    @NotBlank(message = "性质不能为空")
    @Size(max = 64, message = "性质长度不能超过{max}个字符")
    private String nature;

    @NotBlank(message = "类别不能为空")
    @Size(max = 64, message = "类别长度不能超过{max}个字符")
    private String category;

    @NotBlank(message = "类型不能为空")
    @Size(max = 64, message = "类型长度不能超过{max}个字符")
    private String violationType;

    @Size(max = 500, message = "说明长度不能超过{max}个字符")
    private String description;

    private String status;

    private String remark;
}
