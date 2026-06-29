package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BizTaskBo;
import org.dromara.system.domain.bo.BizTaskCloseBo;
import org.dromara.system.domain.vo.BizTaskVo;

/**
 * Common todo task service.
 */
public interface IBizTaskService {

    TableDataInfo<BizTaskVo> queryMyPendingPage(BizTaskBo bo, PageQuery pageQuery);

    TableDataInfo<BizTaskVo> queryDonePage(BizTaskBo bo, PageQuery pageQuery);

    BizTaskVo queryById(Long id);

    BizTaskVo openTask(Long id);

    Boolean closeTask(Long id, BizTaskCloseBo bo);
}
