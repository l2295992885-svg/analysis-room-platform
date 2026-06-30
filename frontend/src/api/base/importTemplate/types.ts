export interface ImportTemplateVO extends BaseEntity {
  id: number | string;
  templateCode: string;
  templateName: string;
  businessType: string;
  versionNo: string;
  fileName: string;
  fileOssId?: number | string;
  status: string;
  remark: string;
}

export interface ImportTemplateForm {
  id?: number | string;
  templateCode: string;
  templateName: string;
  businessType: string;
  versionNo: string;
  fileName: string;
  fileOssId?: number | string;
  status: string;
  remark: string;
}

export interface ImportTemplateQuery extends PageQuery {
  templateCode?: string;
  templateName?: string;
  businessType?: string;
  status?: string;
}
