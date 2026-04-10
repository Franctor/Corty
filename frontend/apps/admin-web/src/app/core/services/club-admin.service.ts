import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL, ClubRequest, ClubResponse } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class ClubAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(): Observable<ClubResponse[]> {
    return this.http.get<ClubResponse[]>(`${this.apiUrl}/clubs`);
  }

  create(body: ClubRequest): Observable<ClubResponse> {
    return this.http.post<ClubResponse>(`${this.apiUrl}/clubs`, body);
  }

  update(id: number, body: ClubRequest): Observable<ClubResponse> {
    return this.http.put<ClubResponse>(`${this.apiUrl}/clubs/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clubs/${id}`);
  }

  forceDelete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clubs/${id}/force`);
  }

  getCities(): Observable<{ id: number; label: string }[]> {
    return this.http.get<{ idCity: number; label: string }[]>(`${this.apiUrl}/location/cities`).pipe(
      map(cities => cities.map(c => ({ id: c.idCity, label: c.label })))
    );
  }
}
