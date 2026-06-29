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
import org.dromara.system.domain.bo.BaseImportTemplateBo;
import org.dromara.system.domain.vo.BaseImportTemplateVo;
import org.dromara.system.service.IBaseImportTemplateService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/importTemplate")
public class BaseImportTemplateController extends BaseController {

    private final IBaseImportTemplateService baseImportTemplateService;

    @SaCheckPermission("base:importTemplate:list")
    @GetMapping("/list")
    public TableDataInfo<BaseImportTemplateVo> list(BaseImportTemplateBo bo, PageQuery pageQuery) {
        return baseImportTemplateService.queryPageList(bo, pageQuery);
    }

    @Log(title = "导入模板", businessType = BusinessType.EXPORT)
    @SaCheckPermission("base:importTemplate:export")
    @PostMapping("/export")
    public void export(BaseImportTemplateBo bo, HttpServletResponse response) {
        List<BaseImportTemplateVo> list = baseImportTemplateService.queryList(bo);
        ExcelUtil.exportExcel(list, "导入模板", BaseImportTemplateVo.class, response);
    }

    @SaCheckPermission("base:importTemplate:query")
    @GetMapping("/{id}")
    public R<BaseImportTemplateVo> getInfo(@PathVariable Long id) {
        return R.ok(baseImportTemplateService.queryById(id));
    }

    @SaCheckPermission("base:importTemplate:add")
    @Log(title = "导入模板", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated @RequestBody BaseImportTemplateBo bo) {
        return toAjax(baseImportTemplateService.insertByBo(bo));
    }

    @SaCheckPermission("base:importTemplate:edit")
    @Log(title = "导入模板", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated @RequestBody BaseImportTemplateBo bo) {
        return toAjax(baseImportTemplateService.updateByBo(bo));
    }

    @SaCheckPermission("base:importTemplate:remove")
    @Log(title = "导入模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(baseImportTemplateService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @Log(title = "导入模板", businessType = BusinessType.IMPORT)
    @SaCheckPermission("base:importTemplate:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        List<BaseImportTemplateVo> rows = ExcelUtil.importExcel(file.getInputStream(), BaseImportTemplateVo.class);
        return R.ok(baseImportTemplateService.importData(rows, updateSupport));
    }

    @SaCheckPermission("base:importTemplate:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(List.of(), "导入模板", BaseImportTemplateVo.class, response);
    }
}
