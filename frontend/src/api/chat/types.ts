export interface ChatConversationVO {
  id: string | number;
  conversationType: string;
  conversationTitle: string;
  businessType: string;
  businessId: string;
  businessTitle: string;
  businessUrl: string;
  conversationStatus: string;
  lastMessageId: string | number;
  lastMessageContent: string;
  lastMessageTime: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface ChatMessageVO {
  id: string | number;
  conversationId: string | number;
  messageType: string;
  messageContent: string;
  businessType: string;
  businessId: string;
  businessTitle: string;
  businessUrl: string;
  businessPayload: string;
  senderUserId: string | number;
  senderUserName: string;
  senderDeptId: string | number;
  senderDeptName: string;
  remark: string;
  createTime: string;
  updateTime: string;
}

export interface ChatConversationQuery extends PageQuery {
  conversationType?: string;
  conversationTitle?: string;
  businessType?: string;
  businessId?: string;
  conversationStatus?: string;
}

export interface ChatMessageQuery extends PageQuery {}

export interface ChatMessageSendForm {
  messageContent: string;
}

export interface ChatBusinessCardForm {
  conversationId?: string | number;
  businessType: string;
  businessId: string;
  businessTitle: string;
  businessUrl?: string;
  businessPayload?: string;
  messageContent?: string;
}
