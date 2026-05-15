import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, BookingAdminResponse, BookingAdminDetailResponse, BookingAdminUpdateRequest, BookingPresencialRequest, CourtAdminResponse, Page } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class BookingAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(page = 0, size = 10, search = '', sort = 'date', dir = 'desc'): Observable<Page<BookingAdminResponse>> {
    return this.http.get<Page<BookingAdminResponse>>(`${this.apiUrl}/admin/bookings`, {
      params: { page, size, search, sort, dir },
    });
  }

  getById(id: number): Observable<BookingAdminDetailResponse> {
    return this.http.get<BookingAdminDetailResponse>(`${this.apiUrl}/admin/bookings/${id}`);
  }

  create(body: BookingPresencialRequest): Observable<BookingAdminDetailResponse> {
    return this.http.post<BookingAdminDetailResponse>(`${this.apiUrl}/admin/bookings`, body);
  }

  update(id: number, body: BookingAdminUpdateRequest): Observable<BookingAdminDetailResponse> {
    return this.http.patch<BookingAdminDetailResponse>(`${this.apiUrl}/admin/bookings/${id}`, body);
  }

  getCourtsByClub(clubId: number): Observable<CourtAdminResponse[]> {
    return this.http.get<CourtAdminResponse[]>(`${this.apiUrl}/courts/by-club/${clubId}`);
  }
}
