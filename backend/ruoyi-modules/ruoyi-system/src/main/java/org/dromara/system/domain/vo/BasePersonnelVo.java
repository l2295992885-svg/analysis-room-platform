package org.dromara.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.system.domain.BasePersonnel;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BasePersonnel.class)
public class BasePersonnelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ExcelProperty(value = "工号")
    private String jobNo;

    @ExcelProperty(value = "姓名")
    private String personName;

    @ExcelProperty(value = "系统部门ID")
    private Long deptId;

    @ExcelProperty(value = "责任部门")
    private String deptName;

    @ExcelProperty(value = "车间")
    private String workshop;

    @ExcelProperty(value = "车队")
    private String teamName;

    @ExcelProperty(value = "指导组")
    private String guideGroup;

    @ExcelProperty(value = "岗位")
    private String positionName;

    @ExcelProperty(value = "联系电话")
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

    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
