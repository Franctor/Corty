import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { BookingCreateRequest, BookingCreateResponse, BookingDetailResponse, BookingListItemResponse, BookingResultRequest, BookingStatus, CancellationResponse, JoinPaymentCheckResponse, JoinRequestResponse, PublicBookingResponse, SlotResponse } from '../models/booking.models';
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

  getPublicBookings(params: {
    lat?: number; lon?: number; radiusKm?: number;
    sport?: string; dateFrom?: string; dateTo?: string;
    levelMin?: number; levelMax?: number; limit?: number;
  }): Observable<PublicBookingResponse[]> {
    let p = new HttpParams();
    if (params.lat != null)      p = p.set('lat', params.lat);
    if (params.lon != null)      p = p.set('lon', params.lon);
    if (params.radiusKm != null) p = p.set('radiusKm', params.radiusKm);
    if (params.sport)            p = p.set('sport', params.sport);
    if (params.dateFrom)          p = p.set('dateFrom', params.dateFrom);
    if (params.dateTo)            p = p.set('dateTo', params.dateTo);
    if (params.levelMin != null)  p = p.set('levelMin', params.levelMin);
    if (params.levelMax != null)  p = p.set('levelMax', params.levelMax);
    if (params.limit != null)     p = p.set('limit', params.limit);
    return this.http.get<PublicBookingResponse[]>(`${this.apiUrl}/bookings/public`, { params: p });
  }

  joinPaymentCheck(bookingId: number): Observable<JoinPaymentCheckResponse> {
    return this.http.get<JoinPaymentCheckResponse>(`${this.apiUrl}/bookings/public/${bookingId}/join/payment-check`);
  }

  sendJoinRequest(bookingId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bookings/public/${bookingId}/join`, {});
  }

  cancelJoinRequest(bookingId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/public/${bookingId}/join`);
  }

  getJoinRequests(bookingId: number): Observable<JoinRequestResponse[]> {
    return this.http.get<JoinRequestResponse[]>(`${this.apiUrl}/bookings/public/${bookingId}/requests`);
  }

  acceptJoinRequest(bookingId: number, requestId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bookings/public/${bookingId}/requests/${requestId}/accept`, {});
  }

  rejectJoinRequest(bookingId: number, requestId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bookings/public/${bookingId}/requests/${requestId}/reject`, {});
  }

  getMyBookings(statuses?: BookingStatus[]): Observable<BookingListItemResponse[]> {
    let params = new HttpParams();
    statuses?.forEach(s => { params = params.append('status', s); });
    return this.http.get<BookingListItemResponse[]>(`${this.apiUrl}/bookings/mine`, { params });
  }

  registerResult(bookingId: number, request: BookingResultRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/bookings/${bookingId}/result`, request);
  }
}
