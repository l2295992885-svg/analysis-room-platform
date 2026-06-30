package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.system.domain.BasePersonnel;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = BasePersonnel.class, reverseConvertGenerate = false)
public class BasePersonnelBo extends BaseEntity {

    private Long id;

    @NotBlank(message = "工号不能为空")
    @Size(max = 32, message = "工号长度不能超过{max}个字符")
    private String jobNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过{max}个字符")
    private String personName;

    private Long deptId;

    @Size(max = 100, message = "责任部门长度不能超过{max}个字符")
    private String deptName;

    @Size(max = 100, message = "车间长度不能超过{max}个字符")
    private String workshop;

    @Size(max = 100, message = "车队长度不能超过{max}个字符")
    private String teamName;

    @Size(max = 100, message = "指导组长度不能超过{max}个字符")
    private String guideGroup;

    @Size(max = 100, message = "岗位长度不能超过{max}个字符")
    private String positionName;

    @Size(max = 32, message = "联系电话长度不能超过{max}个字符")
    private String phone;

    private String nation;

    private String lineName;

    private String jobTitle;

    private String currentPosition;

    private String commandTime;

    private String postTime;

    private String politicalStatus;

    private String qualification;

    private String permittedLocomotiveType;

    private String birthDate;

    private String workStartDate;

    private String idCard;

    private String workCardNo;

    private String normalizedName;

    private String rawJson;

    private String status;

    private String remark;
}
