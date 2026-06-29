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
import org.dromara.system.domain.BaseViolationCode;
import org.dromara.system.domain.bo.BaseViolationCodeBo;
import org.dromara.system.domain.vo.BaseViolationCodeVo;
import org.dromara.system.mapper.BaseViolationCodeMapper;
import org.dromara.system.service.IBaseViolationCodeService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BaseViolationCodeServiceImpl implements IBaseViolationCodeService {

    private final BaseViolationCodeMapper baseMapper;

    @Override
    public TableDataInfo<BaseViolationCodeVo> queryPageList(BaseViolationCodeBo bo, PageQuery pageQuery) {
        Page<BaseViolationCodeVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<BaseViolationCodeVo> queryList(BaseViolationCodeBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public BaseViolationCodeVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public BaseViolationCodeVo queryByCode(String violationCode) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<BaseViolationCode>()
            .eq(BaseViolationCode::getViolationCode, violationCode)
            .eq(BaseViolationCode::getStatus, SystemConstants.NORMAL), false);
    }

    @Override
    public boolean insertByBo(BaseViolationCodeBo bo) {
        validateUniqueCode(bo);
        normalize(bo);
        BaseViolationCode entity = MapstructUtils.convert(bo, BaseViolationCode.class);
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(BaseViolationCodeBo bo) {
        validateUniqueCode(bo);
        normalize(bo);
        BaseViolationCode entity = MapstructUtils.convert(bo, BaseViolationCode.class);
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(List<Long> ids) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public String importData(List<BaseViolationCodeVo> rows, Boolean updateSupport) {
        int success = 0;
        int failure = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (BaseViolationCodeVo row : rows) {
            try {
                if (StringUtils.isBlank(row.getViolationCode()) || StringUtils.isBlank(row.getNature())
                    || StringUtils.isBlank(row.getCategory()) || StringUtils.isBlank(row.getViolationType())) {
                    throw new ServiceException("违标编码、性质、类别、类型不能为空");
                }
                BaseViolationCodeVo existing = baseMapper.selectVoOne(new LambdaQueryWrapper<BaseViolationCode>()
                    .eq(BaseViolationCode::getViolationCode, row.getViolationCode()), false);
                BaseViolationCodeBo bo = BeanUtil.toBean(row, BaseViolationCodeBo.class);
                if (existing == null) {
                    insertByBo(bo);
                } else if (Boolean.TRUE.equals(updateSupport)) {
                    bo.setId(existing.getId());
                    updateByBo(bo);
                } else {
                    throw new ServiceException("违标编码已存在");
                }
                success++;
            } catch (Exception e) {
                failure++;
                failureMsg.append("<br/>第 ").append(failure).append(" 条：")
                    .append(row.getViolationCode()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failure > 0) {
            throw new ServiceException("违标编码导入失败 " + failure + " 条：" + failureMsg);
        }
        return "违标编码导入成功 " + success + " 条";
    }

    private LambdaQueryWrapper<BaseViolationCode> buildQueryWrapper(BaseViolationCodeBo bo) {
        LambdaQueryWrapper<BaseViolationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(bo.getViolationCode()), BaseViolationCode::getViolationCode, bo.getViolationCode())
            .like(StringUtils.isNotBlank(bo.getViolationName()), BaseViolationCode::getViolationName, bo.getViolationName())
            .eq(StringUtils.isNotBlank(bo.getNature()), BaseViolationCode::getNature, bo.getNature())
            .eq(StringUtils.isNotBlank(bo.getCategory()), BaseViolationCode::getCategory, bo.getCategory())
            .eq(StringUtils.isNotBlank(bo.getViolationType()), BaseViolationCode::getViolationType, bo.getViolationType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), BaseViolationCode::getStatus, bo.getStatus())
            .orderByDesc(BaseViolationCode::getCreateTime);
        return wrapper;
    }

    private void validateUniqueCode(BaseViolationCodeBo bo) {
        boolean exists = baseMapper.exists(new LambdaQueryWrapper<BaseViolationCode>()
            .eq(BaseViolationCode::getViolationCode, bo.getViolationCode())
            .ne(bo.getId() != null, BaseViolationCode::getId, bo.getId()));
        if (exists) {
            throw new ServiceException("违标编码已存在");
        }
    }

    private void normalize(BaseViolationCodeBo bo) {
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
    }
}
