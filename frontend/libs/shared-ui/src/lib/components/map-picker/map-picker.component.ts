import {
  Component, forwardRef, input, output, signal, computed,
  ChangeDetectionStrategy, ElementRef, ViewChild, AfterViewInit,
  OnDestroy, inject, NgZone,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, FormControl } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';
import * as L from 'leaflet';
import { debounceTime, distinctUntilChanged, switchMap, catchError, of } from 'rxjs';

export interface MapPickerValue {
  lat: number;
  lng: number;
  cityId: number | null;
  cityName: string;
  address: string;
}

interface NominatimResult {
  lat: string;
  lon: string;
  display_name: string;
  address: {
    city?: string;
    town?: string;
    village?: string;
    municipality?: string;
    state?: string;
    country?: string;
    road?: string;
    house_number?: string;
  };
}

const DEFAULT_LAT = 40.416775;
const DEFAULT_LNG = -3.703790;
const DEFAULT_ZOOM = 6;
const SELECTED_ZOOM = 13;

@Component({
  selector: 'ui-map-picker',
  templateUrl: 'map-picker.component.html',
  styleUrl: 'map-picker.component.scss',
  standalone: true,
  imports: [ReactiveFormsModule, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MapPickerComponent), multi: true }],
})
export class MapPickerComponent implements ControlValueAccessor, AfterViewInit, OnDestroy {
  private http = inject(HttpClient);
  private zone = inject(NgZone);
  private apiUrl = inject(API_URL);

  readonly label = input<string>('Ubicación');

  readonly cityIdChange  = output<number | null>();
  readonly addressChange = output<string>();

  @ViewChild('mapContainer') mapContainer!: ElementRef<HTMLDivElement>;

  readonly searchControl   = new FormControl('');
  readonly searchResults   = signal<NominatimResult[]>([]);
  readonly searching       = signal(false);
  readonly selectedLocation = signal<{ lat: number; lng: number; display: string } | null>(null);

  private map: L.Map | null = null;
  private marker: L.Marker | null = null;
  private onChange: (value: MapPickerValue | null) => void = () => {};
  protected onTouched: () => void = () => {};
  private currentValue: MapPickerValue | null = null;
  private cityCache = new Map<string, number | null>();

  readonly displayValue = computed(() => this.selectedLocation()?.display ?? '');

