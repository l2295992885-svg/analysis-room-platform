package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.BizFileBind;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Business attachment binding view object.
 */
@Data
@AutoMapper(target = BizFileBind.class)
public class BizFileBindVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String tenantId;

    private Long ossId;

    private String businessType;

    private String businessId;

    private String businessAction;

    private String attachmentType;

    private String permissionScope;

    private Long uploadUserId;

    private String uploadUserName;

    private Long uploadDeptId;

    private String uploadDeptName;

    private String originalName;

    private String fileSuffix;

    private Long fileSize;

    private String contentType;

    private String status;

    private String remark;

    private Date createTime;

    private Date updateTime;

    private String fileName;

    private String url;

    private String service;
}
