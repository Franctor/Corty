export interface PlayerAdminResponse {
  id: number;
  username: string;
  email: string;
  name: string;
  surname: string;
  phone: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  birthDate: string;
  biography: string | null;
  avatarUrl: string | null;
  karma: number;
  cityId: number | null;
  city: string | null;
  province: string | null;
}

export interface PlayerAdminCreateRequest {
  username: string;
  email: string;
  password: string;
  name: string;
  surname: string;
  phone: string;
  gender: string;
  birthDate: string;
  biography: string | null;
  avatarUrl: string | null;
  cityId: number | null;
  verified?: boolean;
}

export interface PlayerAdminRequest {
  name: string;
  surname: string;
  phone: string;
  gender: string;
  birthDate: string;
  biography: string | null;
  avatarUrl: string | null;
  karma: number;
  cityId: number | null;
}
