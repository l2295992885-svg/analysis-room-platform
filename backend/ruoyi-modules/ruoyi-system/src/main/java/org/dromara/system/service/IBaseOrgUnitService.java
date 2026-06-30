package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BaseOrgUnitBo;
import org.dromara.system.domain.vo.BaseOrgUnitVo;

import java.util.List;

public interface IBaseOrgUnitService {

    TableDataInfo<BaseOrgUnitVo> queryPageList(BaseOrgUnitBo bo, PageQuery pageQuery);

    List<BaseOrgUnitVo> queryList(BaseOrgUnitBo bo);

    BaseOrgUnitVo queryById(Long id);

    boolean insertByBo(BaseOrgUnitBo bo);

    boolean updateByBo(BaseOrgUnitBo bo);

    boolean deleteWithValidByIds(List<Long> ids);

    String importData(List<BaseOrgUnitVo> rows, Boolean updateSupport);
}
