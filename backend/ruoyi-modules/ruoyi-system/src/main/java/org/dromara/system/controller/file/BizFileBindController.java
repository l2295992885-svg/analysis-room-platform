package org.dromara.system.controller.file;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.BizFileBindBo;
import org.dromara.system.domain.vo.BizFileBindVo;
import org.dromara.system.service.IBizFileBindService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/file/attachment")
public class BizFileBindController extends BaseController {

    private final IBizFileBindService fileBindService;

    @SaCheckPermission("file:attachment:list")
    @GetMapping("/list")
    public TableDataInfo<BizFileBindVo> list(@Validated(QueryGroup.class) BizFileBindBo bo, PageQuery pageQuery) {
        return fileBindService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("file:attachment:query")
    @GetMapping("/{id}")
    public R<BizFileBindVo> getInfo(@PathVariable Long id) {
        return R.ok(fileBindService.queryById(id));
    }

    @SaCheckPermission("file:attachment:query")
    @GetMapping("/business")
    public R<List<BizFileBindVo>> listByBusiness(BizFileBindBo bo) {
        return R.ok(fileBindService.queryList(bo));
    }

    @SaCheckPermission("file:attachment:upload")
    @Log(title = "业务附件", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<BizFileBindVo> upload(@RequestPart("file") MultipartFile file,
                                   @RequestParam("businessType") String businessType,
                                   @RequestParam("businessId") String businessId,
                                   @RequestParam(value = "businessAction", required = false) String businessAction,
                                   @RequestParam(value = "attachmentType", required = false) String attachmentType,
                                   @RequestParam(value = "permissionScope", required = false) String permissionScope,
                                   @RequestParam(value = "remark", required = false) String remark) {
        BizFileBindBo bo = new BizFileBindBo();
        bo.setBusinessType(businessType);
        bo.setBusinessId(businessId);
        bo.setBusinessAction(businessAction);
        bo.setAttachmentType(attachmentType);
        bo.setPermissionScope(permissionScope);
        bo.setRemark(remark);
        return R.ok(fileBindService.uploadAndBind(file, bo));
    }

    @SaCheckPermission("file:attachment:download")
    @Log(title = "业务附件下载", businessType = BusinessType.EXPORT)
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
        fileBindService.download(id, response);
    }

    @SaCheckPermission("file:attachment:remove")
    @Log(title = "业务附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(fileBindService.deleteWithValidByIds(List.of(ids), true));
    }
}
