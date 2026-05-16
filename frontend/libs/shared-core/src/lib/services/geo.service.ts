import { Injectable, signal } from '@angular/core';
import { Geolocation } from '@capacitor/geolocation';
import { Capacitor } from '@capacitor/core';

export interface GeoPosition {
  lat: number;
  lon: number;
}

@Injectable({ providedIn: 'root' })
export class GeoService {
  readonly position = signal<GeoPosition | null>(null);
  readonly resolved = signal(false);
  private requested = false;

  async requestPosition(): Promise<void> {
    if (this.requested) return;
    this.requested = true;

    try {
      if (Capacitor.isNativePlatform()) {
        const permission = await Geolocation.requestPermissions();
        if (permission.location !== 'granted' && permission.coarseLocation !== 'granted') {
          this.resolved.set(true);
          return;
        }
      }

      const pos = await Geolocation.getCurrentPosition({ timeout: 8000, maximumAge: 60_000 });
      this.position.set({ lat: pos.coords.latitude, lon: pos.coords.longitude });
    } catch {
      this.position.set(null);
    } finally {
      this.resolved.set(true);
    }
  }
}
