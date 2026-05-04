import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgTemplateOutlet, UpperCasePipe } from '@angular/common';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import {
  IonContent, IonSpinner, IonModal, IonRange, IonToggle,
} from '@ionic/angular/standalone';
import { HomeService, NearbyCourtResponse, CourtExploreFilters, SportFilterResponse, SurfaceResponse, MediaUrlPipe, GeoService } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-courts',
  templateUrl: './courts.page.html',
  styleUrls: ['./courts.page.scss'],
  standalone: true,
  imports: [
    IonContent, IonSpinner, IonModal, IonRange, IonToggle,
    RouterLink, NgTemplateOutlet, UpperCasePipe,
    PageHeaderComponent,
    LucideAngularModule, MediaUrlPipe,
  ],
})
export class CourtsPage {
  private homeService = inject(HomeService);
  readonly geoService  = inject(GeoService);

  readonly courts        = signal<NearbyCourtResponse[]>([]);
  readonly loading       = signal(false);
  readonly sports        = signal<SportFilterResponse[]>([]);
  readonly surfaces      = signal<SurfaceResponse[]>([]);
  readonly selectedSport = signal<string | undefined>(undefined);
  readonly searchText    = signal('');
  readonly showFilters   = signal(false);

  readonly filterSurface  = signal<string | undefined>(undefined);
  readonly filterCovered  = signal(false);
  readonly filterLighting = signal(false);
  readonly filterMaxPrice        = signal<number | undefined>(undefined);
  readonly filterMaxPriceDisplay = signal<number | undefined>(undefined);
  readonly filterMaxDistanceDisplay = signal<number | undefined>(0.1);
  readonly filterSortBy      = signal<'distance' | 'price'>('distance');
  readonly filterSortDir     = signal<'asc' | 'desc'>('asc');
  readonly filterMaxDistance = signal<number | undefined>(0.1);

  // Escala exponencial: índice 0-15 → km reales
  readonly DISTANCE_STEPS = [0.1, 0.2, 0.5, 1, 2, 3, 5, 8, 10, 15, 20, 30, 50, 75, 100, 200];
  readonly distanceStepIndex = signal<number>(0); // 100m por defecto

  readonly sortOptions: { sortBy: 'distance' | 'price'; sortDir: 'asc' | 'desc'; label: string; icon: string }[] = [
    { sortBy: 'distance', sortDir: 'asc',  label: 'Distancia', icon: 'arrow-up' },
    { sortBy: 'distance', sortDir: 'desc', label: 'Distancia', icon: 'arrow-down' },
    { sortBy: 'price',    sortDir: 'asc',  label: 'Precio',    icon: 'arrow-up' },
    { sortBy: 'price',    sortDir: 'desc', label: 'Precio',    icon: 'arrow-down' },
  ];

  readonly activeFilterCount = computed(() => {
    let n = 0;
    if (this.filterSurface()) n++;
    if (this.filterCovered()) n++;
    if (this.filterLighting()) n++;
    if (this.filterMaxPrice() != null) n++;
    if (this.filterSortBy() !== 'distance' || this.filterSortDir() !== 'asc') n++;
    if (this.distanceStepIndex() > 0 && this.distanceStepIndex() < this.DISTANCE_STEPS.length) n++;
    return n;
  });

  readonly filteredCourts = computed(() => {
    const q = this.searchText().toLowerCase().trim();
    if (!q) return this.courts();
    return this.courts().filter(c =>
      c.name.toLowerCase().includes(q) ||
      c.clubName.toLowerCase().includes(q) ||
      c.sport.toLowerCase().includes(q)
    );
  });

  readonly activeSortLabel = computed(() => {
    const opt = this.sortOptions.find(
      o => o.sortBy === this.filterSortBy() && o.sortDir === this.filterSortDir()
    );
    return opt ? `${opt.label} ${opt.sortDir === 'asc' ? '↑' : '↓'}` : 'Distancia ↑';
  });

  constructor() {
    this.geoService.requestPosition();
    this.loadSports();
    this.loadSurfaces();
    effect(() => {
      if (this.geoService.resolved()) this.loadCourts();
    });
  }

