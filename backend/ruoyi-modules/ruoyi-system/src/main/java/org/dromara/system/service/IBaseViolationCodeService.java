package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BaseViolationCodeBo;
import org.dromara.system.domain.vo.BaseViolationCodeVo;

import java.util.List;

public interface IBaseViolationCodeService {

    TableDataInfo<BaseViolationCodeVo> queryPageList(BaseViolationCodeBo bo, PageQuery pageQuery);

    List<BaseViolationCodeVo> queryList(BaseViolationCodeBo bo);

    BaseViolationCodeVo queryById(Long id);

    BaseViolationCodeVo queryByCode(String violationCode);

    boolean insertByBo(BaseViolationCodeBo bo);

    boolean updateByBo(BaseViolationCodeBo bo);

    boolean deleteWithValidByIds(List<Long> ids);

    String importData(List<BaseViolationCodeVo> rows, Boolean updateSupport);
}
