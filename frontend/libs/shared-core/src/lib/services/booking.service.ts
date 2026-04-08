import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { BookingDetailResponse, CancellationResponse } from '../models/booking.models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getBookingDetail(id: number): Observable<BookingDetailResponse> {
    return this.http.get<BookingDetailResponse>(`${this.apiUrl}/bookings/${id}`);
  }

  cancelBooking(id: number): Observable<CancellationResponse> {
    return this.http.delete<CancellationResponse>(`${this.apiUrl}/bookings/${id}`);
  }

  leaveBooking(id: number): Observable<CancellationResponse> {
    return this.http.delete<CancellationResponse>(`${this.apiUrl}/bookings/${id}/leave`);
  }
}
