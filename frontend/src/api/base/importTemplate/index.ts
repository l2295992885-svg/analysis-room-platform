import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ImportTemplateForm, ImportTemplateQuery, ImportTemplateVO } from './types';

export function listImportTemplate(query: ImportTemplateQuery): AxiosPromise<ImportTemplateVO[]> {
  return request({
    url: '/base/importTemplate/list',
    method: 'get',
    params: query
  });
}

export function getImportTemplate(id: string | number): AxiosPromise<ImportTemplateVO> {
  return request({
    url: `/base/importTemplate/${id}`,
    method: 'get'
  });
}

export function addImportTemplate(data: ImportTemplateForm) {
  return request({
    url: '/base/importTemplate',
    method: 'post',
    data
  });
}

export function updateImportTemplate(data: ImportTemplateForm) {
  return request({
    url: '/base/importTemplate',
    method: 'put',
    data
  });
}

export function delImportTemplate(id: string | number | (string | number)[]) {
  return request({
    url: `/base/importTemplate/${id}`,
    method: 'delete'
  });
}

export function importImportTemplate(data: FormData, updateSupport: boolean) {
  return request({
    url: `/base/importTemplate/importData?updateSupport=${updateSupport}`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
