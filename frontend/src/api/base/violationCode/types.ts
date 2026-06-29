export interface ViolationCodeVO extends BaseEntity {
  id: number | string;
  violationCode: string;
  violationName: string;
  nature: string;
  category: string;
  violationType: string;
  description: string;
  status: string;
  remark: string;
}

export interface ViolationCodeForm {
  id?: number | string;
  violationCode: string;
  violationName: string;
  nature: string;
  category: string;
  violationType: string;
  description: string;
  status: string;
  remark: string;
}

export interface ViolationCodeQuery extends PageQuery {
  violationCode?: string;
  violationName?: string;
  nature?: string;
  category?: string;
  violationType?: string;
  status?: string;
}
