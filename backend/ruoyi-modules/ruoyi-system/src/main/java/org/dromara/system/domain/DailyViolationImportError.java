package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("daily_violation_import_error")
public class DailyViolationImportError {

    @TableId(value = "error_id")
    private Long errorId;

    private String tenantId;
    private Long importBatchId;
    private Long rowId;
    private Integer rowNo;
    private String fieldName;
    private String errorCode;
    private String errorMessage;
    private String rawValue;
    private String suggestion;
    private String severity;
    private Date createTime;
}
