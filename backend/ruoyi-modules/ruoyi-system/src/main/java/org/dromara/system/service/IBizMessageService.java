package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BizMessageBo;
import org.dromara.system.domain.vo.BizMessageVo;

/**
 * Common mailbox message service.
 */
public interface IBizMessageService {

    TableDataInfo<BizMessageVo> queryPageList(BizMessageBo bo, PageQuery pageQuery);

    BizMessageVo queryById(Long id);

    Boolean markRead(Long id);

    Boolean archive(Long id);
}
