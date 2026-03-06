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
    path: '',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/main/main.routes').then((m) => m.mainRoutes),
  },
  {
    path: '**',
    redirectTo: '/auth/login',
  },
];