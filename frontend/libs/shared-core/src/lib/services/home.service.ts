import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { NextBookingResponse, NearbyCourtResponse, RecentActivityResponse } from '../models/booking.models';
import { SportFilterResponse } from '../models/sport.models';

@Injectable({ providedIn: 'root' })
export class HomeService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getNextBooking(): Observable<NextBookingResponse | null> {
    return this.http.get<NextBookingResponse | null>(`${this.apiUrl}/bookings/next`);
  }

  getNearbyCourts(lat?: number, lon?: number, sport?: string): Observable<NearbyCourtResponse[]> {
    let params = new HttpParams();
    if (lat != null) params = params.set('lat', lat);
    if (lon != null) params = params.set('lon', lon);
    if (sport) params = params.set('sport', sport);
    return this.http.get<NearbyCourtResponse[]>(`${this.apiUrl}/courts/nearby`, { params });
  }

  getRecentActivity(): Observable<RecentActivityResponse[]> {
    return this.http.get<RecentActivityResponse[]>(`${this.apiUrl}/bookings/recent`);
  }

  getTopSportsForFilter(): Observable<SportFilterResponse[]> {
    return this.http.get<SportFilterResponse[]>(`${this.apiUrl}/sports/filters`);
  }
}
