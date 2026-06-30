import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ViolationCodeForm, ViolationCodeQuery, ViolationCodeVO } from './types';

export function listViolationCode(query: ViolationCodeQuery): AxiosPromise<ViolationCodeVO[]> {
  return request({
    url: '/base/violationCode/list',
    method: 'get',
    params: query
  });
}

export function getViolationCode(id: string | number): AxiosPromise<ViolationCodeVO> {
  return request({
    url: `/base/violationCode/${id}`,
    method: 'get'
  });
}

export function getViolationCodeByCode(violationCode: string): AxiosPromise<ViolationCodeVO> {
  return request({
    url: `/base/violationCode/code/${violationCode}`,
    method: 'get'
  });
}

export function addViolationCode(data: ViolationCodeForm) {
  return request({
    url: '/base/violationCode',
    method: 'post',
    data
  });
}

export function updateViolationCode(data: ViolationCodeForm) {
  return request({
    url: '/base/violationCode',
    method: 'put',
    data
  });
}

export function delViolationCode(id: string | number | (string | number)[]) {
  return request({
    url: `/base/violationCode/${id}`,
    method: 'delete'
  });
}

export function importViolationCode(data: FormData, updateSupport: boolean) {
  return request({
    url: `/base/violationCode/importData?updateSupport=${updateSupport}`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
