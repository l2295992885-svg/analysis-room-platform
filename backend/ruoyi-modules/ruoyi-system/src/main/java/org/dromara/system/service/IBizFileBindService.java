package org.dromara.system.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BizFileBindBo;
import org.dromara.system.domain.vo.BizFileBindVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface IBizFileBindService {

    TableDataInfo<BizFileBindVo> queryPageList(BizFileBindBo bo, PageQuery pageQuery);

    List<BizFileBindVo> queryList(BizFileBindBo bo);

    BizFileBindVo queryById(Long id);

    BizFileBindVo uploadAndBind(MultipartFile file, BizFileBindBo bo);

    void download(Long id, HttpServletResponse response) throws IOException;

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