  onSportSelect(sport: string | undefined): void {
    this.selectedSport.set(sport);
    this.loadCourts();
  }

  onSearchInput(event: Event): void {
    this.searchText.set((event.target as HTMLInputElement).value);
  }

  onSortSelect(sortBy: 'distance' | 'price', sortDir: 'asc' | 'desc'): void {
    this.filterSortBy.set(sortBy);
    this.filterSortDir.set(sortDir);
    this.onFilterChange();
  }

  onSurfaceToggle(surface: string): void {
    this.filterSurface.set(this.filterSurface() === surface ? undefined : surface);
    this.onFilterChange();
  }

  onMaxPriceChange(event: CustomEvent): void {
    const v = +event.detail.value;
    const resolved = v >= 100 ? undefined : v;
    this.filterMaxPrice.set(resolved);
    this.filterMaxPriceDisplay.set(resolved);
    this.onFilterChange();
  }

  onMaxPriceInput(event: CustomEvent): void {
    const v = +event.detail.value;
    this.filterMaxPriceDisplay.set(v >= 100 ? undefined : v);
  }

  readonly priceFormatter = (value: number): string =>
    value >= 100 ? '∞' : `${value}€`;

  onFilterChange(): void {
    this.loadCourts();
  }

  onMaxDistanceChange(event: CustomEvent): void {
    const idx = +event.detail.value;
    this.distanceStepIndex.set(idx);
    const km = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : undefined;
    this.filterMaxDistance.set(km);
    this.filterMaxDistanceDisplay.set(km);
    this.onFilterChange();
  }

  onMaxDistanceInput(event: CustomEvent): void {
    const idx = +event.detail.value;
    const km = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : undefined;
    this.filterMaxDistanceDisplay.set(km);
  }

  formatDistanceLabel(km: number): string {
    if (km < 1) return `${km * 1000 | 0} m`;
    if (Number.isInteger(km)) return `${km} km`;
    return `${km} km`;
  }

  readonly distanceFormatter = (idx: number): string => {
    if (idx >= this.DISTANCE_STEPS.length) return '∞';
    const km = this.DISTANCE_STEPS[idx];
    return km < 1 ? `${km * 1000 | 0}m` : `${km}km`;
  };

  onResetFilters(): void {
    this.filterSurface.set(undefined);
    this.filterCovered.set(false);
    this.filterLighting.set(false);
    this.filterMaxPrice.set(undefined);
    this.filterMaxPriceDisplay.set(undefined);
    this.filterMaxDistance.set(0.1);
    this.filterMaxDistanceDisplay.set(0.1);
    this.distanceStepIndex.set(0);
    this.filterSortBy.set('distance');
    this.filterSortDir.set('asc');
    this.showFilters.set(false);
    this.onFilterChange();
  }

  isSortActive(sortBy: 'distance' | 'price', sortDir: 'asc' | 'desc'): boolean {
    return this.filterSortBy() === sortBy && this.filterSortDir() === sortDir;
  }

  private loadCourts(): void {
    this.loading.set(true);
    const filters: CourtExploreFilters = {
      sport:    this.selectedSport(),
      surface:  this.filterSurface(),
      covered:  this.filterCovered() || undefined,
      lighting: this.filterLighting() || undefined,
      maxPrice: this.filterMaxPrice(),
      sortBy:   this.filterSortBy(),
      sortDir:  this.filterSortDir(),
    };
    const pos = this.geoService.position();
    const radius = pos != null ? (this.filterMaxDistance() ?? 5000) : undefined;
    this.homeService.getNearbyCourts(pos?.lat, pos?.lon, filters, radius).subscribe({
      next: courts => { this.courts.set(courts); this.loading.set(false); },
      error: ()    => this.loading.set(false),
    });
  }

  private loadSports(): void {
    this.homeService.getTopSportsForFilter().subscribe({
      next: sports => this.sports.set(sports),
    });
  }

  private loadSurfaces(): void {
    this.homeService.getSurfaces().subscribe({
      next: surfaces => this.surfaces.set(surfaces),
    });
  }
}
