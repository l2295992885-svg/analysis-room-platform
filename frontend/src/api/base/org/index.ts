import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OrgUnitForm, OrgUnitQuery, OrgUnitVO } from './types';

export function listOrgUnit(query: OrgUnitQuery): AxiosPromise<OrgUnitVO[]> {
  return request({
    url: '/base/org/list',
    method: 'get',
    params: query
  });
}

export function listAllOrgUnit(query?: OrgUnitQuery): AxiosPromise<OrgUnitVO[]> {
  return request({
    url: '/base/org/all',
    method: 'get',
    params: query
  });
}

export function getOrgUnit(id: string | number): AxiosPromise<OrgUnitVO> {
  return request({
    url: `/base/org/${id}`,
    method: 'get'
  });
}

export function addOrgUnit(data: OrgUnitForm) {
  return request({
    url: '/base/org',
    method: 'post',
    data
  });
}

export function updateOrgUnit(data: OrgUnitForm) {
  return request({
    url: '/base/org',
    method: 'put',
    data
  });
}

export function delOrgUnit(id: string | number | (string | number)[]) {
  return request({
    url: `/base/org/${id}`,
    method: 'delete'
  });
}

export function importOrgUnit(data: FormData, updateSupport: boolean) {
  return request({
    url: `/base/org/importData?updateSupport=${updateSupport}`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
