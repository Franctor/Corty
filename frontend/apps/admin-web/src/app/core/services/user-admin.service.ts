import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, UserAdminResponse, UserRoleRequest, UserStatusRequest } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(): Observable<UserAdminResponse[]> {
    return this.http.get<UserAdminResponse[]>(`${this.apiUrl}/users`);
  }

  updateStatus(id: number, body: UserStatusRequest): Observable<UserAdminResponse> {
    return this.http.patch<UserAdminResponse>(`${this.apiUrl}/users/${id}/status`, body);
  }

  updateRoleAndAuthorities(id: number, body: UserRoleRequest): Observable<UserAdminResponse> {
    return this.http.patch<UserAdminResponse>(`${this.apiUrl}/users/${id}/role`, body);
  }

  getRoles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/users/roles`);
  }

  getAuthorities(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/users/authorities`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`);
  }
}
