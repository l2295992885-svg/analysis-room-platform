export interface OrgUnitVO extends BaseEntity {
  id: number | string;
  parentId?: number | string;
  orgCode: string;
  orgName: string;
  orgType: string;
  leaderName: string;
  sortOrder: number;
  status: string;
  remark: string;
}

export interface OrgUnitForm {
  id?: number | string;
  parentId?: number | string;
  orgCode: string;
  orgName: string;
  orgType: string;
  leaderName: string;
  sortOrder: number;
  status: string;
  remark: string;
}

export interface OrgUnitQuery extends PageQuery {
  parentId?: number | string;
  orgCode?: string;
  orgName?: string;
  orgType?: string;
  status?: string;
}
