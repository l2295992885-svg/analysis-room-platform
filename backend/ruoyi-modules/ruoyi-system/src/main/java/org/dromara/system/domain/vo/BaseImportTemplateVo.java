package org.dromara.system.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.system.domain.BaseImportTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BaseImportTemplate.class)
public class BaseImportTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @ExcelProperty(value = "模板编码")
    private String templateCode;

    @ExcelProperty(value = "模板名称")
    private String templateName;

    @ExcelProperty(value = "业务类型")
    private String businessType;

    @ExcelProperty(value = "版本号")
    private String versionNo;

    @ExcelProperty(value = "模板文件名")
    private String fileName;

    @ExcelProperty(value = "文件OSS ID")
    private Long fileOssId;

    @ExcelProperty(value = "状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String status;

    @ExcelProperty(value = "备注")
    private String remark;

    @ExcelProperty(value = "创建时间")
    private Date createTime;
}
