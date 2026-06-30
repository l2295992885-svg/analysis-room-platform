import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { PersonnelForm, PersonnelQuery, PersonnelVO } from './types';

export function listPersonnel(query: PersonnelQuery): AxiosPromise<PersonnelVO[]> {
  return request({
    url: '/base/personnel/list',
    method: 'get',
    params: query
  });
}

export function getPersonnel(id: string | number): AxiosPromise<PersonnelVO> {
  return request({
    url: `/base/personnel/${id}`,
    method: 'get'
  });
}

export function getPersonnelByJobNo(jobNo: string): AxiosPromise<PersonnelVO> {
  return request({
    url: `/base/personnel/jobNo/${jobNo}`,
    method: 'get'
  });
}

export function addPersonnel(data: PersonnelForm) {
  return request({
    url: '/base/personnel',
    method: 'post',
    data
  });
}

export function updatePersonnel(data: PersonnelForm) {
  return request({
    url: '/base/personnel',
    method: 'put',
    data
  });
}

export function delPersonnel(id: string | number | (string | number)[]) {
  return request({
    url: `/base/personnel/${id}`,
    method: 'delete'
  });
}

export function importPersonnel(data: FormData, updateSupport: boolean) {
  return request({
    url: `/base/personnel/importData?updateSupport=${updateSupport}`,
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
