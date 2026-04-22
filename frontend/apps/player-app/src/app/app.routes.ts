import { Routes } from '@angular/router';
import { authGuard, guestGuard } from '@frontend/shared-auth';

export const routes: Routes = [
  {
    path: 'auth/activate',
    loadComponent: () =>
      import('./features/auth/activate/activate.page').then((m) => m.ActivatePage),
  },
  {
    path: 'auth/check-email',
    loadComponent: () =>
      import('./features/auth/check-email/check-email.page').then((m) => m.CheckEmailPage),
  },
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