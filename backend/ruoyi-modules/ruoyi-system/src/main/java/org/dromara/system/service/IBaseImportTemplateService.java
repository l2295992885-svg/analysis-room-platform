package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BaseImportTemplateBo;
import org.dromara.system.domain.vo.BaseImportTemplateVo;

import java.util.List;

public interface IBaseImportTemplateService {

    TableDataInfo<BaseImportTemplateVo> queryPageList(BaseImportTemplateBo bo, PageQuery pageQuery);

    List<BaseImportTemplateVo> queryList(BaseImportTemplateBo bo);

    BaseImportTemplateVo queryById(Long id);

    boolean insertByBo(BaseImportTemplateBo bo);

    boolean updateByBo(BaseImportTemplateBo bo);

    boolean deleteWithValidByIds(List<Long> ids);

    String importData(List<BaseImportTemplateVo> rows, Boolean updateSupport);
}
