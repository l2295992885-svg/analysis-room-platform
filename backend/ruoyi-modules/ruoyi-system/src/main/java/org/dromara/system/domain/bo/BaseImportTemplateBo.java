package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.BaseImportTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BaseImportTemplate.class, reverseConvertGenerate = false)
public class BaseImportTemplateBo extends BaseEntity {

    private Long id;

    @NotBlank(message = "模板编码不能为空")
    @Size(max = 64, message = "模板编码长度不能超过{max}个字符")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过{max}个字符")
    private String templateName;

    @NotBlank(message = "业务类型不能为空")
    @Size(max = 64, message = "业务类型长度不能超过{max}个字符")
    private String businessType;

    @Size(max = 32, message = "版本号长度不能超过{max}个字符")
    private String versionNo;

    @Size(max = 255, message = "模板文件名长度不能超过{max}个字符")
    private String fileName;

    private Long fileOssId;

    private String status;

    private String remark;
}
