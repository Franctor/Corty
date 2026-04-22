import { Route } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from '@frontend/shared-auth';

export const appRoutes: Route[] = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.authRoutes),
  },
  {
    path: '',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPERADMIN', 'ORGANIZATION'] },
    loadComponent: () =>
      import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'sports',
        loadComponent: () =>
          import('./features/sports/sports.component').then(
            (m) => m.SportsComponent
          ),
      },
      {
        path: 'surfaces',
        loadComponent: () =>
          import('./features/surfaces/surfaces.component').then(
            (m) => m.SurfacesComponent
          ),
        data: { roles: ['ADMIN', 'SUPERADMIN'] },
      },
      {
        path: 'clubs',
        loadComponent: () =>
          import('./features/clubs/clubs.component').then(
            (m) => m.ClubsComponent
          ),
      },
      {
        path: 'courts',
        loadComponent: () =>
          import('./features/courts/courts.component').then(
            (m) => m.CourtsComponent
          ),
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/users/users.component').then(
            (m) => m.UsersComponent
          ),
        data: { roles: ['ADMIN', 'SUPERADMIN'] },
      },
      {
        path: 'players',
        loadComponent: () =>
          import('./features/players/players.component').then(
            (m) => m.PlayersComponent
          ),
        data: { roles: ['ADMIN', 'SUPERADMIN'] },
      },
      {
        path: 'organizations',
        loadComponent: () =>
          import('./features/organizations/organizations.component').then(
            (m) => m.OrganizationsComponent
          ),
        data: { roles: ['ADMIN', 'SUPERADMIN'] },
      },
      {
        path: 'bookings',
        loadComponent: () =>
          import('./features/bookings/bookings.component').then(
            (m) => m.BookingsComponent
          ),
      },
    ],
  },
  {
    path: 'forbidden',
    loadComponent: () =>
      import('./features/forbidden/forbidden.component').then(
        (m) => m.ForbiddenComponent
      ),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
