import { Component, inject, computed, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '@frontend/shared-auth';
import { CortyLogoComponent } from '@frontend/shared-ui';
import { LucideAngularModule } from 'lucide-angular';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  roles: string[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard',      route: '/dashboard',    icon: 'layout-dashboard', roles: ['ADMIN', 'ORGANIZATION'] },
  { label: 'Clubes',         route: '/clubs',         icon: 'building-2',       roles: ['ADMIN', 'ORGANIZATION'] },
  { label: 'Pistas',         route: '/courts',        icon: 'map-pin',          roles: ['ADMIN', 'ORGANIZATION'] },
  { label: 'Reservas',       route: '/bookings',      icon: 'calendar-days',    roles: ['ADMIN', 'ORGANIZATION'] },
  { label: 'Usuarios',       route: '/users',         icon: 'users',            roles: ['ADMIN'] },
  { label: 'Organizaciones', route: '/organizations', icon: 'briefcase',        roles: ['ADMIN'] },
  { label: 'Deportes',       route: '/sports',        icon: 'dumbbell',         roles: ['ADMIN'] },
  { label: 'Superficies',    route: '/surfaces',      icon: 'layers',           roles: ['ADMIN'] },
];

@Component({
  selector: 'app-sidebar',
  templateUrl: 'sidebar.component.html',
  styleUrl: 'sidebar.component.scss',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CortyLogoComponent, LucideAngularModule],
})
export class SidebarComponent {
  private authService = inject(AuthService);

  readonly navItemClick = output();

  readonly role = computed(() => this.authService.getRole());

  readonly navItems = computed(() =>
    NAV_ITEMS.filter((item) => {
      const r = this.role();
      return r ? item.roles.includes(r) : false;
    })
  );
}
