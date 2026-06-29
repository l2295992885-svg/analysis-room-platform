package org.dromara.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.BaseOrgUnit;
import org.dromara.system.domain.bo.BaseOrgUnitBo;
import org.dromara.system.domain.vo.BaseOrgUnitVo;
import org.dromara.system.mapper.BaseOrgUnitMapper;
import org.dromara.system.service.IBaseOrgUnitService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BaseOrgUnitServiceImpl implements IBaseOrgUnitService {

    private final BaseOrgUnitMapper baseMapper;

    @Override
    public TableDataInfo<BaseOrgUnitVo> queryPageList(BaseOrgUnitBo bo, PageQuery pageQuery) {
        Page<BaseOrgUnitVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<BaseOrgUnitVo> queryList(BaseOrgUnitBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public BaseOrgUnitVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public boolean insertByBo(BaseOrgUnitBo bo) {
        validateUniqueOrgCode(bo);
        normalize(bo);
        BaseOrgUnit entity = MapstructUtils.convert(bo, BaseOrgUnit.class);
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(BaseOrgUnitBo bo) {
        validateUniqueOrgCode(bo);
        normalize(bo);
        BaseOrgUnit entity = MapstructUtils.convert(bo, BaseOrgUnit.class);
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(List<Long> ids) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public String importData(List<BaseOrgUnitVo> rows, Boolean updateSupport) {
        int success = 0;
        int failure = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (BaseOrgUnitVo row : rows) {
            try {
                if (StringUtils.isBlank(row.getOrgCode()) || StringUtils.isBlank(row.getOrgName())) {
                    throw new ServiceException("组织编码和组织名称不能为空");
                }
                BaseOrgUnitVo existing = baseMapper.selectVoOne(new LambdaQueryWrapper<BaseOrgUnit>()
                    .eq(BaseOrgUnit::getOrgCode, row.getOrgCode()), false);
                BaseOrgUnitBo bo = BeanUtil.toBean(row, BaseOrgUnitBo.class);
                if (existing == null) {
                    insertByBo(bo);
                } else if (Boolean.TRUE.equals(updateSupport)) {
                    bo.setId(existing.getId());
                    updateByBo(bo);
                } else {
                    throw new ServiceException("组织编码已存在");
                }
                success++;
            } catch (Exception e) {
                failure++;
                failureMsg.append("<br/>第 ").append(failure).append(" 条：")
                    .append(row.getOrgCode()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failure > 0) {
            throw new ServiceException("组织基础数据导入失败 " + failure + " 条：" + failureMsg);
        }
        return "组织基础数据导入成功 " + success + " 条";
    }

    private LambdaQueryWrapper<BaseOrgUnit> buildQueryWrapper(BaseOrgUnitBo bo) {
        LambdaQueryWrapper<BaseOrgUnit> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(bo.getOrgCode()), BaseOrgUnit::getOrgCode, bo.getOrgCode())
            .like(StringUtils.isNotBlank(bo.getOrgName()), BaseOrgUnit::getOrgName, bo.getOrgName())
            .eq(StringUtils.isNotBlank(bo.getOrgType()), BaseOrgUnit::getOrgType, bo.getOrgType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), BaseOrgUnit::getStatus, bo.getStatus())
            .eq(bo.getParentId() != null, BaseOrgUnit::getParentId, bo.getParentId())
            .orderByAsc(BaseOrgUnit::getSortOrder)
            .orderByDesc(BaseOrgUnit::getCreateTime);
        return wrapper;
    }

    private void validateUniqueOrgCode(BaseOrgUnitBo bo) {
        boolean exists = baseMapper.exists(new LambdaQueryWrapper<BaseOrgUnit>()
            .eq(BaseOrgUnit::getOrgCode, bo.getOrgCode())
            .ne(bo.getId() != null, BaseOrgUnit::getId, bo.getId()));
        if (exists) {
            throw new ServiceException("组织编码已存在");
        }
    }

    private void normalize(BaseOrgUnitBo bo) {
        if (bo.getParentId() == null) {
            bo.setParentId(0L);
        }
        if (bo.getSortOrder() == null) {
            bo.setSortOrder(0);
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
    }
}
