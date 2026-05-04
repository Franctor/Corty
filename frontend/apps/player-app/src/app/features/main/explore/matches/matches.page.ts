import { Component, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { NgTemplateOutlet } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { interval } from 'rxjs';
import { IonContent, IonSpinner, IonModal, IonRange } from '@ionic/angular/standalone';
import { BookingService, GeoService, HomeService, NotificationService, PublicBookingResponse, SportFilterResponse } from '@frontend/shared-core';
import { PageHeaderComponent } from '../../../../components/page-header/page-header.component';
import { LucideAngularModule } from 'lucide-angular';
import { UiDaterangePickerComponent, DateRange } from '../../../../components/forms/ui-daterange-picker/ui-daterange-picker.component';
import { MatchCardComponent } from './components/match-card/match-card.component';

@Component({
  selector: 'app-matches',
  templateUrl: './matches.page.html',
  styleUrls: ['./matches.page.scss'],
  standalone: true,
  imports: [
    IonContent, IonSpinner, IonModal, IonRange,
    RouterLink, NgTemplateOutlet, ReactiveFormsModule,
    PageHeaderComponent, LucideAngularModule,
    MatchCardComponent, UiDaterangePickerComponent,
  ],
})
export class MatchesPage {
  private bookingService       = inject(BookingService);
  private homeService          = inject(HomeService);
  private notificationService  = inject(NotificationService);
  readonly geoService          = inject(GeoService);

  readonly state   = signal<{ loading: boolean; matches: PublicBookingResponse[] }>({ loading: true, matches: [] });
  readonly sports  = signal<SportFilterResponse[]>([]);
  readonly showFilters = signal(false);

  readonly filterSport    = signal<string | undefined>(undefined);
  readonly filterSortDir  = signal<'asc' | 'desc'>('asc');
  readonly filterLevelMin = signal(0);
  readonly filterLevelMax = signal(10);

  readonly DISTANCE_STEPS = [0.1, 0.2, 0.5, 1, 2, 3, 5, 8, 10, 15, 20, 30, 50, 75, 100, 200];
  readonly distanceStepIndex = signal(0); // 100m por defecto
  readonly filterMaxDistanceDisplay = signal<number | undefined>(0.1);

  readonly dateRangeControl = new FormControl<DateRange>({ from: null, to: null });

  readonly activeFilterCount = () => {
    let n = 0;
    if (this.filterSport()) n++;
    const dr = this.dateRangeControl.value;
    if (dr?.from || dr?.to) n++;
    if (this.distanceStepIndex() > 0 && this.distanceStepIndex() < this.DISTANCE_STEPS.length) n++;
    if (this.filterSortDir() !== 'asc') n++;
    if (this.filterLevelMin() > 0) n++;
    if (this.filterLevelMax() < 10) n++;
    return n;
  };

  readonly distanceFormatter = (idx: number): string => {
    if (idx >= this.DISTANCE_STEPS.length) return '∞';
    const km = this.DISTANCE_STEPS[idx];
    return km < 1 ? `${km * 1000 | 0}m` : `${km}km`;
  };

  formatDistanceLabel(km: number): string {
    if (km < 1) return `${km * 1000 | 0} m`;
    return Number.isInteger(km) ? `${km} km` : `${km} km`;
  }

  constructor() {
    this.geoService.requestPosition();
    this.homeService.getTopSportsForFilter().subscribe({ next: s => this.sports.set(s) });
    this.dateRangeControl.valueChanges.subscribe(() => this.load());
    effect(() => {
      if (this.geoService.resolved()) this.load();
    });
    effect(() => {
      const notif = this.notificationService.lastEvent();
      if (notif?.type === 'BOOKING_CANCELLED') {
        this.state.update(s => ({
          ...s,
          matches: s.matches.filter(m => m.id !== Number(notif.referenceId)),
        }));
      }
    });

    interval(30_000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        if (this.geoService.resolved()) this.loadSilent();
      });
  }

  ionViewWillEnter(): void {
    if (this.geoService.resolved()) this.load();
  }

  onSportSelect(sport: string | undefined): void {
    this.filterSport.set(sport);
    this.load();
  }

  onMaxDistanceChange(event: CustomEvent): void {
    const idx = +event.detail.value;
    this.distanceStepIndex.set(idx);
    const km = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : undefined;
    this.filterMaxDistanceDisplay.set(km);
    this.load();
  }

  onMaxDistanceInput(event: CustomEvent): void {
    const idx = +event.detail.value;
    const km = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : undefined;
    this.filterMaxDistanceDisplay.set(km);
  }

  onLevelMinChange(event: CustomEvent): void {
    this.filterLevelMin.set(+event.detail.value);
    this.load();
  }

  onLevelMaxChange(event: CustomEvent): void {
    this.filterLevelMax.set(+event.detail.value);
    this.load();
  }

  onSortDirToggle(dir: 'asc' | 'desc'): void {
    this.filterSortDir.set(dir);
    this.load();
  }

  onResetFilters(): void {
    this.filterSport.set(undefined);
    this.dateRangeControl.setValue({ from: null, to: null }, { emitEvent: false });
    this.distanceStepIndex.set(0);
    this.filterMaxDistanceDisplay.set(0.1);
    this.filterSortDir.set('asc');
    this.filterLevelMin.set(0);
    this.filterLevelMax.set(10);
    this.showFilters.set(false);
    this.load();
  }

  onJoinSent(bookingId: number): void {
    this.state.update(s => ({
      ...s,
      matches: s.matches.map(m =>
        m.id === bookingId ? { ...m, myRequestStatus: 'PENDING' as const } : m
      ),
    }));
  }

  private loadSilent(): void {
    const pos = this.geoService.position();
    const idx = this.distanceStepIndex();
    const radiusKm = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : 5000;
    const dr = this.dateRangeControl.value;
    this.bookingService.getPublicBookings({
      lat: pos?.lat, lon: pos?.lon, radiusKm,
      sport: this.filterSport(),
      dateFrom: dr?.from ?? undefined,
      dateTo: dr?.to ?? undefined,
      levelMin: this.filterLevelMin() > 0 ? this.filterLevelMin() : undefined,
      levelMax: this.filterLevelMax() < 10 ? this.filterLevelMax() : undefined,
    }).subscribe({
      next: data => this.state.update(s => ({ ...s, matches: data })),
    });
  }

  private load(): void {
    this.state.set({ loading: true, matches: [] });
    const pos = this.geoService.position();
    const idx = this.distanceStepIndex();
    const radiusKm = idx < this.DISTANCE_STEPS.length ? this.DISTANCE_STEPS[idx] : 5000;
    const dr = this.dateRangeControl.value;
    this.bookingService.getPublicBookings({
      lat: pos?.lat,
      lon: pos?.lon,
      radiusKm,
      sport: this.filterSport(),
      dateFrom: dr?.from ?? undefined,
      dateTo: dr?.to ?? undefined,
      levelMin: this.filterLevelMin() > 0 ? this.filterLevelMin() : undefined,
      levelMax: this.filterLevelMax() < 10 ? this.filterLevelMax() : undefined,
    }).subscribe({
      next: data => this.state.set({ loading: false, matches: data }),
      error: ()  => this.state.set({ loading: false, matches: [] }),
    });
  }
}
