export interface OrgAdminResponse {
  id: number;
  username: string;
  email: string;
  businessName: string;
  cif: string;
  cityId: number | null;
  city: string | null;
  province: string | null;
}

export interface OrgAdminCreateRequest {
  username: string;
  email: string;
  password: string;
  businessName: string;
  cif: string;
  cityId: number | null;
  verified?: boolean;
}

export interface OrgAdminRequest {
  businessName: string;
  cif: string;
  cityId: number | null;
}
