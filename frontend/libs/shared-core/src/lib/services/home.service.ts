import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../tokens/api.tokens';
import { NextBookingResponse, NearbyCourtResponse, RecentActivityResponse, CourtExploreFilters } from '../models/booking.models';
import { SportFilterResponse } from '../models/sport.models';
import { SurfaceResponse } from '../models/surface.models';

@Injectable({ providedIn: 'root' })
export class HomeService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getNextBooking(): Observable<NextBookingResponse | null> {
    return this.http.get<NextBookingResponse | null>(`${this.apiUrl}/bookings/next`);
  }

  getNearbyCourts(lat?: number, lon?: number, filters: CourtExploreFilters = {}, radiusKm?: number): Observable<NearbyCourtResponse[]> {
    let params = new HttpParams();
    if (lat != null) params = params.set('lat', lat);
    if (lon != null) params = params.set('lon', lon);
    if (filters.sport) params = params.set('sport', filters.sport);
    if (filters.surface) params = params.set('surface', filters.surface);
    if (filters.covered != null) params = params.set('covered', filters.covered);
    if (filters.lighting != null) params = params.set('lighting', filters.lighting);
    if (filters.maxPrice != null) params = params.set('maxPrice', filters.maxPrice);
    if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
    if (filters.sortDir) params = params.set('sortDir', filters.sortDir);
    if (radiusKm != null) params = params.set('radiusKm', radiusKm);
    return this.http.get<NearbyCourtResponse[]>(`${this.apiUrl}/courts/nearby`, { params });
  }

  getRecentActivity(): Observable<RecentActivityResponse[]> {
    return this.http.get<RecentActivityResponse[]>(`${this.apiUrl}/bookings/recent`);
  }

  getTopSportsForFilter(): Observable<SportFilterResponse[]> {
    return this.http.get<SportFilterResponse[]>(`${this.apiUrl}/sports/filters`);
  }

  getSurfaces(): Observable<SurfaceResponse[]> {
    return this.http.get<SurfaceResponse[]>(`${this.apiUrl}/surfaces`);
  }
}
