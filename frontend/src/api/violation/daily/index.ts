import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  DailyViolationActionForm,
  DailyViolationAttachmentVO,
  DailyViolationFeedbackVO,
  DailyViolationFlowLogVO,
  DailyViolationImportBatchVO,
  DailyViolationImportRowVO,
  DailyViolationRecordForm,
  DailyViolationRecordQuery,
  DailyViolationRecordVO,
  DailyViolationResultCompareVO,
  DailyViolationResultCorrectForm,
  DailyViolationResultQuery,
  DailyViolationResultVO
} from './types';

export const listDailyViolationRecords = (query: DailyViolationRecordQuery): AxiosPromise<DailyViolationRecordVO[]> => {
  return request({
    url: '/violation/daily/records',
    method: 'get',
    params: query
  });
};

export const getDailyViolationRecord = (recordId: string | number): AxiosPromise<DailyViolationRecordVO> => {
  return request({
    url: `/violation/daily/records/${recordId}`,
    method: 'get'
  });
};

export const listDailyViolationLogs = (recordId: string | number): AxiosPromise<DailyViolationFlowLogVO[]> => {
  return request({
    url: `/violation/daily/records/${recordId}/logs`,
    method: 'get'
  });
};

export const listDailyViolationFeedbacks = (recordId: string | number): AxiosPromise<DailyViolationFeedbackVO[]> => {
  return request({
    url: `/violation/daily/records/${recordId}/feedbacks`,
    method: 'get'
  });
};

export const addDailyViolationRecord = (data: DailyViolationRecordForm) => {
  return request({
    url: '/violation/daily/records',
    method: 'post',
    data
  });
};

export const updateDailyViolationRecord = (recordId: string | number, data: DailyViolationRecordForm) => {
  return request({
    url: `/violation/daily/records/${recordId}`,
    method: 'put',
    data
  });
};

export const runDailyViolationAction = (recordId: string | number, action: string, data?: DailyViolationActionForm) => {
  return request({
    url: `/violation/daily/records/${recordId}/${action}`,
    method: 'post',
    data: data || {}
  });
};

export const listDailyViolationResults = (query: DailyViolationResultQuery): AxiosPromise<DailyViolationResultVO[]> => {
  return request({
    url: '/violation/daily/results',
    method: 'get',
    params: query
  });
};

export const getDailyViolationResult = (resultId: string | number): AxiosPromise<DailyViolationResultVO> => {
  return request({
    url: `/violation/daily/results/${resultId}`,
    method: 'get'
  });
};

export const listDailyViolationResultVersions = (resultId: string | number): AxiosPromise<DailyViolationResultVO[]> => {
  return request({
    url: `/violation/daily/results/${resultId}/versions`,
    method: 'get'
  });
};

export const compareDailyViolationResultVersions = (
  resultId: string | number,
  sourceVersion: number,
  targetVersion: number
): AxiosPromise<DailyViolationResultCompareVO> => {
  return request({
    url: `/violation/daily/results/${resultId}/versions/compare`,
    method: 'get',
    params: { sourceVersion, targetVersion }
  });
};

export const correctDailyViolationResult = (resultId: string | number, data: DailyViolationResultCorrectForm): AxiosPromise<DailyViolationResultVO> => {
  return request({
    url: `/violation/daily/results/${resultId}/correct`,
    method: 'post',
    data
  });
};

export const listDailyViolationAttachments = (recordId: string | number): AxiosPromise<DailyViolationAttachmentVO[]> => {
  return request({
    url: `/violation/daily/records/${recordId}/attachments`,
    method: 'get'
  });
};

export const uploadDailyViolationAttachment = (recordId: string | number, data: FormData): AxiosPromise<DailyViolationAttachmentVO> => {
  return request({
    url: `/violation/daily/records/${recordId}/attachments`,
    method: 'post',
    data
  });
};

export const downloadDailyViolationAttachment = (recordId: string | number, attachmentId: string | number): Promise<Blob> => {
  return request({
    url: `/violation/daily/records/${recordId}/attachments/${attachmentId}/download`,
    method: 'get',
    responseType: 'blob'
  });
};

export const importDailyViolationExcel = (data: FormData): AxiosPromise<DailyViolationImportBatchVO> => {
  return request({
    url: '/violation/daily/imports',
    method: 'post',
    data
  });
};

export const getDailyViolationImport = (importBatchId: string | number): AxiosPromise<DailyViolationImportBatchVO> => {
  return request({
    url: `/violation/daily/imports/${importBatchId}`,
    method: 'get'
  });
};

export const listDailyViolationImportRows = (
  importBatchId: string | number,
  query: PageQuery & { validationStatus?: string; confirmStatus?: string }
): AxiosPromise<DailyViolationImportRowVO[]> => {
  return request({
    url: `/violation/daily/imports/${importBatchId}/rows`,
    method: 'get',
    params: query
  });
};

export const updateDailyViolationImportRow = (importBatchId: string | number, rowId: string | number, data: Partial<DailyViolationImportRowVO>) => {
  return request({
    url: `/violation/daily/imports/${importBatchId}/rows/${rowId}`,
    method: 'put',
    data
  });
};

export const validateDailyViolationImport = (importBatchId: string | number): AxiosPromise<DailyViolationImportBatchVO> => {
  return request({
    url: `/violation/daily/imports/${importBatchId}/validate`,
    method: 'post'
  });
};

export const submitDailyViolationImportRows = (importBatchId: string | number, rowIds: Array<string | number>) => {
  return request({
    url: `/violation/daily/imports/${importBatchId}/submit`,
    method: 'post',
    data: { rowIds }
  });
};
