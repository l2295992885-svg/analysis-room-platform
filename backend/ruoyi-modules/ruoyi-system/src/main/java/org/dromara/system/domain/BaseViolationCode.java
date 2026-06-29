package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Violation code base data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_violation_code")
public class BaseViolationCode extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private String violationCode;

    private String violationName;

    private String nature;

    private String category;

    private String violationType;

    private String description;

    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
