package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BasePersonnelBo;
import org.dromara.system.domain.vo.BasePersonnelVo;
import org.dromara.system.domain.vo.PersonnelRosterImportResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBasePersonnelService {

    TableDataInfo<BasePersonnelVo> queryPageList(BasePersonnelBo bo, PageQuery pageQuery);

    List<BasePersonnelVo> queryList(BasePersonnelBo bo);

    BasePersonnelVo queryById(Long id);

    BasePersonnelVo queryByJobNo(String jobNo);

    boolean insertByBo(BasePersonnelBo bo);

    boolean updateByBo(BasePersonnelBo bo);

    boolean deleteWithValidByIds(List<Long> ids);

    String importData(List<BasePersonnelVo> rows, Boolean updateSupport);

    PersonnelRosterImportResultVo importRoster(MultipartFile file, String sheetName);
}
