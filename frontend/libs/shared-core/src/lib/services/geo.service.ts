import { Injectable, signal } from '@angular/core';

export interface GeoPosition {
  lat: number;
  lon: number;
}

@Injectable({ providedIn: 'root' })
export class GeoService {
  readonly position = signal<GeoPosition | null>(null);
  readonly resolved = signal(false);
  private requested = false;

  requestPosition(): void {
    if (this.requested) return;
    this.requested = true;
    if (!navigator.geolocation) {
      this.resolved.set(true);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.position.set({ lat: pos.coords.latitude, lon: pos.coords.longitude });
        this.resolved.set(true);
      },
      () => {
        this.position.set(null);
        this.resolved.set(true);
      },
      { timeout: 8000, maximumAge: 60_000 },
    );
  }
}
