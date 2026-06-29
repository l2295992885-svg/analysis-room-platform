package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

/**
 * Business attachment binding.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_file_bind")
public class BizFileBind extends TenantEntity {

    @TableId(value = "id")
    private Long id;

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

    @TableLogic
    private String delFlag;

    private String remark;
}
