package org.dromara.system.controller.business;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.BizMessageBo;
import org.dromara.system.domain.vo.BizMessageVo;
import org.dromara.system.service.IBizMessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Mailbox center.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mailbox/messages")
public class MailboxController extends BaseController {

    private final IBizMessageService messageService;

    @SaCheckPermission("mailbox:view")
    @GetMapping
    public TableDataInfo<BizMessageVo> list(@Validated(QueryGroup.class) BizMessageBo bo, PageQuery pageQuery) {
        return messageService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("mailbox:view")
    @GetMapping("/{id}")
    public R<BizMessageVo> getInfo(@PathVariable Long id) {
        return R.ok(messageService.queryById(id));
    }

    @SaCheckPermission("mailbox:view")
    @Log(title = "信箱已读", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/read")
    public R<Void> read(@PathVariable Long id) {
        return toAjax(messageService.markRead(id));
    }

    @SaCheckPermission("mailbox:view")
    @Log(title = "信箱归档", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/archive")
    public R<Void> archive(@PathVariable Long id) {
        return toAjax(messageService.archive(id));
    }
}
