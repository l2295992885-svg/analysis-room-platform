package org.dromara.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BizFileBind;
import org.dromara.system.domain.bo.BizFileBindBo;
import org.dromara.system.domain.vo.BizFileBindVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.mapper.BizFileBindMapper;
import org.dromara.system.service.IBizFileBindService;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BizFileBindServiceImpl implements IBizFileBindService {

    private static final String NORMAL_STATUS = "0";
    private static final String NORMAL_DEL_FLAG = "0";
    private static final String DEFAULT_PERMISSION_SCOPE = "BUSINESS";

    private final BizFileBindMapper baseMapper;
    private final ISysOssService ossService;

    @Override
    public TableDataInfo<BizFileBindVo> queryPageList(BizFileBindBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizFileBind> lqw = buildQueryWrapper(bo);
        Page<BizFileBindVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        result.setRecords(enrich(result.getRecords()));
        return TableDataInfo.build(result);
    }

    @Override
    public List<BizFileBindVo> queryList(BizFileBindBo bo) {
        return enrich(baseMapper.selectVoList(buildQueryWrapper(bo)));
    }

    @Override
    public BizFileBindVo queryById(Long id) {
        return enrich(baseMapper.selectVoById(id));
    }

    @Override
    public BizFileBindVo uploadAndBind(MultipartFile file, BizFileBindBo bo) {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw new ServiceException("上传附件不能为空");
        }
        if (StringUtils.isBlank(bo.getBusinessType())) {
            throw new ServiceException("业务类型不能为空");
        }
        if (StringUtils.isBlank(bo.getBusinessId())) {
            throw new ServiceException("业务ID不能为空");
        }
        SysOssVo oss = ossService.upload(file);
        BizFileBind entity = new BizFileBind();
        entity.setTenantId(LoginHelper.getTenantId());
        entity.setOssId(oss.getOssId());
        entity.setBusinessType(bo.getBusinessType());
        entity.setBusinessId(bo.getBusinessId());
        entity.setBusinessAction(bo.getBusinessAction());
        entity.setAttachmentType(bo.getAttachmentType());
        entity.setPermissionScope(StringUtils.defaultIfBlank(bo.getPermissionScope(), DEFAULT_PERMISSION_SCOPE));
        entity.setUploadUserId(LoginHelper.getUserId());
        entity.setUploadUserName(LoginHelper.getUsername());
        entity.setUploadDeptId(LoginHelper.getDeptId());
        entity.setUploadDeptName(LoginHelper.getDeptName());
        entity.setOriginalName(oss.getOriginalName());
        entity.setFileSuffix(oss.getFileSuffix());
        entity.setFileSize(file.getSize());
        entity.setContentType(file.getContentType());
        entity.setStatus(NORMAL_STATUS);
        entity.setDelFlag(NORMAL_DEL_FLAG);
        entity.setRemark(bo.getRemark());
        baseMapper.insert(entity);
        return queryById(entity.getId());
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws IOException {
        BizFileBindVo attachment = queryById(id);
        if (ObjectUtil.isNull(attachment)) {
            throw new ServiceException("业务附件不存在或已删除");
        }
        ossService.download(attachment.getOssId(), response);
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid && (ids == null || ids.isEmpty())) {
            throw new ServiceException("业务附件ID不能为空");
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    private LambdaQueryWrapper<BizFileBind> buildQueryWrapper(BizFileBindBo bo) {
        LambdaQueryWrapper<BizFileBind> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), BizFileBind::getBusinessType, bo.getBusinessType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), BizFileBind::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessAction()), BizFileBind::getBusinessAction, bo.getBusinessAction());
        lqw.eq(StringUtils.isNotBlank(bo.getAttachmentType()), BizFileBind::getAttachmentType, bo.getAttachmentType());
        lqw.eq(StringUtils.isNotBlank(bo.getPermissionScope()), BizFileBind::getPermissionScope, bo.getPermissionScope());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), BizFileBind::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), BizFileBind::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), BizFileBind::getStatus, bo.getStatus());
        lqw.orderByDesc(BizFileBind::getCreateTime);
        return lqw;
    }

    private List<BizFileBindVo> enrich(List<BizFileBindVo> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        records.forEach(this::enrich);
        return records;
    }

    private BizFileBindVo enrich(BizFileBindVo record) {
        if (record == null || record.getOssId() == null) {
            return record;
        }
        SysOssVo oss = ossService.getById(record.getOssId());
        if (oss != null) {
            record.setFileName(oss.getFileName());
            record.setUrl(oss.getUrl());
            record.setService(oss.getService());
            if (StringUtils.isBlank(record.getOriginalName())) {
                record.setOriginalName(oss.getOriginalName());
            }
            if (StringUtils.isBlank(record.getFileSuffix())) {
                record.setFileSuffix(oss.getFileSuffix());
            }
        }
        return record;
    }
}
