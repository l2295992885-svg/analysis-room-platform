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
import org.dromara.system.domain.BizTask;
import org.dromara.system.domain.bo.BizTaskBo;
import org.dromara.system.domain.bo.BizTaskCloseBo;
import org.dromara.system.domain.vo.BizTaskVo;
import org.dromara.system.mapper.BizTaskMapper;
import org.dromara.system.service.IBizTaskService;
import org.dromara.system.service.IDailyViolationService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * Common todo task service implementation.
 */
@RequiredArgsConstructor
@Service
public class BizTaskServiceImpl implements IBizTaskService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String DAILY_VIOLATION = "DAILY_VIOLATION";

    private final BizTaskMapper baseMapper;
    private final IDailyViolationService dailyViolationService;

    @Override
    public TableDataInfo<BizTaskVo> queryMyPendingPage(BizTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizTask> lqw = buildQueryWrapper(bo);
        if (StringUtils.isBlank(bo.getTaskStatus())) {
            lqw.eq(BizTask::getTaskStatus, STATUS_PENDING);
        }
        appendReceiverScope(lqw);
        lqw.orderByDesc(BizTask::getCreateTime);
        Page<BizTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<BizTaskVo> queryDonePage(BizTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizTask> lqw = buildQueryWrapper(bo);
        lqw.in(BizTask::getTaskStatus, STATUS_DONE, STATUS_CLOSED);
        if (!LoginHelper.isSuperAdmin()) {
            lqw.eq(BizTask::getFinishUserId, LoginHelper.getUserId());
        }
        lqw.orderByDesc(BizTask::getFinishTime);
        Page<BizTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public BizTaskVo queryById(Long id) {
        BizTask task = selectTaskEntity(id);
        checkTaskScope(task);
        checkBusinessDataScope(task);
        return baseMapper.selectVoById(id);
    }

    @Override
    public BizTaskVo openTask(Long id) {
        return queryById(id);
    }

    @Override
    public Boolean closeTask(Long id, BizTaskCloseBo bo) {
        BizTask task = selectTaskEntity(id);
        checkTaskScope(task);
        if (!STATUS_PENDING.equals(task.getTaskStatus())) {
            throw new ServiceException("只有待处理状态的待办可以关闭");
        }
        task.setTaskStatus(STATUS_CLOSED);
        task.setFinishTime(new Date());
        task.setFinishUserId(LoginHelper.getUserId());
        task.setFinishUserName(LoginHelper.getUsername());
        task.setFinishComment(bo == null ? null : bo.getFinishComment());
        return baseMapper.updateById(task) > 0;
    }

    private BizTask selectTaskEntity(Long id) {
        BizTask task = baseMapper.selectById(id);
        if (ObjectUtil.isNull(task)) {
            throw new ServiceException("待办不存在或已删除");
        }
        return task;
    }

    private LambdaQueryWrapper<BizTask> buildQueryWrapper(BizTaskBo bo) {
        LambdaQueryWrapper<BizTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), BizTask::getBusinessType, bo.getBusinessType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), BizTask::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getBatchId()), BizTask::getBatchId, bo.getBatchId());
        lqw.like(StringUtils.isNotBlank(bo.getTaskTitle()), BizTask::getTaskTitle, bo.getTaskTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskStatus()), BizTask::getTaskStatus, bo.getTaskStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getCurrentNode()), BizTask::getCurrentNode, bo.getCurrentNode());
        lqw.eq(StringUtils.isNotBlank(bo.getPriority()), BizTask::getPriority, bo.getPriority());
        lqw.eq(ObjectUtil.isNotNull(bo.getSenderUserId()), BizTask::getSenderUserId, bo.getSenderUserId());
        return lqw;
    }

    private void appendReceiverScope(LambdaQueryWrapper<BizTask> lqw) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        Set<String> roles = currentRoleKeys();
        lqw.and(wrapper -> {
            wrapper.eq(BizTask::getReceiverUserId, userId);
            if (deptId != null) {
                wrapper.or().eq(BizTask::getReceiverDeptId, deptId);
            }
            if (CollUtil.isNotEmpty(roles)) {
                wrapper.or().in(BizTask::getReceiverRoleKey, roles);
            }
        });
    }

    private void checkTaskScope(BizTask task) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Long userId = LoginHelper.getUserId();
        Long deptId = LoginHelper.getDeptId();
        Set<String> roles = currentRoleKeys();
        boolean matched = ObjectUtil.equals(task.getReceiverUserId(), userId)
            || ObjectUtil.equals(task.getReceiverDeptId(), deptId)
            || (StringUtils.isNotBlank(task.getReceiverRoleKey()) && roles.contains(task.getReceiverRoleKey()))
            || ObjectUtil.equals(task.getFinishUserId(), userId);
        if (!matched) {
            throw new ServiceException("无权访问该待办", HttpStatus.FORBIDDEN);
        }
    }

    private void checkBusinessDataScope(BizTask task) {
        if (DAILY_VIOLATION.equals(task.getBusinessType()) && StringUtils.isNotBlank(task.getBusinessId())) {
            dailyViolationService.queryRecord(Long.valueOf(task.getBusinessId()));
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
