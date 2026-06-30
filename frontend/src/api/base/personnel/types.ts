export interface PersonnelVO extends BaseEntity {
  id: number | string;
  jobNo: string;
  personName: string;
  deptId?: number | string;
  deptName: string;
  workshop: string;
  teamName: string;
  guideGroup: string;
  positionName: string;
  phone: string;
  status: string;
  remark: string;
}

export interface PersonnelForm {
  id?: number | string;
  jobNo: string;
  personName: string;
  deptId?: number | string;
  deptName: string;
  workshop: string;
  teamName: string;
  guideGroup: string;
  positionName: string;
  phone: string;
  status: string;
  remark: string;
}

export interface PersonnelQuery extends PageQuery {
  jobNo?: string;
  personName?: string;
  deptId?: number | string;
  deptName?: string;
  workshop?: string;
  teamName?: string;
  guideGroup?: string;
  status?: string;
}
