export interface UserAdminResponse {
  id: number;
  username: string;
  email: string;
  role: string;
  authorities: string[];
  enabled: boolean;
  locked: boolean;
  creationDate: string;
}

export interface UserStatusRequest {
  enabled: boolean;
  locked: boolean;
}

export interface UserRoleRequest {
  role: string;
  authorities: string[];
}
