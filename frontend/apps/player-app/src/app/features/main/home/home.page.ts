import { Component, signal } from '@angular/core';
import { IonContent } from '@ionic/angular/standalone';
import { NearbyCourtItem, NearbyCourtCardComponent } from './nearby-courts/nearby-courts.component';
import { NextBooking, NextBookingCardComponent } from './next-booking-card/next-booking-card.component';
import { RecentActivityItem, RecentActivityItemComponent } from './recent-activity-list/recent-activity.component';
import { GreetingHeaderComponent } from "./greeting-header/greeting-header.component";
import { SportFiltersComponent } from "./sport-quick-filters-chips/sport-filters.component";
import { FindPlayerComponent } from "./find-player/find-player.component";
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrl: 'home.page.scss',
  standalone: true,
  imports: [ RouterLink ,IonContent, GreetingHeaderComponent, NextBookingCardComponent, NearbyCourtCardComponent, SportFiltersComponent, RecentActivityItemComponent, FindPlayerComponent],
})
export class HomePage {

  constructor() { };

  // Mock data — replace with real API calls when backend is ready
  readonly nextBooking = signal<NextBooking | null>({
    id: 1,
    courtLabel: 'Court A',
    clubName: 'Club Corty',
    date: new Date(Date.now() + 2 * 24 * 60 * 60 * 1000).toISOString(),
    time: '18:00',
    sport: 'Pádel',
    pendingPlayers: 2,
    totalPlayers: 4,
  });

  readonly nearbyCourts = signal<NearbyCourtItem[]>([
    {
      id: 1,
      name: 'Club Deportivo Elite',
      clubName: 'CDElite',
      sport: 'PÁDEL PRO',
      coverType: 'indoor',
      pricePerHour: 24,
      distance: 1.2,
    },
    {
      id: 2,
      name: 'Arena City Center',
      clubName: 'CdSpeed',
      sport: 'FÚTBOL 7',
      coverType: 'outdoor',
      pricePerHour: 18,
      distance: 2.8,
    },
    {
      id: 3,
      name: 'Pistas Municipales',
      clubName: 'Ayuntamiento',
      sport: 'TENIS',
      coverType: 'outdoor',
      pricePerHour: 8,
      distance: 0.8,
    },
  ]);

  readonly recentActivity = signal<RecentActivityItem[]>([
    {
      id: 10,
      sport: 'Fútbol',
      sportIcon: 'football-outline',
      description: 'Fútbol 7 — Goleadores',
      result: '12-8 (Victoria)', //No se usa
      timeAgo: 'Hace 2 días',
      iconColor: '#58CC02',
    },
    {
      id: 11,
      sport: 'Pádel',
      sportIcon: 'tennisball-outline',
      description: 'Pádel Mixto',
      result: '6-4, 7-5 (Victoria)', //No se usa
      timeAgo: 'Hace 4 días',
      iconColor: '#1CB0F6',
    },
    {
      id: 12,
      sport: 'Baloncesto',
      sportIcon: 'basketball-outline',
      description: 'Basket 3×3 Street',
      result: '21-19 (Victoria)', //No se usa
      timeAgo: 'Hace 1 semana',
      iconColor: '#FF9600',
    },
  ]);

  onSportFilterChange(sportId: string): void {
    // Will filter nearbyCourts when API is connected
    console.log('Sport filter:', sportId);
  }
}