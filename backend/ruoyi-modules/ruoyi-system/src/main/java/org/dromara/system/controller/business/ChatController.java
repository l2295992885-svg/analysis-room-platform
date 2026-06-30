package org.dromara.system.controller.business;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.BizChatBusinessCardBo;
import org.dromara.system.domain.bo.BizChatConversationBo;
import org.dromara.system.domain.bo.BizChatMessageSendBo;
import org.dromara.system.domain.vo.BizChatConversationVo;
import org.dromara.system.domain.vo.BizChatMessageVo;
import org.dromara.system.service.IBizChatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Lightweight chat collaboration.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    private final IBizChatService chatService;

    @SaCheckPermission("chat:view")
    @GetMapping("/conversations")
    public TableDataInfo<BizChatConversationVo> conversations(@Validated(QueryGroup.class) BizChatConversationBo bo, PageQuery pageQuery) {
        return chatService.queryConversationPage(bo, pageQuery);
    }

    @SaCheckPermission("chat:view")
    @GetMapping("/conversations/{conversationId}/messages")
    public TableDataInfo<BizChatMessageVo> messages(@PathVariable Long conversationId, PageQuery pageQuery) {
        return chatService.queryMessagePage(conversationId, pageQuery);
    }

    @SaCheckPermission("chat:view")
    @Log(title = "聊天消息发送", businessType = BusinessType.INSERT)
    @PostMapping("/conversations/{conversationId}/messages")
    public R<BizChatMessageVo> sendTextMessage(@PathVariable Long conversationId, @Validated @RequestBody BizChatMessageSendBo bo) {
        return R.ok(chatService.sendTextMessage(conversationId, bo));
    }

    @SaCheckPermission("chat:share")
    @Log(title = "聊天业务卡片发送", businessType = BusinessType.INSERT)
    @PostMapping("/business-cards")
    public R<BizChatMessageVo> sendBusinessCard(@Validated @RequestBody BizChatBusinessCardBo bo) {
        return R.ok(chatService.sendBusinessCard(bo));
    }

    @SaCheckPermission("chat:view")
    @Log(title = "聊天业务卡片打开", businessType = BusinessType.OTHER)
    @GetMapping("/business-cards/{messageId}/open")
    public R<BizChatMessageVo> openBusinessCard(@PathVariable Long messageId) {
        return R.ok(chatService.openBusinessCard(messageId));
    }
}
