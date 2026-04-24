import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { IonContent } from '@ionic/angular/standalone';
import { NearbyCourtItem, NearbyCourtCardComponent } from './nearby-courts/nearby-courts.component';
import { NextBooking, NextBookingCardComponent } from './next-booking-card/next-booking-card.component';
import { RecentActivityItem, RecentActivityItemComponent } from './recent-activity-list/recent-activity.component';
import { GreetingHeaderComponent } from './greeting-header/greeting-header.component';
import { SportFiltersComponent } from './sport-quick-filters-chips/sport-filters.component';
import { FindPlayerComponent } from './find-player/find-player.component';
import { RouterLink } from '@angular/router';
import { HomeService, RecentActivityResponse, SportFilterResponse, GeoService, MediaService } from '@frontend/shared-core';
import { AuthService } from '@frontend/shared-auth';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrl: 'home.page.scss',
  standalone: true,
  imports: [RouterLink, IonContent, GreetingHeaderComponent, NextBookingCardComponent, NearbyCourtCardComponent, SportFiltersComponent, RecentActivityItemComponent, FindPlayerComponent, LucideAngularModule],
})
export class HomePage {
  private homeService  = inject(HomeService);
  private authService  = inject(AuthService);
  private geoService   = inject(GeoService);
  private mediaService = inject(MediaService);

  readonly nextBooking    = signal<NextBooking | null>(null);
  readonly nearbyCourts   = signal<NearbyCourtItem[]>([]);
  readonly recentActivity = signal<RecentActivityItem[]>([]);
  readonly topSports      = signal<SportFilterResponse[]>([]);
  readonly activeSportId  = signal<string>('all');

  // Actividad filtrada por sportId
  readonly filteredActivity = computed(() => {
    const id = this.activeSportId();
    if (id === 'all') return this.recentActivity();
    return this.recentActivity().filter(a => String(a.sportId) === id);
  });

  // Deportes que el usuario ha jugado realmente (para los chips)
  readonly userSports = computed(() => {
    const played = new Set(this.recentActivity().map(a => a.sportId));
    return this.topSports().filter(s => played.has(s.id));
  });

  // Usuario nuevo: sin actividad propia
  readonly isNewUser = computed(() => this.recentActivity().length === 0);

  constructor() {
    this.geoService.requestPosition();

    // Recarga pistas cada vez que cambia la posición (GPS aceptado tarde)
    effect(() => {
      const pos = this.geoService.position();
      if (pos) this.reloadCourtsWithActiveFilter();
    });

    effect(() => {
      if (this.authService.isLoggedIn()) {
        this.reloadAll();
      } else {
        this.clearAll();
      }
    });
  }

  ionViewWillEnter(): void {
    this.reloadAll();
  }

  onSportFilterChange(sportId: string): void {
    this.activeSportId.set(sportId);
    this.reloadCourtsWithActiveFilter();
  }

  private reloadAll(): void {
    this.loadNextBooking();
    this.reloadCourtsWithActiveFilter();
    this.loadRecentActivity();
    this.loadTopSports();
  }

  private clearAll(): void {
    this.nextBooking.set(null);
    this.nearbyCourts.set([]);
    this.recentActivity.set([]);
    this.topSports.set([]);
    this.activeSportId.set('all');
  }

  private loadNextBooking(): void {
    this.homeService.getNextBooking().subscribe({
      next: booking => {
        if (!booking) { this.nextBooking.set(null); return; }
        this.nextBooking.set({
          id: booking.id,
          courtLabel: booking.courtLabel,
          clubName: booking.clubName,
          date: booking.date,
          time: booking.time.slice(0, 5),
          sport: booking.sport,
          pendingPlayers: booking.totalPlayers - booking.confirmedPlayers,
          totalPlayers: booking.totalPlayers,
        });
      },
    });
  }

  private reloadCourtsWithActiveFilter(): void {
    const sportId = untracked(() => this.activeSportId());
    const sportName = sportId === 'all'
      ? undefined
      : untracked(() => this.topSports()).find(s => String(s.id) === sportId)?.name;
    const pos = untracked(() => this.geoService.position());
    this.homeService.getNearbyCourts(pos?.lat, pos?.lon, { sport: sportName }).subscribe({
      next: courts => this.nearbyCourts.set(courts.map(c => this.toCourtItem(c))),
    });
  }

  private loadRecentActivity(): void {
    this.homeService.getRecentActivity().subscribe({
      next: items => this.recentActivity.set(items.map(i => this.toActivityItem(i))),
    });
  }

  private loadTopSports(): void {
    this.homeService.getTopSportsForFilter().subscribe({
      next: sports => this.topSports.set(sports),
    });
  }

  private toCourtItem(c: any): NearbyCourtItem {
    return {
      id: c.id,
      name: c.name,
      clubName: c.clubName,
      clubCity: c.clubCity ?? undefined,
      sport: c.sport,
      surface: c.surface ?? undefined,
      pricePerHour: c.pricePerHour,
      distance: c.distance,
      coverType: c.coverType,
      imageUrl: c.imageUrl ?? undefined,
    };
  }

  private toActivityItem(item: RecentActivityResponse): RecentActivityItem {
    return {
      id: item.id,
      sportId: item.sportId,
      sport: item.sport,
      sportIconAbsUrl: this.mediaService.getFullUrl(item.sportIconUrl),
      description: item.description,
      timeAgo: this.resolveTimeAgo(item.date),
      iconColor: item.sportColor,
    };
  }

  private resolveTimeAgo(isoDate: string): string {
    const days = Math.floor((Date.now() - new Date(isoDate).getTime()) / 86_400_000);
    const weeks = Math.floor(days / 7);
    if (days === 0)       return 'Hoy';
    if (days === 1)       return 'Ayer';
    if (days < 7)         return `Hace ${days} días`;
    if (weeks === 1)      return 'Hace 1 semana';
    if (weeks < 4)        return `Hace ${weeks} semanas`;
    return 'Hace más de 1 mes';
  }
}
