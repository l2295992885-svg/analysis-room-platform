export interface FileAttachmentVO extends BaseEntity {
  id: string | number;
  ossId: string | number;
  businessType: string;
  businessId: string;
  businessAction: string;
  attachmentType: string;
  permissionScope: string;
  uploadUserId: string | number;
  uploadUserName: string;
  uploadDeptId: string | number;
  uploadDeptName: string;
  originalName: string;
  fileSuffix: string;
  fileSize: number;
  contentType: string;
  status: string;
  fileName: string;
  url: string;
  service: string;
}

export interface FileAttachmentQuery extends PageQuery {
  businessType?: string;
  businessId?: string;
  businessAction?: string;
  attachmentType?: string;
  permissionScope?: string;
  originalName?: string;
  fileSuffix?: string;
  status?: string;
}

export interface FileAttachmentUploadForm {
  businessType: string;
  businessId: string;
  businessAction?: string;
  attachmentType?: string;
  permissionScope?: string;
  remark?: string;
  file?: File;
}