  ngAfterViewInit(): void {
    // Use setTimeout so the modal finishes its CSS transition before Leaflet measures the container
    setTimeout(() => {
      this.zone.runOutsideAngular(() => this.initMap());
    }, 150);
    this.setupSearch();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private initMap(): void {
    this.map = L.map(this.mapContainer.nativeElement, {
      center: [DEFAULT_LAT, DEFAULT_LNG],
      zoom: DEFAULT_ZOOM,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);

    // Force Leaflet to recalculate tile positions after the container is fully visible
    this.map.invalidateSize();

    this.map.on('click', (event: L.LeafletMouseEvent) => {
      this.zone.run(() => this.onMapClick(event.latlng.lat, event.latlng.lng));
    });

    if (this.currentValue) {
      this.placeMarker(this.currentValue.lat, this.currentValue.lng, this.currentValue.address, false);
      this.map.setView([this.currentValue.lat, this.currentValue.lng], SELECTED_ZOOM);
    }
  }

  private setupSearch(): void {
    this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap((query) => {
        const trimmed = (query ?? '').trim();
        if (trimmed.length < 3) {
          this.searchResults.set([]);
          return of([]);
        }
        this.searching.set(true);
        return this.http.get<NominatimResult[]>('https://nominatim.openstreetmap.org/search', {
          params: {
            q: trimmed,
            format: 'json',
            addressdetails: '1',
            limit: '5',
            countrycodes: 'es',
          },
        }).pipe(catchError(() => of([])));
      }),
    ).subscribe((results: NominatimResult[]) => {
      this.searching.set(false);
      this.searchResults.set(results);
    });
  }

  private onMapClick(lat: number, lng: number): void {
    this.searching.set(true);
    this.http.get<NominatimResult>('https://nominatim.openstreetmap.org/reverse', {
      params: { lat: String(lat), lon: String(lng), format: 'json', addressdetails: '1' },
    }).pipe(catchError(() => of(null))).subscribe((result) => {
      this.searching.set(false);
      const address  = result ? this.buildAddress(result) : `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
      const cityName = result ? this.extractCityName(result) : '';
      this.placeMarker(lat, lng, address, true);
      this.resolveCityAndEmit(lat, lng, address, cityName);
    });
  }

  selectResult(result: NominatimResult): void {
    const lat      = parseFloat(result.lat);
    const lng      = parseFloat(result.lon);
    const address  = this.buildAddress(result);
    const cityName = this.extractCityName(result);
    this.searchResults.set([]);
    this.searchControl.setValue('', { emitEvent: false });
    this.placeMarker(lat, lng, address, true);
    this.map?.setView([lat, lng], SELECTED_ZOOM);
    this.resolveCityAndEmit(lat, lng, address, cityName);
  }

  private placeMarker(lat: number, lng: number, display: string, pan: boolean): void {
    this.zone.runOutsideAngular(() => {
      if (this.marker) this.marker.remove();
      const icon = L.divIcon({
        className: '',
        html: `<div class="map-picker__pin"></div>`,
        iconSize: [24, 24],
        iconAnchor: [12, 24],
      });
      this.marker = L.marker([lat, lng], { icon }).addTo(this.map!);
      if (pan) this.map?.panTo([lat, lng]);
    });
    this.zone.run(() => this.selectedLocation.set({ lat, lng, display }));
  }

  private resolveCityAndEmit(lat: number, lng: number, address: string, cityName: string): void {
    const cacheKey = cityName.toLowerCase();
    if (this.cityCache.has(cacheKey)) {
      this.emitValue(lat, lng, address, cityName, this.cityCache.get(cacheKey)!);
      return;
    }
    // Nominatim puts articles first ("La Guardia") but DB stores them last ("Guardia de Jaén, La")
    // Strip leading article to get the core name for searching
    const searchName = cityName.replace(/^(La|El|Los|Las|L')\s+/i, '').trim();

    this.http.get<{ idCity: number; label: string }[]>(`${this.apiUrl}/location/cities`, { params: { name: searchName } })
      .pipe(catchError(() => of([])))
      .subscribe((cities) => {
        const exactMatch = cities.find(
          (city) => city.label.toLowerCase() === cityName.toLowerCase()
            || city.label.toLowerCase() === searchName.toLowerCase()
        );
        const cityId = exactMatch ? exactMatch.idCity : (cities.length > 0 ? cities[0].idCity : null);
        this.cityCache.set(cacheKey, cityId);
        this.emitValue(lat, lng, address, cityName, cityId);
      });
  }

  private emitValue(lat: number, lng: number, address: string, cityName: string, cityId: number | null): void {
    const value: MapPickerValue = { lat, lng, cityId, cityName, address };
    this.currentValue = value;
    this.onChange(value);
    this.onTouched();
    this.cityIdChange.emit(cityId);
    this.addressChange.emit(address);
  }

  private buildAddress(result: NominatimResult): string {
    const parts: string[] = [];
    const addr = result.address;
    if (addr.road) {
      parts.push(addr.house_number ? `${addr.road} ${addr.house_number}` : addr.road);
    }
    const city = this.extractCityName(result);
    if (city) parts.push(city);
    if (addr.state) parts.push(addr.state);
    return parts.join(', ') || result.display_name;
  }

  private extractCityName(result: NominatimResult): string {
    const addr = result.address;
    // Priority: city (capital/large city) > municipality > town > village
    return addr.city ?? addr.municipality ?? addr.town ?? addr.village ?? '';
  }

  writeValue(value: MapPickerValue | null): void {
    this.currentValue = value;
    if (value) {
      this.selectedLocation.set({ lat: value.lat, lng: value.lng, display: value.address });
      if (this.map) {
        this.placeMarker(value.lat, value.lng, value.address, false);
        this.map.setView([value.lat, value.lng], SELECTED_ZOOM);
      }
    } else {
      this.selectedLocation.set(null);
    }
  }

  registerOnChange(fn: (value: MapPickerValue | null) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
}
