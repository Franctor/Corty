import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, SurfaceRequest, SurfaceResponse } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class SurfaceAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(): Observable<SurfaceResponse[]> {
    return this.http.get<SurfaceResponse[]>(`${this.apiUrl}/surfaces`);
  }

  create(body: SurfaceRequest): Observable<SurfaceResponse> {
    return this.http.post<SurfaceResponse>(`${this.apiUrl}/surfaces`, body);
  }

  update(id: number, body: SurfaceRequest): Observable<SurfaceResponse> {
    return this.http.put<SurfaceResponse>(`${this.apiUrl}/surfaces/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/surfaces/${id}`);
  }
}
