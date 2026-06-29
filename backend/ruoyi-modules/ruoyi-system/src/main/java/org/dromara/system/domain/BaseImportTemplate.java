package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Import template base data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_import_template")
public class BaseImportTemplate extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private String templateCode;

    private String templateName;

    private String businessType;

    private String versionNo;

    private String fileName;

    private Long fileOssId;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
