import {
  Component, forwardRef, inject, signal, computed, OnInit,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { LocationService } from '@frontend/shared-core';
import { SelectComponent, SelectOption } from '../select/select.component';

@Component({
  selector: 'ui-location-select',
  templateUrl: 'location-select.component.html',
  styleUrl: 'location-select.component.scss',
  standalone: true,
  imports: [SelectComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => LocationSelectComponent),
      multi: true,
    },
  ],
})
export class LocationSelectComponent implements ControlValueAccessor, OnInit {
  private locationService = inject(LocationService);

  readonly provinceOptions = signal<SelectOption<string>[]>([]);
  readonly cityOptions = signal<SelectOption<number>[]>([]);
  readonly loadingCities = signal(false);

  readonly selectedProvinceCode = signal<string | null>(null);
  readonly selectedCityId = signal<number | null>(null);

  readonly cityDisabled = computed(() => !this.selectedProvinceCode());

  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};
  private pendingCityId: number | null = null;

  ngOnInit(): void {
    this.locationService.getProvinces().subscribe(provinces => {
      this.provinceOptions.set(provinces.map(p => ({ value: p.code, label: p.label })));
      if (this.pendingCityId !== null) {
        this.resolvePendingCity(this.pendingCityId);
      }
    });
  }

  onProvinceChange(code: string | null): void {
    this.selectedProvinceCode.set(code);
    this.selectedCityId.set(null);
    this.cityOptions.set([]);
    this.onChange(null);
    this.onTouched();
    if (!code) return;
    this.loadingCities.set(true);
    this.locationService.getCitiesByProvince(code).subscribe({
      next: (cities) => {
        this.cityOptions.set(cities.map(c => ({ value: c.idCity, label: c.label })));
        this.loadingCities.set(false);
      },
      error: () => this.loadingCities.set(false),
    });
  }

  onCityChange(cityId: number | null): void {
    this.selectedCityId.set(cityId);
    this.onChange(cityId);
    this.onTouched();
  }

  private resolvePendingCity(cityId: number): void {
    this.pendingCityId = null;
    this.locationService.getCityById(cityId).subscribe(city => {
      this.selectedProvinceCode.set(city.provinceCode);
      this.loadingCities.set(true);
      this.locationService.getCitiesByProvince(city.provinceCode).subscribe({
        next: (cities) => {
          this.cityOptions.set(cities.map(c => ({ value: c.idCity, label: c.label })));
          this.selectedCityId.set(cityId);
          this.loadingCities.set(false);
        },
        error: () => this.loadingCities.set(false),
      });
    });
  }

  // CVA
  writeValue(cityId: number | null): void {
    if (!cityId) {
      this.selectedProvinceCode.set(null);
      this.selectedCityId.set(null);
      return;
    }
    if (this.provinceOptions().length > 0) {
      this.resolvePendingCity(cityId);
    } else {
      this.pendingCityId = cityId;
    }
  }

  registerOnChange(fn: (v: number | null) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
}
