import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CityResponse, ProvinceResponse } from '../models/location.models';
import { API_URL } from '../tokens/api.tokens'

@Injectable({ providedIn: 'root' })
export class LocationService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  // Fetches all provinces ordered alphabetically
  getProvinces(): Observable<ProvinceResponse[]> {
    return this.http.get<ProvinceResponse[]>(`${this.apiUrl}/location/provinces`);
  }

  getCityById(cityId: number): Observable<CityResponse> {
    return this.http.get<CityResponse>(`${this.apiUrl}/location/cities/${cityId}`);
  }

  // Fetches cities filtered by province code
  getCitiesByProvince(provinceCode: string): Observable<CityResponse[]> {
    return this.http.get<CityResponse[]>(`${this.apiUrl}/location/provinces/${provinceCode}/cities`);
  }
}