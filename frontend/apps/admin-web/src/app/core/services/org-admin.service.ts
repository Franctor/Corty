import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, OrgAdminCreateRequest, OrgAdminRequest, OrgAdminResponse } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class OrgAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);
  private base = `${this.apiUrl}/organizations`;

  getAll(): Observable<OrgAdminResponse[]> {
    return this.http.get<OrgAdminResponse[]>(this.base);
  }

  create(request: OrgAdminCreateRequest): Observable<OrgAdminResponse> {
    return this.http.post<OrgAdminResponse>(this.base, request);
  }

  createBatch(file: File): Observable<OrgAdminResponse[]> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<OrgAdminResponse[]>(`${this.base}/csv`, form);
  }

  update(id: number, request: OrgAdminRequest): Observable<OrgAdminResponse> {
    return this.http.put<OrgAdminResponse>(`${this.base}/${id}`, request);
  }
}
