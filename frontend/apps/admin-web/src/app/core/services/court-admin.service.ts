import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, CourtAdminResponse, CourtRequest, Page } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class CourtAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(page = 0, size = 10, search = ''): Observable<Page<CourtAdminResponse>> {
    return this.http.get<Page<CourtAdminResponse>>(`${this.apiUrl}/courts`, {
      params: { page, size, search },
    });
  }

  create(body: CourtRequest): Observable<CourtAdminResponse> {
    return this.http.post<CourtAdminResponse>(`${this.apiUrl}/courts`, body);
  }

  update(id: number, body: CourtRequest): Observable<CourtAdminResponse> {
    return this.http.put<CourtAdminResponse>(`${this.apiUrl}/courts/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/courts/${id}`);
  }

  forceDelete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/courts/${id}/force`);
  }
}
