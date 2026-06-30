package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BizMessage;
import org.dromara.system.domain.bo.BizMessageBo;
import org.dromara.system.domain.vo.BizMessageVo;
import org.dromara.system.mapper.BizMessageMapper;
import org.dromara.system.service.IBizMessageService;
import org.dromara.system.service.IDailyViolationService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Common mailbox message service implementation.
 */
@RequiredArgsConstructor
@Service
public class BizMessageServiceImpl implements IBizMessageService {

    private static final String FLAG_YES = "1";
    private static final String DAILY_VIOLATION = "DAILY_VIOLATION";

    private final BizMessageMapper baseMapper;
    private final IDailyViolationService dailyViolationService;

    @Override
    public TableDataInfo<BizMessageVo> queryPageList(BizMessageBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizMessage> lqw = buildQueryWrapper(bo);
        appendReceiverScope(lqw);
        lqw.orderByDesc(BizMessage::getCreateTime);
        Page<BizMessageVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public BizMessageVo queryById(Long id) {
        BizMessage message = selectMessageEntity(id);
        checkMessageScope(message);
        checkBusinessDataScope(message);
        return baseMapper.selectVoById(id);
    }

    @Override
    public Boolean markRead(Long id) {
        BizMessage message = selectMessageEntity(id);
        checkMessageScope(message);
        if (FLAG_YES.equals(message.getReadFlag())) {
            return true;
        }
        message.setReadFlag(FLAG_YES);
        message.setReadTime(new Date());
        return baseMapper.updateById(message) > 0;
    }

    @Override
    public Boolean archive(Long id) {
        BizMessage message = selectMessageEntity(id);
        checkMessageScope(message);
        if (!FLAG_YES.equals(message.getReadFlag())) {
            message.setReadFlag(FLAG_YES);
            message.setReadTime(new Date());
        }
        message.setArchiveFlag(FLAG_YES);
        message.setArchiveTime(new Date());
        return baseMapper.updateById(message) > 0;
    }

    private BizMessage selectMessageEntity(Long id) {
        BizMessage message = baseMapper.selectById(id);
        if (ObjectUtil.isNull(message)) {
            throw new ServiceException("信箱消息不存在或已删除");
        }
        return message;
    }

    private LambdaQueryWrapper<BizMessage> buildQueryWrapper(BizMessageBo bo) {
        LambdaQueryWrapper<BizMessage> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), BizMessage::getBusinessType, bo.getBusinessType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), BizMessage::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getBatchId()), BizMessage::getBatchId, bo.getBatchId());
        lqw.eq(StringUtils.isNotBlank(bo.getMessageType()), BizMessage::getMessageType, bo.getMessageType());
        lqw.like(StringUtils.isNotBlank(bo.getMessageTitle()), BizMessage::getMessageTitle, bo.getMessageTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceAction()), BizMessage::getSourceAction, bo.getSourceAction());
        lqw.eq(StringUtils.isNotBlank(bo.getReadFlag()), BizMessage::getReadFlag, bo.getReadFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getArchiveFlag()), BizMessage::getArchiveFlag, bo.getArchiveFlag());
        return lqw;
    }

    private void appendReceiverScope(LambdaQueryWrapper<BizMessage> lqw) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        Set<String> roles = currentRoleKeys();
        lqw.and(wrapper -> {
            wrapper.eq(BizMessage::getReceiverUserId, userId);
            if (deptId != null) {
                wrapper.or().eq(BizMessage::getReceiverDeptId, deptId);
            }
            if (CollUtil.isNotEmpty(roles)) {
                wrapper.or().in(BizMessage::getReceiverRoleKey, roles);
            }
        });
    }

    private void checkMessageScope(BizMessage message) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        Set<String> roles = currentRoleKeys();
        boolean matched = ObjectUtil.equals(message.getReceiverUserId(), userId)
            || ObjectUtil.equals(message.getReceiverDeptId(), deptId)
            || (StringUtils.isNotBlank(message.getReceiverRoleKey()) && roles.contains(message.getReceiverRoleKey()));
        if (!matched) {
            throw new ServiceException("无权访问该信箱消息", HttpStatus.FORBIDDEN);
        }
    }

    private void checkBusinessDataScope(BizMessage message) {
        if (DAILY_VIOLATION.equals(message.getBusinessType()) && StringUtils.isNotBlank(message.getBusinessId())) {
            dailyViolationService.queryRecord(Long.valueOf(message.getBusinessId()));
        }
    }

    private Set<String> currentRoleKeys() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || CollUtil.isEmpty(loginUser.getRolePermission())) {
            return Collections.emptySet();
        }
        return loginUser.getRolePermission();
    }
}
