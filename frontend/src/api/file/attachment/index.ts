import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { FileAttachmentQuery, FileAttachmentVO } from './types';

export const listFileAttachment = (query: FileAttachmentQuery): AxiosPromise<FileAttachmentVO[]> => {
  return request({
    url: '/file/attachment/list',
    method: 'get',
    params: query
  });
};

export const getFileAttachment = (id: string | number): AxiosPromise<FileAttachmentVO> => {
  return request({
    url: `/file/attachment/${id}`,
    method: 'get'
  });
};

export const uploadFileAttachment = (data: FormData): AxiosPromise<FileAttachmentVO> => {
  return request({
    url: '/file/attachment/upload',
    method: 'post',
    data,
    headers: {
      repeatSubmit: false
    }
  });
};

export const delFileAttachment = (id: string | number | Array<string | number>) => {
  return request({
    url: `/file/attachment/${id}`,
    method: 'delete'
  });
};
