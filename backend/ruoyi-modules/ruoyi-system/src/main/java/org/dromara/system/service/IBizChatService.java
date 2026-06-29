package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.bo.BizChatBusinessCardBo;
import org.dromara.system.domain.bo.BizChatConversationBo;
import org.dromara.system.domain.bo.BizChatMessageSendBo;
import org.dromara.system.domain.vo.BizChatConversationVo;
import org.dromara.system.domain.vo.BizChatMessageVo;

/**
 * Lightweight chat collaboration service.
 */
public interface IBizChatService {

    TableDataInfo<BizChatConversationVo> queryConversationPage(BizChatConversationBo bo, PageQuery pageQuery);

    TableDataInfo<BizChatMessageVo> queryMessagePage(Long conversationId, PageQuery pageQuery);

    BizChatMessageVo sendTextMessage(Long conversationId, BizChatMessageSendBo bo);

    BizChatMessageVo sendBusinessCard(BizChatBusinessCardBo bo);

    BizChatMessageVo openBusinessCard(Long messageId);
}
