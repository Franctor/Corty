import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { BookingCreateRequest, BookingCreateResponse, BookingDetailResponse, CancellationResponse, SlotResponse } from '../models/booking.models';
import { HttpParams } from '@angular/common/http';

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

  getAvailableSlots(courtId: number, date: string): Observable<SlotResponse[]> {
    const params = new HttpParams().set('courtId', courtId).set('date', date);
    return this.http.get<SlotResponse[]>(`${this.apiUrl}/bookings/availability`, { params });
  }

  createBooking(request: BookingCreateRequest): Observable<BookingCreateResponse> {
    return this.http.post<BookingCreateResponse>(`${this.apiUrl}/bookings`, request);
  }
}
