package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Organization base data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_org_unit")
public class BaseOrgUnit extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private Long parentId;

    private String orgCode;

    private String orgName;

    private String orgType;

    private String leaderName;

    private Integer sortOrder;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
