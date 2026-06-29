package org.dromara.system.controller.base;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.BaseViolationCodeBo;
import org.dromara.system.domain.vo.BaseViolationCodeVo;
import org.dromara.system.service.IBaseViolationCodeService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/violationCode")
public class BaseViolationCodeController extends BaseController {

    private final IBaseViolationCodeService baseViolationCodeService;

    @SaCheckPermission("base:violationCode:list")
    @GetMapping("/list")
    public TableDataInfo<BaseViolationCodeVo> list(BaseViolationCodeBo bo, PageQuery pageQuery) {
        return baseViolationCodeService.queryPageList(bo, pageQuery);
    }

    @Log(title = "违标编码", businessType = BusinessType.EXPORT)
    @SaCheckPermission("base:violationCode:export")
    @PostMapping("/export")
    public void export(BaseViolationCodeBo bo, HttpServletResponse response) {
        List<BaseViolationCodeVo> list = baseViolationCodeService.queryList(bo);
        ExcelUtil.exportExcel(list, "违标编码", BaseViolationCodeVo.class, response);
    }

    @SaCheckPermission("base:violationCode:query")
    @GetMapping("/{id}")
    public R<BaseViolationCodeVo> getInfo(@PathVariable Long id) {
        return R.ok(baseViolationCodeService.queryById(id));
    }

    @SaCheckPermission("base:violationCode:query")
    @GetMapping("/code/{violationCode}")
    public R<BaseViolationCodeVo> getByCode(@PathVariable String violationCode) {
        return R.ok(baseViolationCodeService.queryByCode(violationCode));
    }

    @SaCheckPermission("base:violationCode:add")
    @Log(title = "违标编码", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated @RequestBody BaseViolationCodeBo bo) {
        return toAjax(baseViolationCodeService.insertByBo(bo));
    }

    @SaCheckPermission("base:violationCode:edit")
    @Log(title = "违标编码", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated @RequestBody BaseViolationCodeBo bo) {
        return toAjax(baseViolationCodeService.updateByBo(bo));
    }

    @SaCheckPermission("base:violationCode:remove")
    @Log(title = "违标编码", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(baseViolationCodeService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @Log(title = "违标编码", businessType = BusinessType.IMPORT)
    @SaCheckPermission("base:violationCode:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        List<BaseViolationCodeVo> rows = ExcelUtil.importExcel(file.getInputStream(), BaseViolationCodeVo.class);
        return R.ok(baseViolationCodeService.importData(rows, updateSupport));
    }

    @SaCheckPermission("base:violationCode:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(List.of(), "违标编码", BaseViolationCodeVo.class, response);
    }
}
