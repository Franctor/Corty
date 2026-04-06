import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonIcon, IonPopover } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { search, calendar, chatbubbles, person, notifications } from 'ionicons/icons';
import { BreakpointService, CortyLogoComponent, ThemeToggleComponent } from '@frontend/shared-ui';
import { ProfileMenuComponent } from './profile-menu/profile-menu.component';

interface NavItem {
  tab:   string;
  href:  string;
  icon:  string;
  label: string;
}

@Component({
  selector: 'app-topnav',
  templateUrl: './topnav.component.html',
  styleUrls: ['./topnav.component.scss'],
  standalone: true,
  imports: [
    RouterLink, RouterLinkActive,
    IonIcon, IonPopover,
    CortyLogoComponent, ThemeToggleComponent,
    ProfileMenuComponent,
  ],
})
export class TopnavComponent {
  private bp = inject(BreakpointService);

  readonly isDesktop = computed(() => this.bp.isTablet());
  readonly isProfileMenuOpen = signal(false);

  readonly centerNavItems: NavItem[] = [
    { tab: 'explore',  href: '/explore',  icon: 'search',      label: 'Explorar' },
    { tab: 'bookings', href: '/bookings', icon: 'calendar',    label: 'Reservas' },
    { tab: 'social',   href: '/social',   icon: 'chatbubbles', label: 'Social'   },
  ];

  constructor() {
    addIcons({ search, calendar, chatbubbles, person, notifications });
  }

  openProfileMenu(): void {
    this.isProfileMenuOpen.set(true);
  }

  closeProfileMenu(): void {
    this.isProfileMenuOpen.set(false);
  }
}