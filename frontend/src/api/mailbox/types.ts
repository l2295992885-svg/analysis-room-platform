export interface MailboxMessageVO extends BaseEntity {
  id: string | number;
  businessType: string;
  businessId: string;
  batchId: string;
  messageType: string;
  messageTitle: string;
  messageContent: string;
  sourceAction: string;
  businessUrl: string;
  businessPayload: string;
  senderUserId: string | number;
  senderUserName: string;
  senderDeptId: string | number;
  senderDeptName: string;
  receiverUserId: string | number;
  receiverUserName: string;
  receiverDeptId: string | number;
  receiverDeptName: string;
  receiverRoleKey: string;
  readFlag: string;
  readTime: string;
  archiveFlag: string;
  archiveTime: string;
  remark: string;
}

export interface MailboxMessageQuery extends PageQuery {
  businessType?: string;
  businessId?: string;
  batchId?: string;
  messageType?: string;
  messageTitle?: string;
  sourceAction?: string;
  readFlag?: string;
  archiveFlag?: string;
}
