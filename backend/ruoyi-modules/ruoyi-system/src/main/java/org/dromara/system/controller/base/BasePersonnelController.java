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
import org.dromara.system.domain.bo.BasePersonnelBo;
import org.dromara.system.domain.vo.BasePersonnelVo;
import org.dromara.system.domain.vo.PersonnelRosterImportResultVo;
import org.dromara.system.service.IBasePersonnelService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/base/personnel")
public class BasePersonnelController extends BaseController {

    private final IBasePersonnelService basePersonnelService;

    @SaCheckPermission("base:personnel:list")
    @GetMapping("/list")
    public TableDataInfo<BasePersonnelVo> list(BasePersonnelBo bo, PageQuery pageQuery) {
        return basePersonnelService.queryPageList(bo, pageQuery);
    }

    @Log(title = "人员基础数据", businessType = BusinessType.EXPORT)
    @SaCheckPermission("base:personnel:export")
    @PostMapping("/export")
    public void export(BasePersonnelBo bo, HttpServletResponse response) {
        List<BasePersonnelVo> list = basePersonnelService.queryList(bo);
        ExcelUtil.exportExcel(list, "人员基础数据", BasePersonnelVo.class, response);
    }

    @SaCheckPermission("base:personnel:query")
    @GetMapping("/{id}")
    public R<BasePersonnelVo> getInfo(@PathVariable Long id) {
        return R.ok(basePersonnelService.queryById(id));
    }

    @SaCheckPermission("base:personnel:query")
    @GetMapping("/jobNo/{jobNo}")
    public R<BasePersonnelVo> getByJobNo(@PathVariable String jobNo) {
        return R.ok(basePersonnelService.queryByJobNo(jobNo));
    }

    @SaCheckPermission("base:personnel:add")
    @Log(title = "人员基础数据", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated @RequestBody BasePersonnelBo bo) {
        return toAjax(basePersonnelService.insertByBo(bo));
    }

    @SaCheckPermission("base:personnel:edit")
    @Log(title = "人员基础数据", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated @RequestBody BasePersonnelBo bo) {
        return toAjax(basePersonnelService.updateByBo(bo));
    }

    @SaCheckPermission("base:personnel:remove")
    @Log(title = "人员基础数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(basePersonnelService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    @Log(title = "人员基础数据", businessType = BusinessType.IMPORT)
    @SaCheckPermission("base:personnel:import")
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file, boolean updateSupport) throws Exception {
        List<BasePersonnelVo> rows = ExcelUtil.importExcel(file.getInputStream(), BasePersonnelVo.class);
        return R.ok(basePersonnelService.importData(rows, updateSupport));
    }

    @Log(title = "人员执行名单导入", businessType = BusinessType.IMPORT)
    @SaCheckPermission("base:personnel:import")
    @PostMapping(value = "/importRoster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<PersonnelRosterImportResultVo> importRoster(@RequestPart("file") MultipartFile file,
                                                         @RequestParam(required = false) String sheetName) {
        return R.ok(basePersonnelService.importRoster(file, sheetName));
    }

    @SaCheckPermission("base:personnel:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(List.of(), "人员基础数据", BasePersonnelVo.class, response);
    }
}
