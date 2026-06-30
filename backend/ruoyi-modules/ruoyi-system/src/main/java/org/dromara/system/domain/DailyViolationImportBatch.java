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
@TableName("daily_violation_import_batch")
public class DailyViolationImportBatch extends TenantEntity {

    @TableId(value = "import_batch_id")
    private Long importBatchId;

    private Date reportDate;
    private Long originalFileId;
    private String originalFileName;
    private String sheetName;
    private String titleText;
    private Integer headerRowIndex;
    private Integer totalRows;
    private Integer validRows;
    private Integer warningRows;
    private Integer invalidRows;
    private String importStatus;
    private Long importedBy;
    private String importedUserName;
    private Date importedTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
