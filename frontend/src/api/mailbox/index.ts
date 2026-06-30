import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { MailboxMessageQuery, MailboxMessageVO } from './types';

export const listMailboxMessages = (query: MailboxMessageQuery): AxiosPromise<MailboxMessageVO[]> => {
  return request({
    url: '/mailbox/messages',
    method: 'get',
    params: query
  });
};

export const getMailboxMessage = (id: string | number): AxiosPromise<MailboxMessageVO> => {
  return request({
    url: `/mailbox/messages/${id}`,
    method: 'get'
  });
};

export const markMailboxMessageRead = (id: string | number) => {
  return request({
    url: `/mailbox/messages/${id}/read`,
    method: 'post'
  });
};

export const archiveMailboxMessage = (id: string | number) => {
  return request({
    url: `/mailbox/messages/${id}/archive`,
    method: 'post'
  });
};
