import { Injectable, signal } from '@angular/core';

export interface GeoPosition {
  lat: number;
  lon: number;
}

@Injectable({ providedIn: 'root' })
export class GeoService {
  readonly position = signal<GeoPosition | null>(null);
  private requested = false;

  requestPosition(): void {
    if (this.requested || !navigator.geolocation) return;
    this.requested = true;
    navigator.geolocation.getCurrentPosition(
      pos => this.position.set({ lat: pos.coords.latitude, lon: pos.coords.longitude }),
      () => this.position.set(null),
      { timeout: 8000, maximumAge: 60_000 },
    );
  }
}
