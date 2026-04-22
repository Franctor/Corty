import { Component, effect, inject, signal } from '@angular/core';
import { IonContent } from '@ionic/angular/standalone';
import { NearbyCourtItem, NearbyCourtCardComponent } from './nearby-courts/nearby-courts.component';
import { NextBooking, NextBookingCardComponent } from './next-booking-card/next-booking-card.component';
import { RecentActivityItem, RecentActivityItemComponent } from './recent-activity-list/recent-activity.component';
import { GreetingHeaderComponent } from './greeting-header/greeting-header.component';
import { SportFiltersComponent } from './sport-quick-filters-chips/sport-filters.component';
import { FindPlayerComponent } from './find-player/find-player.component';
import { RouterLink } from '@angular/router';
import { HomeService, RecentActivityResponse, SportFilterResponse } from '@frontend/shared-core';
import { AuthService } from '@frontend/shared-auth';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrl: 'home.page.scss',
  standalone: true,
  imports: [RouterLink, IonContent, GreetingHeaderComponent, NextBookingCardComponent, NearbyCourtCardComponent, SportFiltersComponent, RecentActivityItemComponent, FindPlayerComponent],
})
export class HomePage {
  private homeService = inject(HomeService);
  private authService = inject(AuthService);

  readonly nextBooking = signal<NextBooking | null>(null);
  readonly nearbyCourts = signal<NearbyCourtItem[]>([]);
  readonly recentActivity = signal<RecentActivityItem[]>([]);
  readonly topSports = signal<SportFilterResponse[]>([]);

  constructor() {
    // Recarga los datos cada vez que el usuario cambia (login/logout)
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

  private reloadAll(): void {
    this.loadNextBooking();
    this.loadNearbyCourts();
    this.loadRecentActivity();
    this.loadTopSports();
  }

  private clearAll(): void {
    this.nextBooking.set(null);
    this.nearbyCourts.set([]);
    this.recentActivity.set([]);
    this.topSports.set([]);
  }

  onSportFilterChange(sportId: string): void {
    const sport = sportId === 'all' ? undefined : sportId;
    this.homeService.getNearbyCourts(undefined, undefined, { sport }).subscribe({
      next: courts => this.nearbyCourts.set(courts.map(c => ({
        id: c.id,
        name: c.name,
        clubName: c.clubName,
        sport: c.sport,
        surface: c.surface ?? undefined,
        pricePerHour: c.pricePerHour,
        distance: c.distance,
        coverType: c.coverType,
      }))),
    });
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
          time: booking.time.slice(0, 5), // "HH:MM:SS" → "HH:MM"
          sport: booking.sport,
          pendingPlayers: booking.totalPlayers - booking.confirmedPlayers,
          totalPlayers: booking.totalPlayers,
        });
      },
    });
  }

  private loadNearbyCourts(): void {
    this.homeService.getNearbyCourts().subscribe({
      next: courts => this.nearbyCourts.set(courts.map(c => ({
        id: c.id,
        name: c.name,
        clubName: c.clubName,
        sport: c.sport,
        surface: c.surface ?? undefined,
        pricePerHour: c.pricePerHour,
        distance: c.distance,
        coverType: c.coverType,
      }))),
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

  private toActivityItem(item: RecentActivityResponse): RecentActivityItem {
    return {
      id: item.id,
      sport: item.sport,
      sportIcon: item.sportIconUrl,
      description: item.description,
      timeAgo: this.resolveTimeAgo(item.date),
      iconColor: item.sportColor,
    };
  }

  private resolveTimeAgo(isoDate: string): string {
    const days = Math.floor((Date.now() - new Date(isoDate).getTime()) / 86_400_000);
    const weeks = Math.floor(days / 7);
    let label: string;
    if (days === 0)      label = 'Hoy';
    else if (days === 1) label = 'Ayer';
    else if (days < 7)   label = `Hace ${days} días`;
    else if (weeks === 1) label = 'Hace 1 semana';
    else if (weeks < 4)   label = `Hace ${weeks} semanas`;
    else                   label = 'Hace más de 1 mes';
    return label;
  }
}