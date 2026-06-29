import request from '@/utils/request';
import { ChatBusinessCardForm, ChatConversationQuery, ChatMessageQuery, ChatMessageSendForm } from './types';

export function listChatConversations(query?: ChatConversationQuery) {
  return request({
    url: '/chat/conversations',
    method: 'get',
    params: query
  });
}

export function listChatMessages(conversationId: string | number, query?: ChatMessageQuery) {
  return request({
    url: `/chat/conversations/${conversationId}/messages`,
    method: 'get',
    params: query
  });
}

export function sendChatMessage(conversationId: string | number, data: ChatMessageSendForm) {
  return request({
    url: `/chat/conversations/${conversationId}/messages`,
    method: 'post',
    data
  });
}

export function sendBusinessCard(data: ChatBusinessCardForm) {
  return request({
    url: '/chat/business-cards',
    method: 'post',
    data
  });
}

export function openBusinessCard(messageId: string | number) {
  return request({
    url: `/chat/business-cards/${messageId}/open`,
    method: 'get'
  });
}
