export interface DailyViolationRecordVO extends BaseEntity {
  recordId: string | number;
  batchId?: string | number;
  reportDate: string;
  violationDate?: string;
  violationTime?: string;
  sequenceNo?: string;
  violationCode: string;
  violationNatureSnapshot?: string;
  violationCategorySnapshot?: string;
  violationTypeSnapshot?: string;
  proposedAssessmentContent: string;
  responsibleDeptId?: string | number;
  responsibleDeptNameSnapshot?: string;
  employeeNo?: string;
  employeeNameSnapshot?: string;
  locomotive?: string;
  trainNo?: string;
  location?: string;
  timeSegment?: string;
  ticketNo?: string;
  guideDriver?: string;
  guideGroup?: string;
  partyMemberFlag?: string;
  abcdAssignment?: string;
  handlingAssessment?: string;
  issuingDept?: string;
  currentStatus: string;
  validationStatus?: string;
  validationMessage?: string;
}

export interface DailyViolationRecordQuery extends PageQuery {
  batchId?: string | number;
  reportDate?: string;
  violationDateStart?: string;
  violationDateEnd?: string;
  responsibleDeptId?: string | number;
  responsibleDeptName?: string;
  employeeNo?: string;
  employeeName?: string;
  violationCode?: string;
  currentStatus?: string;
}

export interface DailyViolationResultVO extends BaseEntity {
  resultId: string | number;
  recordId: string | number;
  batchId?: string | number;
  resultVersion?: number;
  resultStatus?: string;
  included?: string;
  resultSnapshot?: string;
  archivedBy?: string | number;
  archivedUserName?: string;
  archivedTime?: string;
  correctedFromResultId?: string | number;
  correctReason?: string;
}

export interface DailyViolationResultQuery extends DailyViolationRecordQuery {
  resultId?: string | number;
  recordId?: string | number;
  resultVersion?: number;
  resultStatus?: string;
  included?: string;
  archivedTimeStart?: string;
  archivedTimeEnd?: string;
}

export interface DailyViolationResultCorrectForm {
  included?: string;
  resultStatus?: string;
  correctReason: string;
}

export interface DailyViolationResultCompareVO {
  recordId: string | number;
  sourceVersion: number;
  targetVersion: number;
  diffs: Array<{
    field: string;
    sourceValue?: unknown;
    targetValue?: unknown;
  }>;
}

export interface DailyViolationAttachmentVO extends BaseEntity {
  id: string | number;
  ossId?: string | number;
  businessType?: string;
  businessId?: string | number;
  businessAction?: string;
  attachmentType?: string;
  permissionScope?: string;
  uploadUserId?: string | number;
  uploadUserName?: string;
  uploadDeptId?: string | number;
  uploadDeptName?: string;
  originalName?: string;
  fileSuffix?: string;
  fileSize?: number;
  contentType?: string;
  status?: string;
  remark?: string;
}

export interface DailyViolationFlowLogVO {
  flowId: string | number;
  recordId?: string | number;
  batchId?: string | number;
  actionCode?: string;
  beforeStatus?: string;
  afterStatus?: string;
  operatorId?: string | number;
  operatorNameSnapshot?: string;
  operatorDeptSnapshot?: string;
  operatorRoleSnapshot?: string;
  opinion?: string;
  attachmentRefs?: string;
  changedFieldsJson?: string;
  traceId?: string;
  createTime?: string;
}

export interface DailyViolationFeedbackVO extends BaseEntity {
  feedbackId: string | number;
  recordId?: string | number;
  batchId?: string | number;
  reasonType?: string;
  reasonDescription?: string;
  feedbackDeptId?: string | number;
  feedbackDeptNameSnapshot?: string;
  feedbackUserId?: string | number;
  feedbackUserNameSnapshot?: string;
  attachmentRefs?: string;
  feedbackStatus?: string;
}

export interface DailyViolationRecordForm {
  reportDate?: string;
  violationDate?: string;
  violationTime?: string;
  sequenceNo?: string;
  violationCode?: string;
  proposedAssessmentContent?: string;
  responsibleDeptId?: string | number;
  responsibleDeptName?: string;
  employeeNo?: string;
  employeeName?: string;
  locomotive?: string;
  trainNo?: string;
  location?: string;
  timeSegment?: string;
  ticketNo?: string;
  guideDriver?: string;
  guideGroup?: string;
  partyMemberFlag?: string;
  abcdAssignment?: string;
  handlingAssessment?: string;
  issuingDept?: string;
}

export interface DailyViolationActionForm {
  opinion?: string;
  workshopId?: string | number;
  workshopName?: string;
  teamId?: string | number;
  teamName?: string;
  guideGroupId?: string | number;
  guideGroupName?: string;
  reasonType?: string;
  reasonDescription?: string;
  attachmentIds?: Array<string | number>;
  finalOpinion?: string;
  finalDecision?: string;
  cancelReason?: string;
}

export interface DailyViolationImportBatchVO extends BaseEntity {
  importBatchId: string | number;
  reportDate?: string;
  originalFileId?: string | number;
  originalFileName?: string;
  sheetName?: string;
  titleText?: string;
  headerRowIndex?: number;
  totalRows: number;
  validRows: number;
  warningRows: number;
  invalidRows: number;
  importStatus: string;
}

export interface DailyViolationImportRowVO extends BaseEntity {
  rowId: string | number;
  importBatchId: string | number;
  rowNo: number;
  sequenceNo?: string;
  violationCode?: string;
  proposedAssessmentContent?: string;
  violationNature?: string;
  violationCategory?: string;
  violationType?: string;
  responsibleDeptName?: string;
  responsiblePersonName?: string;
  employeeNo?: string;
  parsedViolationDate?: string;
  parsedViolationTime?: string;
  parsedLocomotive?: string;
  parsedTrainNo?: string;
  candidatePersonNames?: string;
  validationStatus: 'VALID' | 'NEED_CONFIRM' | 'INVALID';
  validationMessage?: string;
  confirmStatus: 'UNCONFIRMED' | 'CONFIRMED' | 'REJECTED';
  generatedRecordId?: string | number;
}
