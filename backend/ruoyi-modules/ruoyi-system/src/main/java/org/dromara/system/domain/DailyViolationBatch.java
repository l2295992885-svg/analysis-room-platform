package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("daily_violation_batch")
public class DailyViolationBatch extends TenantEntity {

    @TableId(value = "batch_id")
    private Long batchId;

    private Date reportDate;

    private String reportTitle;

    private String sourceType;

    private Long sourceFileId;

    private String batchStatus;

    private Integer totalRows;

    private Integer validRows;

    private Integer warningRows;

    private Integer invalidRows;

    private Integer submittedRows;

    @TableLogic
    private String delFlag;

    private String remark;
}
