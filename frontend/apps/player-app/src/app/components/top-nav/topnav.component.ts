import { Component, computed, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonIcon, IonPopover } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { search, calendar, chatbubbles, person, notifications } from 'ionicons/icons';
import { BreakpointService, CortyLogoComponent, ThemeToggleComponent } from '@frontend/shared-ui';
import { ProfileMenuComponent } from './profile-menu/profile-menu.component';
import { NotificationService } from '@frontend/shared-core';
import { TokenService } from '@frontend/shared-auth';
import { NotifDropdownComponent } from './notif-dropdown/notif-dropdown.component';

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
    ProfileMenuComponent, NotifDropdownComponent,
  ],
})
export class TopnavComponent implements OnInit, OnDestroy {
  private bp                  = inject(BreakpointService);
  private tokenService        = inject(TokenService);
  readonly notificationService = inject(NotificationService);

  readonly isDesktop          = computed(() => this.bp.isTablet());
  readonly isProfileMenuOpen  = signal(false);
  readonly isNotifOpen        = signal(false);
  readonly unreadCount        = computed(() => this.notificationService.unreadCount());

  readonly centerNavItems: NavItem[] = [
    { tab: 'explore',  href: '/explore',  icon: 'search',      label: 'Explorar' },
    { tab: 'bookings', href: '/bookings', icon: 'calendar',    label: 'Reservas' },
    { tab: 'social',   href: '/social',   icon: 'chatbubbles', label: 'Social'   },
  ];

  constructor() {
    addIcons({ search, calendar, chatbubbles, person, notifications });
  }

  ngOnInit(): void {
    this.notificationService.loadUnreadCount();
    this.notificationService.loadLatest();
    const token = this.tokenService.get();
    if (token) this.notificationService.connectSse(token);
  }

  ngOnDestroy(): void {
    this.notificationService.disconnectSse();
  }

  openProfileMenu(): void  { this.isProfileMenuOpen.set(true); }
  closeProfileMenu(): void { this.isProfileMenuOpen.set(false); }
  openNotif(): void        { this.isNotifOpen.set(true); }
  closeNotif(): void       { this.isNotifOpen.set(false); }
}
