package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * Common mailbox message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_message")
public class BizMessage extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    private String businessType;

    private String businessId;

    private String batchId;

    private String messageType;

    private String messageTitle;

    private String messageContent;

    private String sourceAction;

    private String businessUrl;

    private String businessPayload;

    private Long senderUserId;

    private String senderUserName;

    private Long senderDeptId;

    private String senderDeptName;

    private Long receiverUserId;

    private String receiverUserName;

    private Long receiverDeptId;

    private String receiverDeptName;

    private String receiverRoleKey;

    private String readFlag;

    private Date readTime;

    private String archiveFlag;

    private Date archiveTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
