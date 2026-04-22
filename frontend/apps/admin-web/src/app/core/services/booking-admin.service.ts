import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL, BookingAdminResponse, BookingAdminDetailResponse, BookingAdminUpdateRequest, Page } from '@frontend/shared-core';

@Injectable({ providedIn: 'root' })
export class BookingAdminService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAll(page = 0, size = 10, search = ''): Observable<Page<BookingAdminResponse>> {
    return this.http.get<Page<BookingAdminResponse>>(`${this.apiUrl}/admin/bookings`, {
      params: { page, size, search },
    });
  }

  getById(id: number): Observable<BookingAdminDetailResponse> {
    return this.http.get<BookingAdminDetailResponse>(`${this.apiUrl}/admin/bookings/${id}`);
  }

  update(id: number, body: BookingAdminUpdateRequest): Observable<BookingAdminDetailResponse> {
    return this.http.patch<BookingAdminDetailResponse>(`${this.apiUrl}/admin/bookings/${id}`, body);
  }
}
