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
    data: { roles: ['ADMIN', 'ORGANIZATION'] },
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
        data: { roles: ['ADMIN'] },
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
