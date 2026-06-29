export interface TodoTaskVO extends BaseEntity {
  id: string | number;
  businessType: string;
  businessId: string;
  batchId: string;
  taskTitle: string;
  taskContent: string;
  taskStatus: string;
  currentNode: string;
  priority: string;
  businessUrl: string;
  receiverUserId: string | number;
  receiverUserName: string;
  receiverDeptId: string | number;
  receiverDeptName: string;
  receiverRoleKey: string;
  senderUserId: string | number;
  senderUserName: string;
  senderDeptId: string | number;
  senderDeptName: string;
  dueTime: string;
  finishTime: string;
  finishUserId: string | number;
  finishUserName: string;
  finishComment: string;
  remark: string;
}

export interface TodoTaskQuery extends PageQuery {
  businessType?: string;
  businessId?: string;
  batchId?: string;
  taskTitle?: string;
  taskStatus?: string;
  currentNode?: string;
  priority?: string;
}

export interface TodoTaskCloseForm {
  finishComment?: string;
}
