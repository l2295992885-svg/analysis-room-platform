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
import org.dromara.system.domain.bo.BaseOrgUnitBo;
import org.dromara.system.domain.vo.BaseOrgUnitVo;
import org.dromara.system.service.IBaseOrgUnitService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/org")
public class BaseOrgUnitController extends BaseController {

    private final IBaseOrgUnitService baseOrgUnitService;

    @SaCheckPermission("base:org:list")
    @GetMapping("/list")
    public TableDataInfo<BaseOrgUnitVo> list(BaseOrgUnitBo bo, PageQuery pageQuery) {
        return baseOrgUnitService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("base:org:list")
    @GetMapping("/all")
    public R<List<BaseOrgUnitVo>> all(BaseOrgUnitBo bo) {
        return R.ok(baseOrgUnitService.queryList(bo));
    }

    @Log(title = "组织基础数据", businessType = BusinessType.EXPORT)
    @SaCheckPermission("base:org:export")
    @PostMapping("/export")
    public void export(BaseOrgUnitBo bo, HttpServletResponse response) {
        List<BaseOrgUnitVo> list = baseOrgUnitService.queryList(bo);
        ExcelUtil.exportExcel(list, "组织基础数据", BaseOrgUnitVo.class, response);
    }

    @SaCheckPermission("base:org:query")
    @GetMapping("/{id}")
    public R<BaseOrgUnitVo> getInfo(@PathVariable Long id) {
        return R.ok(baseOrgUnitService.queryById(id));
    }

    @SaCheckPermission("base:org:add")
    @Log(title = "组织基础数据", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated @RequestBody BaseOrgUnitBo bo) {
        return toAjax(baseOrgUnitService.insertByBo(bo));
    }

    @SaCheckPermission("base:org:edit")
    @Log(title = "组织基础数据", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated @RequestBody BaseOrgUnitBo bo) {
        return toAjax(baseOrgUnitService.updateByBo(bo));
    }

    @SaCheckPermission("base:org:remove")
    @Log(title = "组织基础数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(baseOrgUnitService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @Log(title = "组织基础数据", businessType = BusinessType.IMPORT)
    @SaCheckPermission("base:org:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        List<BaseOrgUnitVo> rows = ExcelUtil.importExcel(file.getInputStream(), BaseOrgUnitVo.class);
        return R.ok(baseOrgUnitService.importData(rows, updateSupport));
    }

    @SaCheckPermission("base:org:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(List.of(), "组织基础数据", BaseOrgUnitVo.class, response);
    }
}
