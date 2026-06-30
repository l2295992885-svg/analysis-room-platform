package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.BizChatConversation;
import org.dromara.system.domain.BizChatConversationMember;
import org.dromara.system.domain.BizChatMessage;
import org.dromara.system.domain.bo.BizChatBusinessCardBo;
import org.dromara.system.domain.bo.BizChatConversationBo;
import org.dromara.system.domain.bo.BizChatMessageSendBo;
import org.dromara.system.domain.vo.BizChatConversationVo;
import org.dromara.system.domain.vo.BizChatMessageVo;
import org.dromara.system.mapper.BizChatConversationMapper;
import org.dromara.system.mapper.BizChatConversationMemberMapper;
import org.dromara.system.mapper.BizChatMessageMapper;
import org.dromara.system.service.IBizChatService;
import org.dromara.system.service.IDailyViolationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Lightweight chat collaboration service implementation.
 */
@RequiredArgsConstructor
@Service
public class BizChatServiceImpl implements IBizChatService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_BUSINESS_CARD = "BUSINESS_CARD";
    private static final String DAILY_VIOLATION = "DAILY_VIOLATION";

    private final BizChatConversationMapper conversationMapper;
    private final BizChatConversationMemberMapper memberMapper;
    private final BizChatMessageMapper messageMapper;
    private final IDailyViolationService dailyViolationService;

    @Override
    public TableDataInfo<BizChatConversationVo> queryConversationPage(BizChatConversationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<BizChatConversation> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getConversationType()), BizChatConversation::getConversationType, bo.getConversationType());
        lqw.like(StringUtils.isNotBlank(bo.getConversationTitle()), BizChatConversation::getConversationTitle, bo.getConversationTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), BizChatConversation::getBusinessType, bo.getBusinessType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), BizChatConversation::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getConversationStatus()), BizChatConversation::getConversationStatus, bo.getConversationStatus());
        if (!LoginHelper.isSuperAdmin()) {
            List<Long> conversationIds = selectCurrentUserConversationIds();
            if (CollUtil.isEmpty(conversationIds)) {
                return TableDataInfo.build(Collections.emptyList());
            }
            lqw.in(BizChatConversation::getId, conversationIds);
        }
        lqw.orderByDesc(BizChatConversation::getLastMessageTime);
        Page<BizChatConversationVo> result = conversationMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<BizChatMessageVo> queryMessagePage(Long conversationId, PageQuery pageQuery) {
        selectConversationEntity(conversationId);
        checkConversationMember(conversationId);
        LambdaQueryWrapper<BizChatMessage> lqw = Wrappers.lambdaQuery();
        lqw.eq(BizChatMessage::getConversationId, conversationId);
        lqw.orderByAsc(BizChatMessage::getCreateTime);
        Page<BizChatMessageVo> result = messageMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizChatMessageVo sendTextMessage(Long conversationId, BizChatMessageSendBo bo) {
        BizChatConversation conversation = selectConversationEntity(conversationId);
        checkActiveConversation(conversation);
        checkConversationMember(conversationId);
        BizChatMessage message = buildBaseMessage(conversationId, MESSAGE_TYPE_TEXT);
        message.setMessageContent(bo.getMessageContent());
        messageMapper.insert(message);
        updateConversationLastMessage(conversation, message);
        return messageMapper.selectVoById(message.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizChatMessageVo sendBusinessCard(BizChatBusinessCardBo bo) {
        BizChatConversation conversation = selectConversationEntity(bo.getConversationId());
        checkActiveConversation(conversation);
        checkConversationMember(bo.getConversationId());
        BizChatMessage message = buildBaseMessage(bo.getConversationId(), MESSAGE_TYPE_BUSINESS_CARD);
        message.setMessageContent(StringUtils.blankToDefault(bo.getMessageContent(), "业务卡片：" + bo.getBusinessTitle()));
        message.setBusinessType(bo.getBusinessType());
        message.setBusinessId(bo.getBusinessId());
        message.setBusinessTitle(bo.getBusinessTitle());
        message.setBusinessUrl(bo.getBusinessUrl());
        message.setBusinessPayload(bo.getBusinessPayload());
        messageMapper.insert(message);
        updateConversationLastMessage(conversation, message);
        return messageMapper.selectVoById(message.getId());
    }

    @Override
    public BizChatMessageVo openBusinessCard(Long messageId) {
        BizChatMessage message = selectMessageEntity(messageId);
        if (!MESSAGE_TYPE_BUSINESS_CARD.equals(message.getMessageType())) {
            throw new ServiceException("该消息不是业务卡片");
        }
        checkConversationMember(message.getConversationId());
        checkBusinessDataScope(message);
        return messageMapper.selectVoById(messageId);
    }

    private BizChatMessage buildBaseMessage(Long conversationId, String messageType) {
        BizChatMessage message = new BizChatMessage();
        message.setId(IdGeneratorUtil.nextLongId());
        message.setConversationId(conversationId);
        message.setMessageType(messageType);
        message.setSenderUserId(LoginHelper.getUserId());
        message.setSenderUserName(LoginHelper.getUsername());
        message.setSenderDeptId(LoginHelper.getDeptId());
        message.setSenderDeptName(LoginHelper.getDeptName());
        message.setDelFlag("0");
        message.setCreateDept(LoginHelper.getDeptId());
        message.setCreateBy(LoginHelper.getUserId());
        message.setCreateTime(new Date());
        return message;
    }

    private void updateConversationLastMessage(BizChatConversation conversation, BizChatMessage message) {
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageContent(message.getMessageContent());
        conversation.setLastMessageTime(message.getCreateTime());
        conversation.setUpdateBy(LoginHelper.getUserId());
        conversation.setUpdateTime(new Date());
        conversationMapper.updateById(conversation);
    }

    private BizChatConversation selectConversationEntity(Long conversationId) {
        BizChatConversation conversation = conversationMapper.selectById(conversationId);
        if (ObjectUtil.isNull(conversation)) {
            throw new ServiceException("聊天会话不存在或已删除");
        }
        return conversation;
    }

    private BizChatMessage selectMessageEntity(Long messageId) {
        BizChatMessage message = messageMapper.selectById(messageId);
        if (ObjectUtil.isNull(message)) {
            throw new ServiceException("聊天消息不存在或已删除");
        }
        return message;
    }

    private void checkActiveConversation(BizChatConversation conversation) {
        if (!STATUS_ACTIVE.equals(conversation.getConversationStatus())) {
            throw new ServiceException("只有启用状态的会话可以发送消息");
        }
    }

    private void checkConversationMember(Long conversationId) {
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        Long count = memberMapper.selectCount(Wrappers.lambdaQuery(BizChatConversationMember.class)
            .eq(BizChatConversationMember::getConversationId, conversationId)
            .eq(BizChatConversationMember::getMemberUserId, LoginHelper.getUserId()));
        if (count == null || count <= 0) {
            throw new ServiceException("无权访问该聊天会话", HttpStatus.FORBIDDEN);
        }
    }

    private void checkBusinessDataScope(BizChatMessage message) {
        if (DAILY_VIOLATION.equals(message.getBusinessType()) && StringUtils.isNotBlank(message.getBusinessId())) {
            dailyViolationService.queryRecord(Long.valueOf(message.getBusinessId()));
        }
    }

    private List<Long> selectCurrentUserConversationIds() {
        return memberMapper.selectList(Wrappers.lambdaQuery(BizChatConversationMember.class)
                .eq(BizChatConversationMember::getMemberUserId, LoginHelper.getUserId()))
            .stream()
            .map(BizChatConversationMember::getConversationId)
            .distinct()
            .toList();
    }
}
