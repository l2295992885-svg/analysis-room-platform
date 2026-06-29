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
import org.dromara.system.domain.bo.BizTaskBo;
import org.dromara.system.domain.bo.BizTaskCloseBo;
import org.dromara.system.domain.vo.BizTaskVo;
import org.dromara.system.service.IBizTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Todo center.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/todos")
public class TodoController extends BaseController {

    private final IBizTaskService taskService;

    @SaCheckPermission("todo:view")
    @GetMapping("/my")
    public TableDataInfo<BizTaskVo> my(@Validated(QueryGroup.class) BizTaskBo bo, PageQuery pageQuery) {
        return taskService.queryMyPendingPage(bo, pageQuery);
    }

    @SaCheckPermission("todo:view")
    @GetMapping("/done")
    public TableDataInfo<BizTaskVo> done(@Validated(QueryGroup.class) BizTaskBo bo, PageQuery pageQuery) {
        return taskService.queryDonePage(bo, pageQuery);
    }

    @SaCheckPermission("todo:view")
    @GetMapping("/{id}")
    public R<BizTaskVo> getInfo(@PathVariable Long id) {
        return R.ok(taskService.queryById(id));
    }

    @SaCheckPermission("todo:view")
    @Log(title = "待办打开", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/open")
    public R<BizTaskVo> open(@PathVariable Long id) {
        return R.ok(taskService.openTask(id));
    }

    @SaCheckPermission("todo:close")
    @Log(title = "待办关闭", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/close")
    public R<Void> close(@PathVariable Long id, @Validated @RequestBody(required = false) BizTaskCloseBo bo) {
        return toAjax(taskService.closeTask(id, bo));
    }
}
