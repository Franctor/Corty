import { Routes } from '@angular/router';
import { authGuard, guestGuard } from '@frontend/shared-auth';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.authRoutes),
  },
  {
    path: 'tabs',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./tabs/tabs.routes').then((m) => m.routes),
  },
  {
    path: '',
    redirectTo: '/auth/login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: '/auth/login',
  },
];