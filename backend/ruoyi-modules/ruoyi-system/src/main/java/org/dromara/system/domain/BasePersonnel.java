package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * Personnel base data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_personnel")
public class BasePersonnel extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    private String jobNo;

    private String personName;

    private Long deptId;

    private String deptName;

    private String workshop;

    private String teamName;

    private String guideGroup;

    private String positionName;

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

    @TableLogic
    private String delFlag;

    private String remark;
}
