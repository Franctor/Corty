import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonRouterOutlet,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { home, search, calendar, chatbubbles, person } from 'ionicons/icons';
import { BreakpointService } from '@frontend/shared-ui';
import { CortyLogoComponent, ThemeToggleComponent } from '@frontend/shared-ui';

interface NavItem {
  tab:   string;
  href:  string;
  icon:  string;
  label: string;
}

@Component({
  selector: 'app-main',
  templateUrl: 'main.page.html',
  styleUrls: ['main.page.scss'],
  standalone: true,
  imports: [
    RouterLink, RouterLinkActive, RouterOutlet,
    IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonRouterOutlet,
    CortyLogoComponent, ThemeToggleComponent
  ],
})
export class MainPage {
  private bp = inject(BreakpointService);

  readonly isDesktop = computed(() => this.bp.isTablet());

  readonly navItems: NavItem[] = [
    { tab: 'home',     href: '/home',     icon: 'home',        label: 'Inicio'   },
    { tab: 'explore',  href: '/explore',  icon: 'search',      label: 'Explorar' },
    { tab: 'bookings', href: '/bookings', icon: 'calendar',    label: 'Reservas' },
    { tab: 'social',   href: '/social',   icon: 'chatbubbles', label: 'Social'   },
    { tab: 'profile',  href: '/profile',  icon: 'person',      label: 'Perfil'   },
  ];

  constructor() {
    addIcons({ home, search, calendar, chatbubbles, person });
  }
}