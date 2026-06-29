package org.dromara.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.system.domain.BaseOrgUnit;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BaseOrgUnit.class)
public class BaseOrgUnitVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ExcelProperty(value = "父级组织ID")
    private Long parentId;

    @ExcelProperty(value = "组织编码")
    private String orgCode;

    @ExcelProperty(value = "组织名称")
    private String orgName;

    @ExcelProperty(value = "组织类型")
    private String orgType;

    @ExcelProperty(value = "负责人")
    private String leaderName;

    @ExcelProperty(value = "排序")
    private Integer sortOrder;

    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
