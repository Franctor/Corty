import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, SportRequest, SportResponse } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class SportAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(): Observable<SportResponse[]> {
    return this.http.get<SportResponse[]>(`${this.apiUrl}/sports`);
  }

  create(body: SportRequest): Observable<SportResponse> {
    return this.http.post<SportResponse>(`${this.apiUrl}/sports`, body);
  }

  update(id: number, body: SportRequest): Observable<SportResponse> {
    return this.http.put<SportResponse>(`${this.apiUrl}/sports/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/sports/${id}`);
  }
}
