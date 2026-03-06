export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  // Step 1 - Account
  username: string;
  email: string;
  password: string;
  // Step 2 - Profile
  name: string;
  surname: string;
  phone: string;
  gender: Gender;
  birthDate: string; // ISO: 'YYYY-MM-DD'
  biography?: string;
  cityId: number;
  avatarUrl?: string | null;
}

export interface AuthResponse {
  token: string;
}