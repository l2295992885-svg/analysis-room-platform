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
import org.dromara.system.domain.BaseImportTemplate;
import org.dromara.system.domain.bo.BaseImportTemplateBo;
import org.dromara.system.domain.vo.BaseImportTemplateVo;
import org.dromara.system.mapper.BaseImportTemplateMapper;
import org.dromara.system.service.IBaseImportTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BaseImportTemplateServiceImpl implements IBaseImportTemplateService {

    private final BaseImportTemplateMapper baseMapper;

    @Override
    public TableDataInfo<BaseImportTemplateVo> queryPageList(BaseImportTemplateBo bo, PageQuery pageQuery) {
        Page<BaseImportTemplateVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(page);
    }

    @Override
    public List<BaseImportTemplateVo> queryList(BaseImportTemplateBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public BaseImportTemplateVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public boolean insertByBo(BaseImportTemplateBo bo) {
        validateUniqueTemplateCode(bo);
        normalize(bo);
        BaseImportTemplate entity = MapstructUtils.convert(bo, BaseImportTemplate.class);
        return baseMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateByBo(BaseImportTemplateBo bo) {
        validateUniqueTemplateCode(bo);
        normalize(bo);
        BaseImportTemplate entity = MapstructUtils.convert(bo, BaseImportTemplate.class);
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean deleteWithValidByIds(List<Long> ids) {
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public String importData(List<BaseImportTemplateVo> rows, Boolean updateSupport) {
        int success = 0;
        int failure = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (BaseImportTemplateVo row : rows) {
            try {
                if (StringUtils.isBlank(row.getTemplateCode()) || StringUtils.isBlank(row.getTemplateName())
                    || StringUtils.isBlank(row.getBusinessType())) {
                    throw new ServiceException("模板编码、模板名称、业务类型不能为空");
                }
                BaseImportTemplateVo existing = baseMapper.selectVoOne(new LambdaQueryWrapper<BaseImportTemplate>()
                    .eq(BaseImportTemplate::getTemplateCode, row.getTemplateCode()), false);
                BaseImportTemplateBo bo = BeanUtil.toBean(row, BaseImportTemplateBo.class);
                if (existing == null) {
                    insertByBo(bo);
                } else if (Boolean.TRUE.equals(updateSupport)) {
                    bo.setId(existing.getId());
                    updateByBo(bo);
                } else {
                    throw new ServiceException("模板编码已存在");
                }
                success++;
            } catch (Exception e) {
                failure++;
                failureMsg.append("<br/>第 ").append(failure).append(" 条：")
                    .append(row.getTemplateCode()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failure > 0) {
            throw new ServiceException("导入模板数据导入失败 " + failure + " 条：" + failureMsg);
        }
        return "导入模板数据导入成功 " + success + " 条";
    }

    private LambdaQueryWrapper<BaseImportTemplate> buildQueryWrapper(BaseImportTemplateBo bo) {
        LambdaQueryWrapper<BaseImportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(bo.getTemplateCode()), BaseImportTemplate::getTemplateCode, bo.getTemplateCode())
            .like(StringUtils.isNotBlank(bo.getTemplateName()), BaseImportTemplate::getTemplateName, bo.getTemplateName())
            .eq(StringUtils.isNotBlank(bo.getBusinessType()), BaseImportTemplate::getBusinessType, bo.getBusinessType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), BaseImportTemplate::getStatus, bo.getStatus())
            .orderByDesc(BaseImportTemplate::getCreateTime);
        return wrapper;
    }

    private void validateUniqueTemplateCode(BaseImportTemplateBo bo) {
        boolean exists = baseMapper.exists(new LambdaQueryWrapper<BaseImportTemplate>()
            .eq(BaseImportTemplate::getTemplateCode, bo.getTemplateCode())
            .ne(bo.getId() != null, BaseImportTemplate::getId, bo.getId()));
        if (exists) {
            throw new ServiceException("模板编码已存在");
        }
    }

    private void normalize(BaseImportTemplateBo bo) {
        if (StringUtils.isBlank(bo.getVersionNo())) {
            bo.setVersionNo("1.0");
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(SystemConstants.NORMAL);
        }
    }
}
