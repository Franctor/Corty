import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '@frontend/shared-core';
import { PlayerAdminCreateRequest, PlayerAdminRequest, PlayerAdminResponse } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class PlayerAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);
  private base = `${this.apiUrl}/players/admin`;

  getAll(): Observable<PlayerAdminResponse[]> {
    return this.http.get<PlayerAdminResponse[]>(this.base);
  }

  create(request: PlayerAdminCreateRequest): Observable<PlayerAdminResponse> {
    return this.http.post<PlayerAdminResponse>(this.base, request);
  }

  createBatch(file: File): Observable<PlayerAdminResponse[]> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<PlayerAdminResponse[]>(`${this.base}/csv`, form);
  }

  update(id: number, request: PlayerAdminRequest): Observable<PlayerAdminResponse> {
    return this.http.put<PlayerAdminResponse>(`${this.base}/${id}`, request);
  }
}
