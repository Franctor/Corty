import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgTemplateOutlet, UpperCasePipe, DecimalPipe } from '@angular/common';
import {
  IonContent, IonHeader, IonToolbar,
  IonButtons, IonBackButton, IonSpinner, IonModal, IonRange, IonToggle,
} from '@ionic/angular/standalone';
import { HomeService, NearbyCourtResponse, CourtExploreFilters, SportFilterResponse, MediaUrlPipe } from '@frontend/shared-core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-courts',
  templateUrl: './courts.page.html',
  styleUrls: ['./courts.page.scss'],
  standalone: true,
  imports: [
    IonContent, IonHeader, IonToolbar,
    IonButtons, IonBackButton, IonSpinner, IonModal, IonRange, IonToggle,
    RouterLink, NgTemplateOutlet, UpperCasePipe, DecimalPipe,
    LucideAngularModule, MediaUrlPipe,
  ],
})
export class CourtsPage {
  private homeService = inject(HomeService);

  readonly courts        = signal<NearbyCourtResponse[]>([]);
  readonly loading       = signal(false);
  readonly sports        = signal<SportFilterResponse[]>([]);
  readonly selectedSport = signal<string | undefined>(undefined);
  readonly searchText    = signal('');
  readonly showFilters   = signal(false);

  readonly filterSurface  = signal<string | undefined>(undefined);
  readonly filterCovered  = signal(false);
  readonly filterLighting = signal(false);
  readonly filterMaxPrice = signal<number | undefined>(undefined);
  readonly filterSortBy   = signal<'distance' | 'price'>('distance');
  readonly filterSortDir  = signal<'asc' | 'desc'>('asc');

  readonly surfaces = ['Cemento', 'Tierra batida', 'Hierba', 'Moqueta', 'Madera', 'Césped artificial'];

  readonly sortOptions: { sortBy: 'distance' | 'price'; sortDir: 'asc' | 'desc'; label: string }[] = [
    { sortBy: 'distance', sortDir: 'asc',  label: 'Distancia ↑' },
    { sortBy: 'distance', sortDir: 'desc', label: 'Distancia ↓' },
    { sortBy: 'price',    sortDir: 'asc',  label: 'Precio ↑' },
    { sortBy: 'price',    sortDir: 'desc', label: 'Precio ↓' },
  ];

  readonly activeFilterCount = computed(() => {
    let n = 0;
    if (this.filterSurface()) n++;
    if (this.filterCovered()) n++;
    if (this.filterLighting()) n++;
    if (this.filterMaxPrice() != null) n++;
    if (this.filterSortBy() !== 'distance' || this.filterSortDir() !== 'asc') n++;
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
    return opt?.label ?? 'Distancia ↑';
  });

  constructor() {
    this.loadSports();
    this.loadCourts();
  }

  onSportSelect(sport: string | undefined): void {
    this.selectedSport.set(sport);
    this.loadCourts();
  }

  onSearchInput(event: Event): void {
    this.searchText.set((event.target as HTMLInputElement).value);
  }

  onMaxPriceChange(event: CustomEvent): void {
    const v = +event.detail.value;
    this.filterMaxPrice.set(v >= 100 ? undefined : v);
  }

  onSortSelect(sortBy: 'distance' | 'price', sortDir: 'asc' | 'desc'): void {
    this.filterSortBy.set(sortBy);
    this.filterSortDir.set(sortDir);
  }

  onApplyFilters(): void {
    this.showFilters.set(false);
    this.loadCourts();
  }

  onResetFilters(): void {
    this.filterSurface.set(undefined);
    this.filterCovered.set(false);
    this.filterLighting.set(false);
    this.filterMaxPrice.set(undefined);
    this.filterSortBy.set('distance');
    this.filterSortDir.set('asc');
    this.showFilters.set(false);
    this.loadCourts();
  }

  onSurfaceToggle(surface: string): void {
    this.filterSurface.set(this.filterSurface() === surface ? undefined : surface);
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
    this.homeService.getNearbyCourts(undefined, undefined, filters).subscribe({
      next: courts => { this.courts.set(courts); this.loading.set(false); },
      error: ()    => this.loading.set(false),
    });
  }

  private loadSports(): void {
    this.homeService.getTopSportsForFilter().subscribe({
      next: sports => this.sports.set(sports),
    });
  }
}
